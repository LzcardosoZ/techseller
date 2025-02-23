package com.pi.mytechseller.projeto.servicos;

import com.pi.mytechseller.projeto.modelos.*;
import com.pi.mytechseller.projeto.repositorios.CarrinhoRepositorio;
import com.pi.mytechseller.projeto.repositorios.ItemCarrinhoRepositorio;
import com.pi.mytechseller.projeto.repositorios.ProdutoRepositorio;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CarrinhoServico {

    private final CarrinhoRepositorio carrinhoRepositorio;
    private final ItemCarrinhoRepositorio itemCarrinhoRepositorio;
    private final ProdutoRepositorio produtoRepositorio;

    public CarrinhoServico(CarrinhoRepositorio carrinhoRepositorio, ItemCarrinhoRepositorio itemCarrinhoRepositorio, ProdutoRepositorio produtoRepositorio) {
        this.carrinhoRepositorio = carrinhoRepositorio;
        this.itemCarrinhoRepositorio = itemCarrinhoRepositorio;
        this.produtoRepositorio = produtoRepositorio;
    }

    public Carrinho obterCarrinhoAtivo(Usuario usuario) {
        return carrinhoRepositorio.findByUsuarioAndStatus(usuario, StatusCarrinho.ABERTO)
                .orElseGet(() -> {
                    Carrinho novoCarrinho = new Carrinho();
                    novoCarrinho.setUsuario(usuario);
                    return carrinhoRepositorio.save(novoCarrinho);
                });
    }

    public ItemCarrinho adicionarProdutoAoCarrinho(Usuario usuario, Long produtoId, Integer quantidade) {
        Carrinho carrinho = obterCarrinhoAtivo(usuario);
        Produto produto = produtoRepositorio.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        BigDecimal precoTotal = produto.getPreco().multiply(BigDecimal.valueOf(quantidade));

        ItemCarrinho item = new ItemCarrinho();
        item.setCarrinho(carrinho);
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        item.setPrecoTotal(precoTotal);

        return itemCarrinhoRepositorio.save(item);
    }

    public void finalizarCarrinho(Usuario usuario) {
        Carrinho carrinho = obterCarrinhoAtivo(usuario);
        carrinho.setStatus(StatusCarrinho.FECHADO);
        carrinhoRepositorio.save(carrinho);
    }
}
