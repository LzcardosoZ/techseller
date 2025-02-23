package com.pi.mytechseller.projeto.servicos;

import com.pi.mytechseller.projeto.modelos.*;
import com.pi.mytechseller.projeto.repositorios.PedidoRepositorio;
import com.pi.mytechseller.projeto.repositorios.ItemPedidoRepositorio;
import com.pi.mytechseller.projeto.repositorios.CarrinhoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PedidoServico {

    private final PedidoRepositorio pedidoRepositorio;
    private final ItemPedidoRepositorio itemPedidoRepositorio;
    private final CarrinhoRepositorio carrinhoRepositorio;

    public PedidoServico(PedidoRepositorio pedidoRepositorio, ItemPedidoRepositorio itemPedidoRepositorio, CarrinhoRepositorio carrinhoRepositorio) {
        this.pedidoRepositorio = pedidoRepositorio;
        this.itemPedidoRepositorio = itemPedidoRepositorio;
        this.carrinhoRepositorio = carrinhoRepositorio;
    }

    @Transactional
    public Pedido finalizarPedido(Long carrinhoId) {
        Optional<Carrinho> carrinhoOpt = carrinhoRepositorio.findById(carrinhoId);

        if (carrinhoOpt.isEmpty()) {
            throw new RuntimeException("Carrinho não encontrado.");
        }

        Carrinho carrinho = carrinhoOpt.get();

        if (carrinho.getItens().isEmpty()) {
            throw new RuntimeException("Carrinho está vazio.");
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(carrinho.getUsuario());
        pedido.setStatus(StatusPedido.PENDENTE);

        pedido = pedidoRepositorio.save(pedido);

        Pedido finalPedido = pedido;
        List<ItemPedido> itensPedido = carrinho.getItens().stream().map(item -> {
            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setPedido(finalPedido);
            itemPedido.setProduto(item.getProduto());
            itemPedido.setQuantidade(item.getQuantidade());
            itemPedido.setPrecoTotal(item.getPrecoTotal());
            return itemPedido;
        }).collect(Collectors.toList());

        itemPedidoRepositorio.saveAll(itensPedido);

        carrinhoRepositorio.delete(carrinho);

        return pedido;
    }

    @Transactional
    public Pedido atualizarStatusPedido(Long pedidoId, StatusPedido novoStatus) {
        // Buscar o pedido no banco
        Pedido pedido = pedidoRepositorio.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));

        // Verificar se o status pode ser alterado
        if (pedido.getStatus() == StatusPedido.CONCLUIDO || pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new RuntimeException("Não é possível alterar o status de um pedido concluído ou cancelado.");
        }

        // Atualizar o status do pedido
        pedido.setStatus(novoStatus);

        // Salvar e retornar o pedido atualizado
        return pedidoRepositorio.save(pedido);
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepositorio.findAll();
    }

    public Pedido buscarPedidoPorId(Long id) {
        return pedidoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));
    }

    @Transactional
    public Pedido cancelarPedido(Long id) {
        Pedido pedido = buscarPedidoPorId(id);

        if (pedido.getStatus() == StatusPedido.CONCLUIDO) {
            throw new RuntimeException("Não é possível cancelar um pedido concluído.");
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        return pedidoRepositorio.save(pedido);
    }
}
