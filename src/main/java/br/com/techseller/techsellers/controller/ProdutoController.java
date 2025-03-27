package br.com.techseller.techsellers.controller;

import br.com.techseller.techsellers.entity.ImagemProduto;
import br.com.techseller.techsellers.entity.Produto;
import br.com.techseller.techsellers.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nomeUsuario = auth.getName();

        model.addAttribute("nomeUsuario", nomeUsuario);
        List<Produto> produtos = produtoService.listarProdutos(filtro);
        model.addAttribute("produtos", produtos);

        return "listarProdutos";
    }

    @GetMapping("/novo")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("produto", new Produto());
        return "cadastroProduto";
    }

    @GetMapping("/editar/{id}")
    public String exibirFormularioEdicao(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            return produtoService.buscarPorId(id)
                    .map(produto -> {
                        model.addAttribute("produto", produto);
                        return "cadastroProduto";
                    })
                    .orElseGet(() -> {
                        redirectAttributes.addFlashAttribute("erro", "Produto não encontrado para edição");
                        return "redirect:/produtos";
                    });
        } catch (Exception e) {
            log.error("Erro ao carregar produto para edição ID: {}", id, e);
            redirectAttributes.addFlashAttribute("erro", "Erro ao carregar produto para edição");
            return "redirect:/produtos";
        }
    }

    @PostMapping("/salvar")
    public String salvarProduto(
            @Valid @ModelAttribute("produto") Produto produto,
            BindingResult result,
            @RequestParam("imagem") MultipartFile imagem,
            RedirectAttributes redirectAttributes) {

        // Validação do formulário
        if (result.hasErrors()) {
            log.warn("Erros de validação no produto: {}", result.getAllErrors());
            return "cadastroProduto";
        }

        // Validação da imagem
        if (imagem.isEmpty()) {
            log.warn("Tentativa de salvar produto sem imagem");
            redirectAttributes.addFlashAttribute("erro", "Por favor, selecione uma imagem para o produto");
            return produto.getProdutoId() == null ?
                    "redirect:/produtos/novo" :
                    "redirect:/produtos/editar/" + produto.getProdutoId();
        }

        if (!isTipoImagemValido(imagem.getContentType())) {
            log.warn("Tipo de imagem não suportado: {}", imagem.getContentType());
            redirectAttributes.addFlashAttribute("erro", "Tipo de imagem não suportado. Use JPEG, PNG ou GIF");
            return produto.getProdutoId() == null ?
                    "redirect:/produtos/novo" :
                    "redirect:/produtos/editar/" + produto.getProdutoId();
        }

        try {
            // Usando o método salvarProduto existente que já processa produto e imagem
            produtoService.salvarProduto(produto, imagem, true);

            redirectAttributes.addFlashAttribute("sucesso",
                    produto.getProdutoId() == null ?
                            "Produto cadastrado com sucesso!" :
                            "Produto atualizado com sucesso!");

            return "redirect:/produtos";

        } catch (Exception e) {
            log.error("Erro ao salvar produto ID: {}", produto.getProdutoId(), e);
            redirectAttributes.addFlashAttribute("erro",
                    "Erro ao processar o produto: " + e.getMessage());

            return produto.getProdutoId() == null ?
                    "redirect:/produtos/novo" :
                    "redirect:/produtos/editar/" + produto.getProdutoId();
        }
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

            // Caso 1: Tentar obter do sistema de arquivos primeiro
            if (imagem.getCaminhoArquivo() != null) {
                Path caminhoArquivo = Paths.get(imagem.getCaminhoArquivo());
                Resource resource = new UrlResource(caminhoArquivo.toUri());

                if (resource.exists() && resource.isReadable()) {
                    return construirResposta(imagem, resource);
                }
                log.warn("Arquivo físico não encontrado em: {}", caminhoArquivo);
            }

            // Caso 2: Fallback para imagem no banco de dados (BLOB)
            if (imagem.getImagem() != null && imagem.getImagem().length > 0) {
                ByteArrayResource resource = new ByteArrayResource(imagem.getImagem());
                return construirResposta(imagem, resource);
            }

            // Caso 3: Imagem não encontrada em nenhum local
            log.warn("Imagem ID {} não encontrada no sistema de arquivos nem no banco de dados", imagemId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Erro ao carregar imagem ID: {}", imagemId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Método auxiliar para construir a resposta
    private ResponseEntity<Resource> construirResposta(ImagemProduto imagem, Resource resource) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagem.getTipoMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + imagem.getNomeArquivo() + "\"")
                .body(resource);
    }

    @PostMapping("/{id}/imagem")
    public ResponseEntity<String> uploadImagem(
            @PathVariable Long id,
            @RequestParam("imagem") MultipartFile file,
            @RequestParam(value = "imagemPrincipal", defaultValue = "false") boolean imagemPrincipal) {

        try {
            if (file.isEmpty()) {
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

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao salvar imagem: " + e.getMessage());
        }
    }

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