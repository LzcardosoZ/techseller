package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.entity.Produto;
import br.com.techseller.techsellers.repository.ImagemProdutoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class ArmazenamentoImagemService {
    private static final Logger log = LoggerFactory.getLogger(ArmazenamentoImagemService.class);

    @Value("${app.upload.dir}")
    private String diretorioBase;

    @Value("${app.imagem.max-size:5242880}") // 5MB padrão
    private long maxFileSize;

    @Value("${app.imagem.max-quantity:10}")
    private int maxImagesPerProduct;

    private final ImagemProdutoRepository imagemProdutoRepository;

    public ArmazenamentoImagemService(ImagemProdutoRepository imagemProdutoRepository) {
        this.imagemProdutoRepository = imagemProdutoRepository;
    }

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(diretorioBase).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Diretório de upload criado: {}", uploadPath);
            }
        } catch (IOException e) {
            log.error("Falha ao criar diretório de upload", e);
            throw new RuntimeException("Não foi possível inicializar o diretório de upload", e);
        }
    }

    // Metodo simplificado para salvar uma única imagem
    public String salvarImagemNoDisco(Long produtoId, MultipartFile arquivo) throws IOException {
        validarArquivo(arquivo);
        String nomeUnico = gerarNomeUnico(arquivo.getOriginalFilename());
        Path destino = prepararDestino(produtoId, nomeUnico);
        Files.createDirectories(destino.getParent());
        arquivo.transferTo(destino);
        return construirCaminhoRelativo(produtoId, nomeUnico);
    }

    public ImagemProduto criarEntidadeImagem(Produto produto, MultipartFile arquivo,
                                              String caminho, int ordem, boolean isPrincipal) {
        return ImagemProduto.builder()
                .nomeArquivo(arquivo.getOriginalFilename())
                .caminhoArquivo(caminho)
                .tamanho(arquivo.getSize())
                .tipoMime(arquivo.getContentType())
                .imagemPrincipal(isPrincipal)
                .ordem(ordem)
                .produto(produto)
                .build();
    }

    public void removerImagensDoDisco(List<String> caminhos) throws IOException {
        for (String caminho : caminhos) {
            try {
                Path caminhoAbsoluto = obterCaminhoAbsoluto(caminho);
                Files.deleteIfExists(caminhoAbsoluto);
            } catch (IOException e) {
                log.error("Falha ao remover imagem: {}", caminho, e);
                throw e;
            }
        }
    }

    public void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode ser nulo ou vazio");
        }
        if (arquivo.getOriginalFilename() == null || arquivo.getOriginalFilename().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do arquivo inválido");
        }
        if (arquivo.getContentType() == null || !arquivo.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Apenas imagens são permitidas");
        }
        if (arquivo.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("Tamanho máximo excedido (%dMB)", maxFileSize / (1024 * 1024)));
        }
    }

    public String gerarNomeUnico(String nomeOriginal) {
        String extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
        return UUID.randomUUID() + extensao;
    }

    public Path prepararDestino(Long produtoId, String nomeArquivo) {
        return Paths.get(diretorioBase)
                .resolve(produtoId.toString())
                .resolve(nomeArquivo)
                .normalize()
                .toAbsolutePath();
    }

    public String construirCaminhoRelativo(Long produtoId, String nomeArquivo) {
        return produtoId + "/" + nomeArquivo;
    }

    public Path obterCaminhoAbsoluto(String caminhoRelativo) {
        return Paths.get(diretorioBase, caminhoRelativo).normalize().toAbsolutePath();
    }
}