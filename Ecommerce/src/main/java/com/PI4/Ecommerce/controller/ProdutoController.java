package com.seuusuario.ecommerce.controller;

import com.seuusuario.ecommerce.model.Produto;
import com.seuusuario.ecommerce.service.ProdutoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {
    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Produto> listarTodos() {
        return service.listarTodos();
    }
}
