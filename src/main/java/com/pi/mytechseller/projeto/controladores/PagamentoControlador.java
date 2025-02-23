package com.pi.mytechseller.projeto.controladores;

import com.pi.mytechseller.projeto.modelos.MetodoPagamento;
import com.pi.mytechseller.projeto.modelos.Pagamento;
import com.pi.mytechseller.projeto.servicos.PagamentoServico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagamento")
public class PagamentoControlador {

    private final PagamentoServico pagamentoServico;

    public PagamentoControlador(PagamentoServico pagamentoServico) {
        this.pagamentoServico = pagamentoServico;
    }

    @PostMapping("/processar")
    public ResponseEntity<Pagamento> processarPagamento(@RequestParam Long pedidoId, @RequestParam MetodoPagamento metodo) {
        return ResponseEntity.ok(pagamentoServico.processarPagamento(pedidoId, metodo));
    }
}
