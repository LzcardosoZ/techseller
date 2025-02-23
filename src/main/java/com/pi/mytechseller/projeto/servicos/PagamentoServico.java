package com.pi.mytechseller.projeto.servicos;

import com.pi.mytechseller.projeto.modelos.*;
import com.pi.mytechseller.projeto.repositorios.PagamentoRepositorio;
import com.pi.mytechseller.projeto.repositorios.PedidoRepositorio;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PagamentoServico {

    private final PagamentoRepositorio pagamentoRepositorio;
    private final PedidoRepositorio pedidoRepositorio;

    public PagamentoServico(PagamentoRepositorio pagamentoRepositorio, PedidoRepositorio pedidoRepositorio) {
        this.pagamentoRepositorio = pagamentoRepositorio;
        this.pedidoRepositorio = pedidoRepositorio;
    }

    public Pagamento processarPagamento(Long pedidoId, MetodoPagamento metodo) {
        Optional<Pedido> pedidoOpt = pedidoRepositorio.findById(pedidoId);

        if (pedidoOpt.isEmpty()) {
            throw new RuntimeException("Pedido não encontrado.");
        }

        Pedido pedido = pedidoOpt.get();

        if (pagamentoRepositorio.findById(pedidoId).isPresent()) {
            throw new RuntimeException("Este pedido já possui um pagamento.");
        }

        Pagamento pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setMetodoPagamento(metodo);
        pagamento.setStatusPagamento(StatusPagamento.APROVADO);
        pagamento.setDataPagamento(LocalDateTime.now());

        return pagamentoRepositorio.save(pagamento);
    }
}
