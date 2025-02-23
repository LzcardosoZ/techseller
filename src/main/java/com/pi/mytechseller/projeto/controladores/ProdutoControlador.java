package com.pi.mytechseller.projeto.controladores;

import com.pi.mytechseller.projeto.modelos.Produto;
import com.pi.mytechseller.projeto.servicos.ProdutoServico;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produto")
public class ProdutoControlador {

    private final ProdutoServico produtoServico;

    public ProdutoControlador(ProdutoServico produtoServico) {
        this.produtoServico = produtoServico;
    }

    @PostMapping("/adicionar")
    public ResponseEntity<Produto> adicionarProduto(@RequestBody Produto produto) {
        return ResponseEntity.ok(produtoServico.adicionarProduto(produto));
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<Produto> atualizarProduto(@PathVariable Long id, @RequestBody Produto produto) {
        return ResponseEntity.ok(produtoServico.atualizarProduto(id, produto));
    }

    @DeleteMapping("/remover/{id}")
    public ResponseEntity<String> removerProduto(@PathVariable Long id) {
        produtoServico.removerProduto(id);
        return ResponseEntity.ok("Produto removido com sucesso!");
    }

    @GetMapping("/listar")
    public ResponseEntity<List<Produto>> listarProdutos() {
        return ResponseEntity.ok(produtoServico.listarProdutos());
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<Produto> buscarProdutoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produtoServico.buscarProdutoPorId(id));
    }
}
