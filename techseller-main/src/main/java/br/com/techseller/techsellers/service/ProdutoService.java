package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.entity.Produto;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ProdutoService {

    /**
     * Lista produtos com filtro opcional
     * @param filtro Texto para busca por nome ou ID
     * @return Lista de produtos
     */
    List<Produto> listarProdutos(String filtro);

    @Transactional
    Produto salvarProduto(Produto produto, MultipartFile[] imagens) throws IOException;

    @Transactional
    void editarProduto(Produto produto, MultipartFile[] novasImagens, List<Long> imagensRemovidas);

    /**
     * Busca um produto por ID
     * @param id Identificador do produto
     * @return Optional contendo o produto se encontrado
     */
    Optional<Produto> buscarPorId(Long id);

    /**
     * Salva um novo produto com imagem obrigatória
     * @param produto Produto a ser criado
     * @param imagem Arquivo de imagem
     * @param imagemPrincipal Indica se é a imagem principal
     */
    void salvarProduto(Produto produto, MultipartFile imagem, boolean imagemPrincipal);

    /**
     * Edita um produto existente
     * @param produto Produto com dados atualizados
     * @param imagem Nova imagem (opcional)
     * @param imagemPrincipal Indica se é a imagem principal
     */
    void editarProduto(Produto produto, MultipartFile imagem, boolean imagemPrincipal);

    /**
     * Busca uma imagem por ID
     * @param imagemId ID da imagem
     * @return Imagem encontrada
     */
    ImagemProduto buscarImagemPorId(Long imagemId);

    @Transactional(readOnly = true)
    Optional<Produto> findByIdWithImagens(Long produtoId);

    /**
     * Inativa um produto
     * @param produto_id ID do produto
     */
    void inativarProduto(Long produto_id);

    /**
     * Reativa um produto
     * @param produto_id ID do produto
     */
    void reativarProduto(Long produto_id);

    /**
     * Adiciona uma imagem a um produto existente
     * @param produtoId ID do produto
     * @param file Arquivo de imagem
     * @param imagemPrincipal Indica se é a imagem principal
     */
    void salvarImagem(Long produtoId, MultipartFile file, boolean imagemPrincipal);

    @Transactional
    void definirImagemComoPrincipal(Long imagemId);

    /**
     * Lista todas as imagens de um produto
     * @param produtoId ID do produto
     * @return Lista de imagens
     */
    List<ImagemProduto> listarImagensPorProduto(Long produtoId);

    /**
     * Edição básica de produto (método obsoleto - manter para compatibilidade)
     * @deprecated Usar editarProduto(Produto, MultipartFile, boolean)
     */
    @Deprecated
    Produto editarProduto(Produto produto);

    @Transactional
    void removerImagem(Long imagemId);

    @Transactional
    void reordenarImagens(Long produtoId, List<Long> novaOrdemIds);

    List<Produto> listarProdutosAtivos(String filtro);
}