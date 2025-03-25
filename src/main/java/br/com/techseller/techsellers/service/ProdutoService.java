package br.com.techseller.techsellers.service;


import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.entity.Produto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ProdutoService {
    List<Produto> listarProdutos(String filtro);
    Optional<Produto> buscarPorId(Long id);
    void salvarProduto(Produto produto, MultipartFile imagem, boolean imagemPrincipal);
    void inativarProduto(Long produto_id);
    void reativarProduto(Long produto_id);
    void salvarImagem(Long produtoId, MultipartFile file, boolean imagemPrincipal);

    List<ImagemProduto> listarImagensPorProduto(Long produtoId);
}
