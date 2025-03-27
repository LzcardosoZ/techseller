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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/produtos")
@Slf4j
public class ProdutoController {

    @Value("${app.upload.dir}")
    private String diretorioBase;

    @Autowired
    private ProdutoService produtoService;

    @GetMapping
    public String listarProdutos(@RequestParam(required = false) String filtro, Model model) {
        // Obtém o nome do usuário logado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nomeUsuario = auth.getName();

        model.addAttribute("nomeUsuario", nomeUsuario);
        List<Produto> produtos = produtoService.listarProdutos(filtro);
        model.addAttribute("produtos", produtos);

        return "listarProdutos";
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
    public ResponseEntity<Resource> exibirImagem(@PathVariable Long imagemId) {
        try {
            ImagemProduto imagem = produtoService.buscarImagemPorId(imagemId);

            if (imagem == null || imagem.getCaminhoArquivo() == null) {
                return ResponseEntity.notFound().build();
            }

            // CORREÇÃO PRINCIPAL: Usar o diretório base configurado
            Path caminhoAbsoluto = Paths.get(diretorioBase).resolve(imagem.getCaminhoArquivo());
            Resource resource = new UrlResource(caminhoAbsoluto.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("Imagem não encontrada: {}", caminhoAbsoluto);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(imagem.getTipoMime()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + imagem.getNomeArquivo() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Erro ao carregar imagem ID: {}", imagemId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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
        } else if (!isTipoImagemValido(imagem.getContentType())) {
            result.rejectValue("imagens", "imagem.tipo.invalido", "Tipo de imagem não suportado");
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
    public ResponseEntity<String> uploadImagem(
            @PathVariable Long id,
            @RequestParam("imagem") MultipartFile file,
            @RequestParam(value = "imagemPrincipal", defaultValue = "false") boolean imagemPrincipal) {

        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Por favor, selecione um arquivo de imagem válido");
            }

            if (!isTipoImagemValido(file.getContentType())) {
                return ResponseEntity.badRequest()
                        .body("Tipo de arquivo não suportado. Use JPEG, PNG, WEBP ou GIF");
            }

            produtoService.salvarImagem(id, file, imagemPrincipal);
            return ResponseEntity.ok("Imagem salva com sucesso como " +
                    (imagemPrincipal ? "imagem principal" : "imagem secundária"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao salvar imagem: " + e.getMessage());
        }
    }

    /**
     * Valida se o tipo MIME é uma imagem suportada
     */
    private boolean isTipoImagemValido(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/webp") ||
                contentType.equals("image/gif");
    }
}