package com.seuusuario.ecommerce.service;

import com.seuusuario.ecommerce.model.Produto;
import com.seuusuario.ecommerce.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProdutoService {
    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public List<Produto> listarTodos() {
        return repository.findAll();
    }
}
