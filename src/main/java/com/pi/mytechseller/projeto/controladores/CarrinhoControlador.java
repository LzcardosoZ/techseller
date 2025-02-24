package com.pi.mytechseller.projeto.controladores;

import com.pi.mytechseller.projeto.modelos.Carrinho;
import com.pi.mytechseller.projeto.modelos.ItemCarrinho;
import com.pi.mytechseller.projeto.modelos.Usuario;
import com.pi.mytechseller.projeto.servicos.CarrinhoServico;
import com.pi.mytechseller.projeto.servicos.UsuarioServico;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrinho")
public class CarrinhoControlador {

    private final CarrinhoServico carrinhoServico;
    private final UsuarioServico usuarioServico; // Injeção do serviço de usuário

    public CarrinhoControlador(CarrinhoServico carrinhoServico, UsuarioServico usuarioServico) {
        this.carrinhoServico = carrinhoServico;
        this.usuarioServico = usuarioServico;
    }

    @GetMapping
    public ResponseEntity<Carrinho> obterCarrinho(@AuthenticationPrincipal User usuario) {
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
        return usuarioServico.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}
