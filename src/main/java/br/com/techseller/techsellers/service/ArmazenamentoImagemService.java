package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.entity.Produto;
import br.com.techseller.techsellers.repository.ImagemProdutoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

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

    // Método principal para salvar múltiplas imagens
    public List<ImagemProduto> salvarImagens(Produto produto, MultipartFile[] arquivos) throws IOException {
        validarEntrada(produto, arquivos);
        List<ImagemProduto> imagensSalvas = new ArrayList<>();

        for (int i = 0; i < arquivos.length; i++) {
            try {
                MultipartFile arquivo = arquivos[i];
                if (arquivo == null || arquivo.isEmpty()) continue;

                boolean isPrincipal = (i == 0 && produto.getImagens().isEmpty());
                String caminho = salvarImagemNoDisco(produto.getProdutoId(), arquivo);

                ImagemProduto imagem = criarEntidadeImagem(produto, arquivo, caminho,
                        produto.getImagens().size() + 1, isPrincipal);

                imagensSalvas.add(imagem);
                log.debug("Imagem salva: {} ({} bytes) para produto ID {}",
                        caminho, arquivo.getSize(), produto.getProdutoId());
            } catch (IOException e) {
                handleImageSaveError(imagensSalvas, i, e);
            }
        }

        return imagemProdutoRepository.saveAll(imagensSalvas);
    }

    // Método simplificado para salvar uma única imagem
    public String salvarImagemNoDisco(Long produtoId, MultipartFile arquivo) throws IOException {
        validarArquivo(arquivo);
        String nomeUnico = gerarNomeUnico(arquivo.getOriginalFilename());
        Path destino = prepararDestino(produtoId, nomeUnico);
        Files.createDirectories(destino.getParent());
        arquivo.transferTo(destino);
        return construirCaminhoRelativo(produtoId, nomeUnico);
    }

    // Método para exibir imagem (seu método existente)
    public ResponseEntity<Resource> exibirImagem(Long imagemId) {
        try {
            ImagemProduto imagem = imagemProdutoRepository.findById(imagemId)
                    .orElseThrow(() -> new RuntimeException("Imagem não encontrada"));

            Path caminhoAbsoluto = obterCaminhoAbsoluto(imagem.getCaminhoArquivo());
            Resource resource = new UrlResource(caminhoAbsoluto.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(imagem.getTipoMime()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + imagem.getNomeArquivo() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Erro ao carregar imagem ID: {}", imagemId, e);
            return ResponseEntity.notFound().build();
        }
    }

    // Métodos auxiliares privados
    public void validarEntrada(Produto produto, MultipartFile[] arquivos) {
        if (arquivos == null || arquivos.length == 0) {
            throw new IllegalArgumentException("Nenhuma imagem fornecida");
        }
        if (produto.getProdutoId() == null) {
            throw new IllegalStateException("Produto deve ser salvo antes de adicionar imagens");
        }
        if (produto.getImagens().size() + arquivos.length > maxImagesPerProduct) {
            throw new IllegalArgumentException(
                    String.format("Limite de %d imagens por produto excedido", maxImagesPerProduct));
        }
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

    public void handleImageSaveError(List<ImagemProduto> imagensSalvas, int index, IOException e) throws IOException {
        log.error("Falha ao salvar imagem {}: {}", index, e.getMessage());
        if (!imagensSalvas.isEmpty()) {
            try {
                removerImagensDoDisco(imagensSalvas.stream()
                        .map(ImagemProduto::getCaminhoArquivo)
                        .collect(Collectors.toList()));
            } catch (IOException ex) {
                log.error("Falha ao limpar imagens após erro", ex);
            }
        }
        throw new IOException("Falha ao salvar imagem " + index + ": " + e.getMessage(), e);
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