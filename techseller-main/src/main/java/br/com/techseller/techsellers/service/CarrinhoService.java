package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.Carrinho;
import br.com.techseller.techsellers.entity.Produto;

import java.math.BigDecimal;

public interface CarrinhoService {
    Carrinho adicionarItem(Carrinho carrinho, Long produtoId, int quantidade);
    Carrinho atualizarQuantidade(Carrinho carrinho, Long produtoId, int quantidade);
    Carrinho removerItem(Carrinho carrinho, Long produtoId);
    Carrinho limparCarrinho(Carrinho carrinho);
    BigDecimal calcularTotal(Carrinho carrinho);
    int contarItens(Carrinho carrinho);
}
