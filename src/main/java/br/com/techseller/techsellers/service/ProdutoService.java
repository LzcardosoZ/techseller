package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.entity.Produto;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ProdutoService {

    List<Produto> listarProdutos(String filtro);

    @Transactional
    Produto salvarProduto(Produto produto, MultipartFile[] imagens) throws IOException;

    @Transactional
    void editarProduto(Produto produto, MultipartFile[] novasImagens, List<Long> imagensRemovidas);

    Optional<Produto> buscarPorId(Long id);

    void salvarProduto(Produto produto, MultipartFile imagem, boolean imagemPrincipal);

    Produto salvarProduto(Produto produto, MultipartFile[] imagens, boolean ignorarValidacaoImagens) throws IOException;

    void editarProduto(Produto produto, MultipartFile imagem, boolean imagemPrincipal);

    ImagemProduto buscarImagemPorId(Long imagemId);

    @Transactional(readOnly = true)
    Optional<Produto> findByIdWithImagens(Long produtoId);

    void inativarProduto(Long produto_id);


    void reativarProduto(Long produto_id);

    void salvarImagem(Long produtoId, MultipartFile file, boolean imagemPrincipal);

    @Transactional
    void definirImagemComoPrincipal(Long imagemId);

    List<ImagemProduto> listarImagensPorProduto(Long produtoId);

    @Deprecated
    Produto editarProduto(Produto produto);

    @Transactional
    void removerImagem(Long imagemId);

    @Transactional
    void reordenarImagens(Long produtoId, List<Long> novaOrdemIds);

    List<Produto> listarProdutosAtivos(String filtro);
}