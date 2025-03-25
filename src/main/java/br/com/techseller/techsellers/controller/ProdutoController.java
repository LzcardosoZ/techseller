package br.com.techseller.techsellers.controller;

import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.entity.Produto;
import br.com.techseller.techsellers.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Controller
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @GetMapping
    public String listarProdutos(@RequestParam(required = false) String filtro, Model model) {
        // Obtém o nome do usuário logado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nomeUsuario = auth.getName(); // Nome de login do usuário autenticado

        // Adiciona o nome do usuário ao modelo para ser exibido na página
        model.addAttribute("nomeUsuario", nomeUsuario);

        // Busca e adiciona a lista de produtos conforme o filtro
        List<Produto> produtos = produtoService.listarProdutos(filtro);
        model.addAttribute("produtos", produtos);

        return "listarProdutos"; // Certifique-se de que esse nome corresponde ao HTML correto.
    }

    @GetMapping("/novo")
    public String novoProduto(Model model) {
        model.addAttribute("produto", new Produto());
        return "cadastroProduto";
    }

    @GetMapping("/inativar/{id}")
    public String inativarProduto(@PathVariable Long id) {
        produtoService.inativarProduto(id);
        return "redirect:/produtos";
    }

    @GetMapping("/reativar/{id}")
    public String reativarProduto(@PathVariable Long id) {
        produtoService.reativarProduto(id);
        return "redirect:/produtos";
    }

    @GetMapping("/{id}/imagens")
    public ResponseEntity<List<ImagemProduto>> listarImagens(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.listarImagensPorProduto(id));
    }

    @PostMapping("/salvar")
    public String salvarProduto(@ModelAttribute Produto produto,
                                @RequestParam("imagem") MultipartFile imagem,
                                @RequestParam(value = "imagemPrincipal", defaultValue = "true") boolean imagemPrincipal) {
        produtoService.salvarProduto(produto, imagem, imagemPrincipal);
        return "redirect:/produtos";
    }




    @PostMapping("/{id}/imagem")
    public ResponseEntity<String> uploadImagem(@PathVariable Long id,
                                               @RequestParam("imagem") MultipartFile file,
                                               @RequestParam(value = "imagemPrincipal", defaultValue = "false") boolean imagemPrincipal) {
        try {
            produtoService.salvarImagem(id, file, imagemPrincipal);
            return ResponseEntity.ok("Imagem salva com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao salvar imagem: " + e.getMessage());
        }
    }

}

