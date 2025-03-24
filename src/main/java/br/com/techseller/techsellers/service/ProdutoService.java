package br.com.techseller.techsellers.service;


import br.com.techseller.techsellers.entity.Produto;

import java.util.List;
import java.util.Optional;

public interface ProdutoService {
    List<Produto> listarProdutos(String filtro);
    Optional<Produto> buscarPorId(Long id);
    void salvarProduto(Produto produto);
    void inativarProduto(Long produto_id);
    void reativarProduto(Long produto_id);
    void salvarUrlImagem(Long produtoId, String urlImagem);
}
