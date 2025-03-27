package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.entity.Produto;
import br.com.techseller.techsellers.repository.ImagemProdutoRepository;
import br.com.techseller.techsellers.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProdutoServiceImpl implements ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ImagemProdutoRepository imagemProdutoRepository;

    @Value("${app.upload.dir}")
    private String diretorioBase;

    @Override
    @Transactional(readOnly = true)
    public List<Produto> listarProdutos(String filtro) {
        List<Produto> produtos = (filtro != null && !filtro.isEmpty())
                ? produtoRepository.findByNomeContainingIgnoreCaseOrderByProdutoIdDesc(filtro)
                : produtoRepository.findAllByOrderByProdutoIdDesc();

        produtos.forEach(produto -> {
            produto.setImagens(
                    Optional.ofNullable(imagemProdutoRepository.findByProdutoProdutoId(produto.getProdutoId()))
                            .orElseGet(ArrayList::new)
            );
        });

        return produtos;
    }

    @Override
    @Transactional
    public void salvarProduto(Produto produto, MultipartFile imagem, boolean imagemPrincipal) {
        validarProdutoEImagem(produto, imagem);

        try {
            produto = produtoRepository.save(definirValoresPadrao(produto));
            adicionarImagemAoProduto(produto, imagem, imagemPrincipal);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar imagem: " + e.getMessage(), e);
        }
    }

    private void validarProdutoEImagem(Produto produto, MultipartFile imagem) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo");
        }
        if (imagem == null || imagem.isEmpty()) {
            throw new IllegalArgumentException("O produto deve ter pelo menos uma imagem");
        }
    }

    private Produto definirValoresPadrao(Produto produto) {
        if (produto.getAvaliacao() == null) {
            produto.setAvaliacao(new BigDecimal("0.0"));
        }
        if (produto.getAtivo() == null) {
            produto.setAtivo(true);
        }
        return produto;
    }

    private void adicionarImagemAoProduto(Produto produto, MultipartFile imagem, boolean imagemPrincipal) throws IOException {
        String nomeArquivo = gerarNomeArquivoUnico(imagem.getOriginalFilename());
        String caminhoRelativo = salvarImagemNoDisco(imagem, produto.getProdutoId(), nomeArquivo);

        ImagemProduto imagemProduto = ImagemProduto.builder()
                .nomeArquivo(imagem.getOriginalFilename())
                .caminhoArquivo(caminhoRelativo)
                .tamanho(imagem.getSize())
                .tipoMime(imagem.getContentType())
                .imagemPrincipal(imagemPrincipal)
                .build();

        produto.adicionarImagem(imagemProduto);
        imagemProdutoRepository.save(imagemProduto);
    }

    private String gerarNomeArquivoUnico(String nomeOriginal) {
        return UUID.randomUUID() + "_" + nomeOriginal;
    }

    private String salvarImagemNoDisco(MultipartFile imagem, Long produtoId, String nomeArquivo) throws IOException {
        Path diretorioProduto = Paths.get(diretorioBase, produtoId.toString());
        Files.createDirectories(diretorioProduto);

        Path destino = diretorioProduto.resolve(nomeArquivo);
        imagem.transferTo(destino);

        return produtoId + "/" + nomeArquivo;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Produto> buscarPorId(Long produtoId) {
        return produtoRepository.findById(produtoId);
    }

    @Override
    @Transactional(readOnly = true)
    public ImagemProduto buscarImagemPorId(Long imagemId) {
        return imagemProdutoRepository.findById(imagemId)
                .orElseThrow(() -> new RuntimeException("Imagem não encontrada"));
    }

    @Override
    @Transactional
    public void inativarProduto(Long produtoId) {
        produtoRepository.findById(produtoId).ifPresent(produto -> {
            produto.setAtivo(false);
            produtoRepository.save(produto);
        });
    }

    @Override
    @Transactional
    public void reativarProduto(Long produtoId) {
        produtoRepository.findById(produtoId).ifPresent(produto -> {
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

            String nomeArquivo = gerarNomeArquivoUnico(file.getOriginalFilename());
            String caminhoRelativo = salvarImagemNoDisco(file, produtoId, nomeArquivo);

            ImagemProduto imagemProduto = new ImagemProduto();
            imagemProduto.setProduto(produto);
            imagemProduto.setNomeArquivo(file.getOriginalFilename());
            imagemProduto.setCaminhoArquivo(caminhoRelativo);
            imagemProduto.setTamanho(file.getSize());
            imagemProduto.setTipoMime(file.getContentType());
            imagemProduto.setImagemPrincipal(imagemPrincipal);

            imagemProdutoRepository.save(imagemProduto);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar imagem: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImagemProduto> listarImagensPorProduto(Long produtoId) {
        return imagemProdutoRepository.findByProdutoProdutoId(produtoId);
    }
}