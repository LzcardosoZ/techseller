package br.com.techseller.techsellers.controller;

import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.entity.Produto;
import br.com.techseller.techsellers.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/produtos")
@Slf4j
public class ProdutoController {

    @Value("${app.upload.dir}")
    private String diretorioBase;

    @Autowired
    private ProdutoService produtoService;

    // ============== MÉTODOS PRINCIPAIS ==============

    @GetMapping
    public String listarProdutos(@RequestParam(required = false) String filtro, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nomeUsuario = auth.getName();
        model.addAttribute("nomeUsuario", nomeUsuario);

        List<Produto> produtos = produtoService.listarProdutos(filtro);
        model.addAttribute("produtos", produtos);
        return "listarProdutos";
    }

    @GetMapping("/editar/{id}")
    public String exibirFormularioEdicao(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Produto> produtoOpt = produtoService.buscarPorId(id);
        if (produtoOpt.isPresent()) {
            model.addAttribute("produto", produtoOpt.get());
            return "editarProduto";
        }
        redirectAttributes.addFlashAttribute("error", "Produto não encontrado");
        return "redirect:/produtos";
    }

    @PostMapping("/editar/{id}")
    public String processarEdicaoProduto(
            @PathVariable Long id,
            @ModelAttribute @Valid Produto produto,
            BindingResult result,
            @RequestParam(value = "imagem", required = false) MultipartFile imagem,
            @RequestParam(value = "imagemPrincipal", defaultValue = "false") boolean imagemPrincipal,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "editarProduto";
        }

        try {
            produtoService.editarProduto(produto, imagem, imagemPrincipal);
            redirectAttributes.addFlashAttribute("success", "Produto atualizado com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao editar produto ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Erro: " + e.getMessage());
        }
        return "redirect:/produtos";
    }

    @GetMapping("/novo")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("produto", new Produto());
        return "cadastroProduto";
    }

    @PostMapping("/salvar")
    public String processarCadastroProduto(
            @ModelAttribute @Valid Produto produto,
            BindingResult result,
            @RequestParam("imagem") MultipartFile imagem,
            @RequestParam(value = "imagemPrincipal", defaultValue = "false") boolean imagemPrincipal,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return (produto.getProdutoId() == null) ? "cadastroProduto" : "editarProduto";
        }

        try {
            if (produto.getProdutoId() == null) {
                produtoService.salvarProduto(produto, imagem, imagemPrincipal);
            } else {
                produtoService.editarProduto(produto, imagem, imagemPrincipal);
            }
            redirectAttributes.addFlashAttribute("success", "Produto salvo com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao salvar produto", e);
            redirectAttributes.addFlashAttribute("error", "Erro: " + e.getMessage());
        }
        return "redirect:/produtos";
    }

    // ============== MÉTODOS DE IMAGENS ==============

    @GetMapping("/{id}/imagens")
    public ResponseEntity<List<ImagemProduto>> listarImagens(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.listarImagensPorProduto(id));
    }

    @GetMapping("/imagem/{imagemId}")
    public ResponseEntity<Resource> exibirImagem(@PathVariable Long imagemId) {
        try {
            ImagemProduto imagem = produtoService.buscarImagemPorId(imagemId);
            Path caminhoAbsoluto = Paths.get(diretorioBase).resolve(imagem.getCaminhoArquivo());
            Resource resource = new UrlResource(caminhoAbsoluto.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(imagem.getTipoMime()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imagem.getNomeArquivo() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Erro ao carregar imagem ID: {}", imagemId, e);
            return ResponseEntity.status(500).build();
        }
    }

    // ============== MÉTODOS DE STATUS ==============

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

    // ============== MÉTODOS AUXILIARES ==============

    @PostMapping("/{id}/imagem")
    public ResponseEntity<String> uploadImagemAdicional(
            @PathVariable Long id,
            @RequestParam("imagem") MultipartFile file,
            @RequestParam(value = "imagemPrincipal", defaultValue = "false") boolean imagemPrincipal) {

        try {
            produtoService.salvarImagem(id, file, imagemPrincipal);
            return ResponseEntity.ok("Imagem adicionada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}