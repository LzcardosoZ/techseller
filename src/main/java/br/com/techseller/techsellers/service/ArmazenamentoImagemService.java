package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.repository.ImagemProdutoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ArmazenamentoImagemService {
    private static final Logger log = LoggerFactory.getLogger(ArmazenamentoImagemService.class);

    @Value("${app.upload.dir}")
    private String diretorioBase;

    private final ImagemProdutoRepository imagemProdutoRepository;

    public ArmazenamentoImagemService(ImagemProdutoRepository imagemProdutoRepository) {
        this.imagemProdutoRepository = imagemProdutoRepository;
    }

    /**
     * Inicializa o diretório de upload quando o serviço é carregado
     */
    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(diretorioBase);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Diretório de upload inicializado em: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Falha ao criar diretório de upload", e);
        }
    }

    /**
     * Verifica a integridade entre os registros no banco e os arquivos físicos
     * @return Map com lista de arquivos faltantes e outros problemas encontrados
     */
    public Map<String, List<String>> verificarIntegridadeArmazenamento() {
        Map<String, List<String>> relatorio = new HashMap<>();
        relatorio.put("arquivos_faltantes", new ArrayList<>());
        relatorio.put("arquivos_sem_referencia", new ArrayList<>());

        // Verifica imagens registradas no banco
        List<ImagemProduto> imagens = imagemProdutoRepository.findAll();
        imagens.forEach(imagem -> {
            if (imagem.getCaminhoArquivo() != null) {
                Path caminhoAbsoluto = Paths.get(diretorioBase, imagem.getCaminhoArquivo());
                if (!Files.exists(caminhoAbsoluto)) {
                    String mensagem = String.format("Imagem ID %d - Caminho: %s",
                            imagem.getId(), caminhoAbsoluto);
                    relatorio.get("arquivos_faltantes").add(mensagem);
                    log.warn("Imagem registrada não encontrada: {}", caminhoAbsoluto);
                }
            }
        });

        return relatorio;
    }

    /**
     * Salva uma imagem no sistema de arquivos e retorna o caminho relativo
     */
    public String salvarImagem(MultipartFile arquivo, Long produtoId) throws IOException {
        validarArquivo(arquivo);

        String nomeUnico = gerarNomeUnico(arquivo.getOriginalFilename());
        Path destino = prepararDiretorio(produtoId, nomeUnico);

        arquivo.transferTo(destino);
        return produtoId + "/" + nomeUnico;
    }

    /**
     * Remove uma imagem do sistema de arquivos
     */
    public void removerImagem(String caminhoRelativo) throws IOException {
        if (caminhoRelativo == null || caminhoRelativo.isEmpty()) {
            throw new IOException("Caminho da imagem não pode ser vazio");
        }

        Path arquivoCompleto = Paths.get(diretorioBase, caminhoRelativo);
        Files.deleteIfExists(arquivoCompleto);
    }

    /**
     * Obtém o caminho absoluto de uma imagem
     */
    public Path obterCaminhoAbsoluto(String caminhoRelativo) {
        return Paths.get(diretorioBase, caminhoRelativo);
    }

    // --- Métodos privados auxiliares ---

    private void validarArquivo(MultipartFile arquivo) throws IOException {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IOException("Arquivo de imagem inválido");
        }

        if (arquivo.getContentType() == null || !arquivo.getContentType().startsWith("image/")) {
            throw new IOException("O arquivo deve ser uma imagem");
        }
    }

    private String gerarNomeUnico(String nomeOriginal) {
        String nomeSeguro = nomeOriginal.replace(" ", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "");
        return System.currentTimeMillis() + "_" + nomeSeguro;
    }

    private Path prepararDiretorio(Long produtoId, String nomeArquivo) throws IOException {
        Path diretorioProduto = Paths.get(diretorioBase, produtoId.toString());
        Files.createDirectories(diretorioProduto);
        return diretorioProduto.resolve(nomeArquivo);
    }
}