package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.entity.Produto;
import br.com.techseller.techsellers.repository.ImagemProdutoRepository;
import br.com.techseller.techsellers.repository.ProdutoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ImagemProdutoRepository imagemProdutoRepository;

    @Value("${app.upload.dir}")
    private String diretorioBase;

    @Autowired
    public ProdutoServiceImpl(ProdutoRepository produtoRepository,
                              ImagemProdutoRepository imagemProdutoRepository) {
        this.produtoRepository = produtoRepository;
        this.imagemProdutoRepository = imagemProdutoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Produto> listarProdutos(String filtro) {
        try {
            List<Produto> produtos = (filtro != null && !filtro.isEmpty())
                    ? produtoRepository.findByNomeOrId(filtro)
                    : produtoRepository.findAll();

            produtos.forEach(produto -> {
                List<ImagemProduto> imagens = imagemProdutoRepository.findByProdutoProdutoId(produto.getProdutoId());
                produto.setImagens(imagens != null ? imagens : List.of());
            });

            return produtos;
        } catch (Exception e) {
            log.error("Erro ao listar produtos", e);
            throw new RuntimeException("Erro ao listar produtos: " + e.getMessage(), e);
        }
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
            // Configura valores padrão
            produto.setAvaliacao(produto.getAvaliacao() != null ? produto.getAvaliacao() : BigDecimal.ZERO);
            produto.setAtivo(produto.getAtivo() != null ? produto.getAtivo() : true);

            Produto produtoSalvo = produtoRepository.save(produto);
            salvarImagemComTratamento(produtoSalvo, imagem, imagemPrincipal);

        } catch (DataIntegrityViolationException e) {
            log.error("Erro de integridade ao salvar produto", e);
            throw new RuntimeException("Erro ao salvar produto: dados inválidos ou duplicados", e);
        } catch (Exception e) {
            log.error("Erro ao salvar produto", e);
            throw new RuntimeException("Erro ao salvar produto", e);
        }
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
            throw new RuntimeException("Erro ao buscar produto: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ImagemProduto buscarImagemPorId(Long imagemId) {
        return imagemProdutoRepository.findById(imagemId)
                .orElseThrow(() -> {
                    log.warn("Imagem não encontrada com ID: {}", imagemId);
                    return new RuntimeException("Imagem não encontrada");
                });
    }

    @Override
    @Transactional
    public void inativarProduto(Long produtoId) {
        alterarStatusProduto(produtoId, false);
    }

    @Override
    @Transactional
    public void reativarProduto(Long produtoId) {
        alterarStatusProduto(produtoId, true);
    }

    @Override
    @Transactional
    public void salvarImagem(Long produtoId, MultipartFile file, boolean imagemPrincipal) {
        try {
            Produto produto = produtoRepository.findById(produtoId)
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            salvarImagemComTratamento(produto, file, imagemPrincipal);

        } catch (Exception e) {
            log.error("Erro ao salvar imagem para o produto ID: {}", produtoId, e);
            throw new RuntimeException("Erro ao salvar imagem", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImagemProduto> listarImagensPorProduto(Long produtoId) {
        try {
            return imagemProdutoRepository.findByProdutoProdutoId(produtoId);
        } catch (Exception e) {
            log.error("Erro ao listar imagens do produto ID: {}", produtoId, e);
            throw new RuntimeException("Erro ao listar imagens", e);
        }
    }

    @Override
    @Transactional
    public Produto editarProduto(Produto produto) {
        if (produto.getProdutoId() == null) {
            throw new IllegalArgumentException("ID do produto é obrigatório para edição");
        }

        return produtoRepository.findById(produto.getProdutoId())
                .map(produtoExistente -> {
                    atualizarCamposProduto(produtoExistente, produto);
                    return produtoRepository.save(produtoExistente);
                })
                .orElseThrow(() -> {
                    log.error("Produto não encontrado para edição ID: {}", produto.getProdutoId());
                    return new RuntimeException("Produto não encontrado");
                });
    }

    // Métodos auxiliares privados
    private void alterarStatusProduto(Long produtoId, boolean ativo) {
        produtoRepository.findById(produtoId).ifPresentOrElse(
                produto -> {
                    produto.setAtivo(ativo);
                    produtoRepository.save(produto);
                    log.info("Produto ID: {} {} com sucesso", produtoId, ativo ? "reativado" : "inativado");
                },
                () -> {
                    log.warn("Tentativa de alterar status de produto não encontrado ID: {}", produtoId);
                    throw new RuntimeException("Produto não encontrado");
                }
        );
    }

    private void salvarImagemComTratamento(Produto produto, MultipartFile file, boolean imagemPrincipal) throws IOException {
        // 1. Desmarcar outras imagens como não principais se necessário
        if (imagemPrincipal) {
            imagemProdutoRepository.updateAllImagensNotPrincipal(produto.getProdutoId());
        }

        // 2. Criar diretório se não existir
        Path diretorioProduto = Paths.get(diretorioBase, "produtos", produto.getProdutoId().toString());
        Files.createDirectories(diretorioProduto);

        // 3. Gerar nome único para o arquivo
        String nomeArquivo = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path destino = diretorioProduto.resolve(nomeArquivo);

        // 4. Salvar arquivo fisicamente
        Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        // 5. Criar e salvar a entidade ImagemProduto
        ImagemProduto imagemProduto = new ImagemProduto();
        imagemProduto.setProduto(produto);
        imagemProduto.setImagem(file.getBytes()); // Armazena no banco também (opcional)
        imagemProduto.setImagemPrincipal(imagemPrincipal);
        imagemProduto.setTipoMime(file.getContentType());
        imagemProduto.setNomeArquivo(file.getOriginalFilename());
        imagemProduto.setTamanho(file.getSize());
        imagemProduto.setCaminhoArquivo(destino.toString()); // Armazena o caminho completo

        imagemProdutoRepository.save(imagemProduto);
        log.debug("Imagem salva para o produto ID: {}", produto.getProdutoId());
    }

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