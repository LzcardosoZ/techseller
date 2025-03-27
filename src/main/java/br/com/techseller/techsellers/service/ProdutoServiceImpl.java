package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.entity.Produto;
import br.com.techseller.techsellers.repository.ImagemProdutoRepository;
import br.com.techseller.techsellers.repository.ProdutoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ImagemProdutoRepository imagemProdutoRepository;
    private final ArmazenamentoImagemService armazenamentoImagemService;

    @Autowired
    public ProdutoServiceImpl(
            ProdutoRepository produtoRepository,
            ImagemProdutoRepository imagemProdutoRepository,
            ArmazenamentoImagemService armazenamentoImagemService
    ) {
        this.produtoRepository = produtoRepository;
        this.imagemProdutoRepository = imagemProdutoRepository;
        this.armazenamentoImagemService = armazenamentoImagemService;
    }

    // ============== IMPLEMENTAÇÕES DOS MÉTODOS ==============

    @Override
    @Transactional(readOnly = true)
    public List<Produto> listarProdutos(String filtro) {
        List<Produto> produtos;
        if (filtro != null && !filtro.isEmpty()) {
            produtos = produtoRepository.findByNomeOrId(filtro);
        } else {
            produtos = produtoRepository.findAll();
        }

        produtos.forEach(produto -> {
            List<ImagemProduto> imagens = imagemProdutoRepository.findByProdutoProdutoId(produto.getProdutoId());
            produto.setImagens(imagens != null ? imagens : new ArrayList<>());
        });

        return produtos;
    }

    @Override
    @Transactional
    public void salvarProduto(Produto produto, MultipartFile imagem, boolean imagemPrincipal) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo");
        }

        if (imagem == null || imagem.isEmpty()) {
            throw new IllegalArgumentException("O produto deve ter pelo menos uma imagem");
        }

        try {
            // Valores padrão
            if (produto.getAvaliacao() == null) {
                produto.setAvaliacao(new BigDecimal("0.0"));
            }
            if (produto.getAtivo() == null) {
                produto.setAtivo(true);
            }

            // Salva o produto primeiro para gerar ID
            produto = produtoRepository.save(produto);

            // Processa e salva a imagem usando ArmazenamentoImagemService
            String caminhoRelativo = armazenamentoImagemService.salvarImagem(imagem, produto.getProdutoId());

            ImagemProduto imagemProduto = ImagemProduto.builder()
                    .imagemPrincipal(imagemPrincipal)
                    .caminhoArquivo(caminhoRelativo)
                    .nomeArquivo(imagem.getOriginalFilename())
                    .tamanho(imagem.getSize())
                    .tipoMime(imagem.getContentType())
                    .produto(produto)
                    .build();

            imagemProdutoRepository.save(imagemProduto);
            produto.adicionarImagem(imagemProduto);
            produtoRepository.save(produto);

            log.info("Produto salvo com sucesso: ID {}", produto.getProdutoId());

        } catch (IOException e) {
            log.error("Erro ao processar imagem: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar imagem do produto");
        } catch (DataIntegrityViolationException e) {
            log.error("Erro de integridade: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar produto");
        }
    }

    @Override
    @Transactional
    public void editarProduto(Produto produto, MultipartFile imagem, boolean imagemPrincipal) {
        Produto produtoExistente = produtoRepository.findById(produto.getProdutoId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // Atualiza campos básicos
        produtoExistente.setNome(produto.getNome());
        produtoExistente.setDescricaoDetalhada(produto.getDescricaoDetalhada());
        produtoExistente.setPreco(produto.getPreco());
        produtoExistente.setQuantidadeEstoque(produto.getQuantidadeEstoque());
        produtoExistente.setAtivo(produto.getAtivo());

        // Atualiza imagem se fornecida
        if (imagem != null && !imagem.isEmpty()) {
            try {
                // Remove imagens antigas
                List<ImagemProduto> imagensAntigas = produtoExistente.getImagens();
                if (imagensAntigas != null && !imagensAntigas.isEmpty()) {
                    imagensAntigas.forEach(img -> {
                        try {
                            armazenamentoImagemService.removerImagem(img.getCaminhoArquivo());
                        } catch (IOException e) {
                            log.error("Erro ao remover imagem: {}", img.getCaminhoArquivo(), e);
                        }
                    });
                    imagemProdutoRepository.deleteAll(imagensAntigas);
                }

                // Salva nova imagem
                String caminhoRelativo = armazenamentoImagemService.salvarImagem(imagem, produtoExistente.getProdutoId());

                ImagemProduto novaImagem = ImagemProduto.builder()
                        .imagemPrincipal(imagemPrincipal)
                        .caminhoArquivo(caminhoRelativo)
                        .nomeArquivo(imagem.getOriginalFilename())
                        .tamanho(imagem.getSize())
                        .tipoMime(imagem.getContentType())
                        .produto(produtoExistente)
                        .build();

                imagemProdutoRepository.save(novaImagem);
                produtoExistente.getImagens().clear();
                produtoExistente.adicionarImagem(novaImagem);

                log.info("Imagem atualizada para o produto ID: {}", produtoExistente.getProdutoId());

            } catch (IOException e) {
                log.error("Erro ao processar imagem: {}", e.getMessage(), e);
                throw new RuntimeException("Falha ao atualizar imagem");
            }
        }

        produtoRepository.save(produtoExistente);
        log.info("Produto atualizado: ID {}", produtoExistente.getProdutoId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Produto> buscarPorId(Long produtoId) {
        try {
            if (produtoId == null || produtoId <= 0) {
                throw new IllegalArgumentException("ID do produto inválido");
            }

            Optional<Produto> produtoOpt = produtoRepository.findById(produtoId);

            produtoOpt.ifPresent(produto -> {
                List<ImagemProduto> imagens = imagemProdutoRepository.findByProdutoProdutoId(produtoId);
                produto.setImagens(imagens != null ? imagens : List.of());
            });

            return produtoOpt;

        } catch (Exception e) {
            log.error("Erro ao buscar produto por ID: {}", produtoId, e);
            throw new RuntimeException("Erro ao buscar produto");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ImagemProduto buscarImagemPorId(Long imagemId) {
        return imagemProdutoRepository.findById(imagemId)
                .orElseThrow(() -> {
                    log.warn("Imagem não encontrada: ID {}", imagemId);
                    return new RuntimeException("Imagem não encontrada");
                });
    }

    @Override
    @Transactional
    public void inativarProduto(Long produtoId) {
        produtoRepository.findById(produtoId).ifPresent(produto -> {
            produto.setAtivo(false);
            produtoRepository.save(produto);
            log.info("Produto inativado: ID {}", produtoId);
        });
    }

    @Override
    @Transactional
    public void reativarProduto(Long produtoId) {
        produtoRepository.findById(produtoId).ifPresent(produto -> {
            produto.setAtivo(true);
            produtoRepository.save(produto);
            log.info("Produto reativado: ID {}", produtoId);
        });
    }

    @Override
    @Transactional
    public void salvarImagem(Long produtoId, MultipartFile file, boolean imagemPrincipal) {
        try {
            Produto produto = produtoRepository.findById(produtoId)
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            String caminhoRelativo = armazenamentoImagemService.salvarImagem(file, produtoId);

            ImagemProduto imagemProduto = ImagemProduto.builder()
                    .imagemPrincipal(imagemPrincipal)
                    .caminhoArquivo(caminhoRelativo)
                    .nomeArquivo(file.getOriginalFilename())
                    .tamanho(file.getSize())
                    .tipoMime(file.getContentType())
                    .produto(produto)
                    .build();

            imagemProdutoRepository.save(imagemProduto);
            produto.adicionarImagem(imagemProduto);
            produtoRepository.save(produto);

            log.info("Imagem adicional salva para o produto ID: {}", produtoId);

        } catch (IOException e) {
            log.error("Erro ao salvar imagem: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao salvar imagem");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImagemProduto> listarImagensPorProduto(Long produtoId) {
        try {
            return imagemProdutoRepository.findByProdutoProdutoId(produtoId);
        } catch (Exception e) {
            log.error("Erro ao listar imagens: Produto ID {}", produtoId, e);
            throw new RuntimeException("Erro ao carregar imagens");
        }
    }

    @Override
    @Deprecated
    @Transactional
    public Produto editarProduto(Produto produto) {
        if (produto.getProdutoId() == null) {
            throw new IllegalArgumentException("ID do produto é obrigatório");
        }

        return produtoRepository.findById(produto.getProdutoId())
                .map(produtoExistente -> {
                    atualizarCamposProduto(produtoExistente, produto);
                    Produto produtoAtualizado = produtoRepository.save(produtoExistente);
                    log.info("Produto atualizado (método deprecated): ID {}", produtoAtualizado.getProdutoId());
                    return produtoAtualizado;
                })
                .orElseThrow(() -> {
                    log.error("Produto não encontrado: ID {}", produto.getProdutoId());
                    return new RuntimeException("Produto não encontrado");
                });
    }

    // ============== MÉTODOS AUXILIARES PRIVADOS ==============

    private void atualizarCamposProduto(Produto existente, Produto novo) {
        existente.setNome(novo.getNome());
        existente.setDescricaoDetalhada(novo.getDescricaoDetalhada());
        existente.setPreco(novo.getPreco());
        existente.setQuantidadeEstoque(novo.getQuantidadeEstoque());

        if (novo.getAvaliacao() != null) {
            existente.setAvaliacao(novo.getAvaliacao());
        }

        if (novo.getAtivo() != null) {
            existente.setAtivo(novo.getAtivo());
        }
    }
}