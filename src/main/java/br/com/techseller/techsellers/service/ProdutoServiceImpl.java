package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.entity.Produto;
import br.com.techseller.techsellers.repository.ImagemProdutoRepository;
import br.com.techseller.techsellers.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
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
    @Transactional(readOnly = true)
    public Optional<Produto> buscarPorId(Long produto_id) {
        return produtoRepository.findById(produto_id);
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

            // Processa a imagem
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

        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar imagem: " + e.getMessage(), e);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Erro de integridade: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void editarProduto(Produto produto, MultipartFile imagem, boolean imagemPrincipal) {
        Produto produtoExistente = produtoRepository.findById(produto.getProdutoId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        produtoExistente.setNome(produto.getNome());
        produtoExistente.setDescricaoDetalhada(produto.getDescricaoDetalhada());
        produtoExistente.setPreco(produto.getPreco());
        produtoExistente.setQuantidadeEstoque(produto.getQuantidadeEstoque());
        produtoExistente.setAtivo(produto.getAtivo());

        if (imagem != null && !imagem.isEmpty()) {
            try {
                // Remove imagens antigas
                List<ImagemProduto> imagensAntigas = produtoExistente.getImagens();
                if (imagensAntigas != null && !imagensAntigas.isEmpty()) {
                    imagensAntigas.forEach(img -> {
                        try {
                            armazenamentoImagemService.removerImagem(img.getCaminhoArquivo());
                        } catch (IOException e) {
                            throw new RuntimeException("Erro ao remover imagem antiga: " + e.getMessage());
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

            } catch (IOException e) {
                throw new RuntimeException("Erro ao processar imagem: " + e.getMessage());
            }
        }

        produtoRepository.save(produtoExistente);
    }

    @Override
    @Transactional
    public void inativarProduto(Long produto_id) {
        produtoRepository.findById(produto_id).ifPresent(produto -> {
            produto.setAtivo(false);
            produtoRepository.save(produto);
        });
    }

    @Override
    @Transactional
    public void reativarProduto(Long produto_id) {
        produtoRepository.findById(produto_id).ifPresent(produto -> {
            produto.setAtivo(true);
            produtoRepository.save(produto);
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

        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar imagem: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImagemProduto> listarImagensPorProduto(Long produtoId) {
        return imagemProdutoRepository.findByProdutoProdutoId(produtoId);
    }

    @Override
    @Deprecated
    @Transactional
    public Produto editarProduto(Produto produto) {
        if (produto.getProdutoId() == null) {
            throw new IllegalArgumentException("ID do produto é obrigatório");
        }

        Produto produtoExistente = produtoRepository.findById(produto.getProdutoId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        produtoExistente.setNome(produto.getNome());
        produtoExistente.setDescricaoDetalhada(produto.getDescricaoDetalhada());
        produtoExistente.setPreco(produto.getPreco());
        produtoExistente.setQuantidadeEstoque(produto.getQuantidadeEstoque());

        if (produto.getAvaliacao() != null) {
            produtoExistente.setAvaliacao(produto.getAvaliacao());
        }

        if (produto.getAtivo() != null) {
            produtoExistente.setAtivo(produto.getAtivo());
        }

        return produtoRepository.save(produtoExistente);
    }

    @Override
    public ImagemProduto buscarImagemPorId(Long imagemId) {
        return imagemProdutoRepository.findById(imagemId)
                .orElseThrow(() -> new RuntimeException("Imagem não encontrada"));
    }
}