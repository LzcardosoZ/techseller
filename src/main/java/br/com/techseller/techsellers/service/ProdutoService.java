package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.entity.Produto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ProdutoService {
    /**
     * Lista todos os produtos, com possibilidade de filtro
     * @param filtro Texto para filtrar produtos por nome ou ID
     * @return Lista de produtos
     */
    List<Produto> listarProdutos(String filtro);

    /**
     * Busca um produto por seu ID
     * @param id Identificador do produto
     * @return Optional contendo o produto, se encontrado
     */
    Optional<Produto> buscarPorId(Long id);

    /**
     * Salva um novo produto ou atualiza um existente
     * @param produto Produto a ser salvo
     * @param imagem Arquivo de imagem do produto
     * @param imagemPrincipal Indica se é a imagem principal
     */
    void salvarProduto(Produto produto, MultipartFile imagem, boolean imagemPrincipal);

    /**
     * Busca uma imagem de produto por seu ID
     * @param imagemId Identificador da imagem
     * @return Imagem do produto
     */
    ImagemProduto buscarImagemPorId(Long imagemId);

    /**
     * Inativa um produto
     * @param produto_id ID do produto a ser inativado
     */
    void inativarProduto(Long produto_id);

    /**
     * Reativa um produto previamente inativado
     * @param produto_id ID do produto a ser reativado
     */
    void reativarProduto(Long produto_id);

    /**
     * Salva uma imagem adicional para um produto
     * @param produtoId ID do produto
     * @param file Arquivo de imagem
     * @param imagemPrincipal Indica se é a imagem principal
     */
    void salvarImagem(Long produtoId, MultipartFile file, boolean imagemPrincipal);

    /**
     * Lista todas as imagens de um produto
     * @param produtoId ID do produto
     * @return Lista de imagens do produto
     */
    List<ImagemProduto> listarImagensPorProduto(Long produtoId);

    /**
     * Edita um produto existente
     * @param produto Produto a ser editado
     * @return Produto atualizado
     */
    Produto editarProduto(Produto produto);
}