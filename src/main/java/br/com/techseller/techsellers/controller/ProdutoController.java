package br.com.techseller.techsellers.controller;

import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.entity.Produto;
import br.com.techseller.techsellers.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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

    @GetMapping("/imagem/{imagemId}")
    public ResponseEntity<byte[]> exibirImagem(@PathVariable Long imagemId) {
        try {
            ImagemProduto imagem = produtoService.buscarImagemPorId(imagemId);

            if (imagem == null || imagem.getImagem() == null) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(imagem.getImagem().length);

            return new ResponseEntity<>(imagem.getImagem(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/salvar")
    public String salvarProduto(
            @ModelAttribute Produto produto,
            BindingResult result,
            @RequestParam("imagem") MultipartFile imagem,
            @RequestParam(value = "imagemPrincipal", defaultValue = "false") boolean imagemPrincipal,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Validação manual da imagem
        if (imagem == null || imagem.isEmpty()) {
            result.rejectValue("imagens", "imagem.vazia", "Selecione uma imagem");
        }

        if (result.hasErrors()) {
            return "cadastroProduto";
        }

        try {
            produtoService.salvarProduto(produto, imagem, imagemPrincipal);
            redirectAttributes.addFlashAttribute("success", "Produto cadastrado com sucesso!");
            return "redirect:/produtos";
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao cadastrar produto: " + e.getMessage());
            return "cadastroProduto";
        }
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

