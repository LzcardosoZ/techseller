package com.pi.mytechseller.projeto.controladores;

import com.pi.mytechseller.projeto.modelos.Carrinho;
import com.pi.mytechseller.projeto.modelos.ItemCarrinho;
import com.pi.mytechseller.projeto.modelos.Usuario;
import com.pi.mytechseller.projeto.servicos.CarrinhoServico;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrinho")
public class CarrinhoControlador {

    private final CarrinhoServico carrinhoServico;

    public CarrinhoControlador(CarrinhoServico carrinhoServico) {
        this.carrinhoServico = carrinhoServico;
    }

    @GetMapping
    public ResponseEntity<Carrinho> obterCarrinho(@AuthenticationPrincipal User usuario) {
        // Aqui precisaremos buscar o usuário no banco pelo email (usuário autenticado)
        // Supondo que já temos um método para isso no serviço de usuário
        Usuario usuarioBanco = buscarUsuarioPorEmail(usuario.getUsername());
        return ResponseEntity.ok(carrinhoServico.obterCarrinhoAtivo(usuarioBanco));
    }

    @PostMapping("/adicionar")
    public ResponseEntity<ItemCarrinho> adicionarProdutoAoCarrinho(
            @AuthenticationPrincipal User usuario,
            @RequestParam Long produtoId,
            @RequestParam Integer quantidade) {

        Usuario usuarioBanco = buscarUsuarioPorEmail(usuario.getUsername());
        return ResponseEntity.ok(carrinhoServico.adicionarProdutoAoCarrinho(usuarioBanco, produtoId, quantidade));
    }

    @PostMapping("/finalizar")
    public ResponseEntity<String> finalizarCarrinho(@AuthenticationPrincipal User usuario) {
        Usuario usuarioBanco = buscarUsuarioPorEmail(usuario.getUsername());
        carrinhoServico.finalizarCarrinho(usuarioBanco);
        return ResponseEntity.ok("Carrinho finalizado com sucesso!");
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        // Simulação de busca do usuário autenticado no banco
        return new Usuario(); // Aqui, implementar corretamente o serviço de busca no banco
    }
}
