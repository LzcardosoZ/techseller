package br.com.techseller.techsellers.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class Carrinho implements Serializable {
    private List<ItemCarrinho> itens = new ArrayList<>();

    public void adicionarItem(Produto produto, int quantidade) {
        Optional<ItemCarrinho> itemExistente = itens.stream()
                .filter(item -> item.getProduto().getProdutoId().equals(produto.getProdutoId()))
                .findFirst();

        if (itemExistente.isPresent()) {
            ItemCarrinho item = itemExistente.get();
            item.setQuantidade(item.getQuantidade() + quantidade);
        } else {
            ItemCarrinho novoItem = new ItemCarrinho(produto, quantidade);
            itens.add(novoItem);
        }
    }

    public void removerItem(Long produtoId) {
        itens.removeIf(item -> item.getProduto().getProdutoId().equals(produtoId));
    }

    public void atualizarQuantidade(Long produtoId, int quantidade) {
        itens.stream()
                .filter(item -> item.getProduto().getProdutoId().equals(produtoId))
                .findFirst()
                .ifPresent(item -> item.setQuantidade(quantidade));
    }

    public BigDecimal getTotal() {
        return itens.stream()
                .map(ItemCarrinho::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItens() {
        return itens.stream()
                .mapToInt(ItemCarrinho::getQuantidade)
                .sum();
    }

    public boolean isVazio() {
        return itens.isEmpty();
    }

    public List<ItemCarrinho> getItens() {
        return new ArrayList<>(itens);
    }

    public void limpar() {
        itens.clear();
    }

    private BigDecimal valorFrete = BigDecimal.ZERO;


    public BigDecimal getValorFrete() {
        return valorFrete;
    }


    public void setValorFrete(BigDecimal valorFrete) {
        this.valorFrete = valorFrete;
    }

    public BigDecimal getTotalComFrete() {
        return getTotal().add(valorFrete != null ? valorFrete : BigDecimal.ZERO);
    }
}