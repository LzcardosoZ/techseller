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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    @Transactional(readOnly = true)
    public List<Produto> listarProdutos(String filtro) {
        List<Produto> produtos;
        if (filtro != null && !filtro.isEmpty()) {
            produtos = produtoRepository.findByNomeOrId(filtro);
        } else {
            produtos = produtoRepository.findAllWithPrincipalImageOrderedDesc();
        }

        produtos.forEach(produto -> {
            List<ImagemProduto> imagens = imagemProdutoRepository.findByProdutoOrderByOrdem(produto.getProdutoId());
            produto.setImagens(imagens != null ? imagens : new ArrayList<>());
        });

        return produtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Produto> listarProdutosAtivos(String filtro) {
        List<Produto> produtos;
        if (filtro != null && !filtro.isEmpty()) {
            produtos = produtoRepository.findByNomeOrIdAndAtivo(filtro);
        } else {
            produtos = produtoRepository.findByAtivoTrue();
        }

        produtos.forEach(produto -> {
            List<ImagemProduto> imagens = imagemProdutoRepository.findByProdutoOrderByOrdem(produto.getProdutoId());
            produto.setImagens(imagens != null ? imagens : new ArrayList<>());
        });

        return produtos;
    }

    @Transactional
    @Override
    public Produto salvarProduto(Produto produto, MultipartFile[] imagens) throws IOException {
        Objects.requireNonNull(produto, "Produto não pode ser nulo");
        validarImagens(imagens);

        produto.setAvaliacao(Optional.ofNullable(produto.getAvaliacao()).orElse(BigDecimal.ZERO));
        produto.setAtivo(Optional.ofNullable(produto.getAtivo()).orElse(true));

        try {
            // Salva o produto primeiro para gerar ID
            Produto produtoSalvo = produtoRepository.save(produto);

            // Processa e associa as imagens
            List<ImagemProduto> imagensSalvas = processarEAssociarImagens(produtoSalvo, imagens);
            produtoSalvo.setImagens(imagensSalvas);

            log.info("Produto salvo com sucesso: ID {}, {} imagens",
                    produtoSalvo.getProdutoId(), imagensSalvas.size());
            return produtoRepository.save(produtoSalvo);

        } catch (DataIntegrityViolationException e) {
            log.error("Erro de integridade ao salvar produto: {}", e.getMessage());
            throw new IllegalArgumentException("Dados inválidos para cadastro do produto", e);
        }
    }

    private void validarImagens(MultipartFile[] imagens) {
        if (imagens == null || imagens.length == 0 || Arrays.stream(imagens).allMatch(MultipartFile::isEmpty)) {
            throw new IllegalArgumentException("O produto deve ter pelo menos uma imagem válida");
        }
    }

    private List<ImagemProduto> processarEAssociarImagens(Produto produto, MultipartFile[] imagens) throws IOException {
        List<ImagemProduto> imagensSalvas = new ArrayList<>();
        List<String> caminhosSalvos = new ArrayList<>();

        try {
            for (int i = 0; i < imagens.length; i++) {
                MultipartFile arquivo = imagens[i];
                if (arquivo == null || arquivo.isEmpty()) {
                    continue;
                }

                boolean isPrincipal = (i == 0 && imagensSalvas.isEmpty());
                String caminho = armazenamentoImagemService.salvarImagemNoDisco(produto.getProdutoId(), arquivo);
                caminhosSalvos.add(caminho);

                ImagemProduto imagem = armazenamentoImagemService.criarEntidadeImagem(
                        produto,
                        arquivo,
                        caminho,
                        imagensSalvas.size() + 1,
                        isPrincipal
                );

                ImagemProduto imagemSalva = imagemProdutoRepository.save(imagem);
                imagensSalvas.add(imagemSalva);
            }

            return imagensSalvas;

        } catch (IOException e) {
            log.error("Falha ao processar imagens para produto ID {}: {}", produto.getProdutoId(), e.getMessage());

            if (!caminhosSalvos.isEmpty()) {
                try {
                    armazenamentoImagemService.removerImagensDoDisco(caminhosSalvos);
                    log.warn("Removidas {} imagens físicas durante rollback", caminhosSalvos.size());
                } catch (IOException ex) {
                    log.error("Falha ao limpar imagens durante rollback: {}", ex.getMessage());
                    e.addSuppressed(ex);
                }
            }

            throw e;
        }
    }

    @Transactional
    @Override
    public void editarProduto(Produto produto, MultipartFile[] novasImagens, List<Long> imagensRemovidas) {
        Produto produtoExistente = produtoRepository.findByIdWithImagens(produto.getProdutoId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        produtoExistente.setNome(produto.getNome());
        produtoExistente.setDescricaoDetalhada(produto.getDescricaoDetalhada());
        produtoExistente.setPreco(produto.getPreco());
        produtoExistente.setQuantidadeEstoque(produto.getQuantidadeEstoque());
        produtoExistente.setAtivo(produto.getAtivo());
        produtoExistente.setAvaliacao(produto.getAvaliacao());

        if (imagensRemovidas != null && !imagensRemovidas.isEmpty()) {
            List<ImagemProduto> paraRemover = produtoExistente.getImagens().stream()
                    .filter(img -> imagensRemovidas.contains(img.getId()))
                    .collect(Collectors.toList());

            try {
                List<String> caminhosParaRemover = paraRemover.stream()
                        .map(ImagemProduto::getCaminhoArquivo)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                armazenamentoImagemService.removerImagensDoDisco(caminhosParaRemover);
                imagemProdutoRepository.deleteAll(paraRemover);
                produtoExistente.getImagens().removeAll(paraRemover);
                reorganizarOrdemImagens(produtoExistente);
            } catch (IOException e) {
                log.error("Erro ao remover imagens: {}", e.getMessage());
                throw new RuntimeException("Erro ao remover imagens", e);
            }
        }

        if (novasImagens != null && novasImagens.length > 0) {
            try {
                List<MultipartFile> imagensValidas = Arrays.stream(novasImagens)
                        .filter(arquivo -> arquivo != null && !arquivo.isEmpty())
                        .collect(Collectors.toList());

                if (!imagensValidas.isEmpty()) {
                    List<ImagemProduto> novasImagensSalvas = new ArrayList<>();
                    int ultimaOrdem = produtoExistente.getImagens().stream()
                            .mapToInt(ImagemProduto::getOrdem)
                            .max().orElse(0);

                    for (int i = 0; i < imagensValidas.size(); i++) {
                        MultipartFile arquivo = imagensValidas.get(i);
                        boolean isPrincipal = (i == 0 && produtoExistente.getImagens().isEmpty());

                        String caminho = armazenamentoImagemService.salvarImagemNoDisco(
                                produtoExistente.getProdutoId(),
                                arquivo
                        );

                        ImagemProduto imagem = armazenamentoImagemService.criarEntidadeImagem(
                                produtoExistente,
                                arquivo,
                                caminho,
                                ultimaOrdem + i + 1,
                                isPrincipal
                        );

                        novasImagensSalvas.add(imagem);
                    }

                    List<ImagemProduto> imagensPersistidas = imagemProdutoRepository.saveAll(novasImagensSalvas);
                    produtoExistente.getImagens().addAll(imagensPersistidas);
                }
            } catch (IOException e) {
                log.error("Erro ao salvar novas imagens: {}", e.getMessage());
                throw new RuntimeException("Erro ao salvar novas imagens", e);
            }
        }

        atualizarImagemPrincipal(produtoExistente);
        produtoRepository.save(produtoExistente);
        log.info("Produto atualizado: ID {} com {} imagens",
                produtoExistente.getProdutoId(), produtoExistente.getImagens().size());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Produto> buscarPorId(Long produtoId) {
        try {
            if (produtoId == null || produtoId <= 0) {
                throw new IllegalArgumentException("ID do produto inválido");
            }

            Optional<Produto> produtoOpt = produtoRepository.findByIdWithImagens(produtoId);
            produtoOpt.ifPresent(produto -> {
                if (produto.getImagens() == null) {
                    produto.setImagens(new ArrayList<>());
                } else {
                    produto.setImagens(produto.getImagens().stream()
                            .sorted(Comparator.comparing(ImagemProduto::getOrdem))
                            .collect(Collectors.toList()));
                }
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

    @Transactional(readOnly = true)
    @Override
    public Optional<Produto> findByIdWithImagens(Long produtoId) {
        try {
            if (produtoId == null || produtoId <= 0) {
                throw new IllegalArgumentException("ID do produto inválido");
            }

            Optional<Produto> produtoOpt = produtoRepository.findComImagensOrdenadas(produtoId);

            produtoOpt.ifPresent(produto -> {
                if (produto.getImagens() == null) {
                    produto.setImagens(new ArrayList<>());
                } else {
                    produto.setImagens(produto.getImagens().stream()
                            .sorted(Comparator.comparing(ImagemProduto::getOrdem))
                            .collect(Collectors.toList()));
                }
            });

            return produtoOpt;

        } catch (Exception e) {
            log.error("Erro ao buscar produto por ID: {}", produtoId, e);
            throw new RuntimeException("Erro ao buscar produto", e);
        }
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

    @Transactional
    public void definirImagemComoPrincipal(Long imagemId, Long produtoId) {
        // 1. Valida se a imagem existe e pertence ao produto
        ImagemProduto imagem = imagemProdutoRepository.findById(imagemId)
                .orElseThrow(() -> new RuntimeException("Imagem não encontrada"));

        if (!imagem.getProduto().getProdutoId().equals(produtoId)) {
            throw new RuntimeException("A imagem não pertence ao produto informado");
        }

        // 2. Remove status de principal de todas as imagens do produto
        imagemProdutoRepository.updateAllImagensNotPrincipal(produtoId);

        // 3. Define a nova imagem como principal
        imagem.setImagemPrincipal(true);
        imagemProdutoRepository.save(imagem);

        // 4. Reordena as imagens (se necessário)
        reordenarImagens(produtoId,
                reordenarComPrincipalPrimeiro(imagem.getProduto().getImagens(), imagemId));

        log.info("Imagem ID {} definida como principal para o produto ID {}", imagemId, produtoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImagemProduto> listarImagensPorProduto(Long produtoId) {
        try {
            return imagemProdutoRepository.findByProdutoOrderByOrdem(produtoId);
        } catch (Exception e) {
            log.error("Erro ao listar imagens: Produto ID {}", produtoId, e);
            throw new RuntimeException("Erro ao carregar imagens");
        }
    }

    @Override
    @Transactional
    public void removerImagem(Long imagemId) {
        try {
            ImagemProduto imagem = imagemProdutoRepository.findById(imagemId)
                    .orElseThrow(() -> new RuntimeException("Imagem não encontrada"));

            Long produtoId = imagem.getProduto().getProdutoId();
            armazenamentoImagemService.removerImagensDoDisco(Collections.singletonList(imagem.getCaminhoArquivo()));
            imagemProdutoRepository.delete(imagem);

            Produto produto = produtoRepository.findByIdWithImagens(produtoId)
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
            reorganizarOrdemImagens(produto);

            log.info("Imagem ID {} removida com sucesso", imagemId);
        } catch (IOException e) {
            log.error("Erro ao remover imagem: {}", e.getMessage());
            throw new RuntimeException("Erro ao remover imagem do sistema de arquivos");
        } catch (Exception e) {
            log.error("Erro ao remover imagem: {}", e.getMessage());
            throw new RuntimeException("Erro ao remover imagem");
        }
    }

    @Transactional
    @Override
    public void reordenarImagens(Long produtoId, List<Long> novaOrdemIds) {
        Objects.requireNonNull(produtoId, "ID do produto não pode ser nulo");
        Objects.requireNonNull(novaOrdemIds, "Lista de ordenação não pode ser nula");

        Produto produto = produtoRepository.findByIdWithImagens(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        validarOrdemImagens(produto, novaOrdemIds);
        atualizarOrdemImagens(produto, novaOrdemIds);

        produtoRepository.save(produto);
        log.info("Imagens do produto {} reordenadas. Nova ordem: {}", produtoId, novaOrdemIds);
    }

    // ============== MÉTODOS AUXILIARES PRIVADOS ==============

    private void atualizarImagemPrincipal(Produto produto) {
        if (produto.getImagens().isEmpty()) {
            return;
        }

        boolean hasPrincipal = produto.getImagens().stream()
                .anyMatch(ImagemProduto::getImagemPrincipal);

        if (!hasPrincipal) {
            produto.getImagens().get(0).setImagemPrincipal(true);
            imagemProdutoRepository.save(produto.getImagens().get(0));
        }
    }

    private void reorganizarOrdemImagens(Produto produto) {
        List<ImagemProduto> imagensOrdenadas = produto.getImagens().stream()
                .sorted(Comparator.comparing(ImagemProduto::getOrdem))
                .collect(Collectors.toList());

        for (int i = 0; i < imagensOrdenadas.size(); i++) {
            imagensOrdenadas.get(i).setOrdem(i + 1);
        }

        produtoRepository.save(produto);
    }

    private List<Long> reordenarComPrincipalPrimeiro(List<ImagemProduto> imagens, Long imagemPrincipalId) {
        List<Long> novaOrdem = new ArrayList<>();
        novaOrdem.add(imagemPrincipalId);

        imagens.stream()
                .filter(img -> !img.getId().equals(imagemPrincipalId))
                .sorted(Comparator.comparing(ImagemProduto::getOrdem))
                .map(ImagemProduto::getId)
                .forEach(novaOrdem::add);

        return novaOrdem;
    }

    private void validarOrdemImagens(Produto produto, List<Long> novaOrdemIds) {
        if (produto.getImagens().isEmpty()) {
            throw new IllegalArgumentException("Produto não possui imagens para reordenar");
        }

        Set<Long> idsExistentes = produto.getImagens().stream()
                .map(ImagemProduto::getId)
                .collect(Collectors.toSet());

        if (novaOrdemIds.size() != new HashSet<>(novaOrdemIds).size()) {
            throw new IllegalArgumentException("Lista de ordenação contém IDs duplicados");
        }

        for (Long id : novaOrdemIds) {
            if (!idsExistentes.contains(id)) {
                throw new IllegalArgumentException("Imagem ID " + id + " não pertence ao produto");
            }
        }

        if (idsExistentes.size() != novaOrdemIds.size()) {
            throw new IllegalArgumentException("A nova ordem deve incluir exatamente " + idsExistentes.size() + " imagens");
        }
    }

    private void atualizarOrdemImagens(Produto produto, List<Long> novaOrdemIds) {
        Map<Long, ImagemProduto> imagensMap = produto.getImagens().stream()
                .collect(Collectors.toMap(ImagemProduto::getId, Function.identity()));

        for (int i = 0; i < novaOrdemIds.size(); i++) {
            ImagemProduto imagem = imagensMap.get(novaOrdemIds.get(i));
            imagem.setOrdem(i + 1);
            log.debug("Atualizando ordem - Imagem ID: {}, Nova ordem: {}", imagem.getId(), imagem.getOrdem());
        }

        if (!produto.getImagens().isEmpty() &&
                !produto.getImagens().get(0).getImagemPrincipal()) {
            produto.getImagens().get(0).setImagemPrincipal(true);
        }
    }

    // Métodos não utilizados (mantidos para compatibilidade)
    @Override
    public void salvarProduto(Produto produto, MultipartFile imagem, boolean imagemPrincipal) {
        throw new UnsupportedOperationException("Método não suportado. Use salvarProduto(Produto, MultipartFile[])");
    }

    @Override
    public void editarProduto(Produto produto, MultipartFile imagem, boolean imagemPrincipal) {
        throw new UnsupportedOperationException("Método não suportado. Use editarProduto(Produto, MultipartFile[], List<Long>)");
    }

    @Override
    public void salvarImagem(Long produtoId, MultipartFile file, boolean imagemPrincipal) {
        throw new UnsupportedOperationException("Método não suportado");
    }

    @Override
    public void definirImagemComoPrincipal(Long imagemId) {
        
    }

    @Override
    public Produto editarProduto(Produto produto) {
        throw new UnsupportedOperationException("Método não suportado. Use editarProduto(Produto, MultipartFile[], List<Long>)");
    }
}