package com.pi.mytechseller.projeto.controladores;

import com.pi.mytechseller.projeto.modelos.Pedido;
import com.pi.mytechseller.projeto.modelos.StatusPedido;
import com.pi.mytechseller.projeto.servicos.PedidoServico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedido")
public class PedidoControlador {

    private final PedidoServico pedidoServico;

    public PedidoControlador(PedidoServico pedidoServico) {
        this.pedidoServico = pedidoServico;
    }

    // Finalizar um pedido a partir do carrinho
    @PostMapping("/finalizar")
    public ResponseEntity<Pedido> finalizarPedido(@RequestParam Long carrinhoId) {
        return ResponseEntity.ok(pedidoServico.finalizarPedido(carrinhoId));
    }

    // Atualizar o status do pedido
    @PutMapping("/atualizar-status")
    public ResponseEntity<Pedido> atualizarStatus(@RequestParam Long pedidoId, @RequestParam StatusPedido status) {
        return ResponseEntity.ok(pedidoServico.atualizarStatusPedido(pedidoId, status));
    }

    // Listar todos os pedidos
    @GetMapping("/listar")
    public ResponseEntity<List<Pedido>> listarPedidos() {
        return ResponseEntity.ok(pedidoServico.listarPedidos());
    }

    // Buscar um pedido por ID
    @GetMapping("/buscar/{id}")
    public ResponseEntity<Pedido> buscarPedidoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoServico.buscarPedidoPorId(id));
    }

    // Cancelar um pedido
    @PutMapping("/cancelar/{id}")
    public ResponseEntity<Pedido> cancelarPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoServico.cancelarPedido(id));
    }
}
