package br.com.techseller.techsellers.repository;

import br.com.techseller.techsellers.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    // Find by name
    List<Produto> findByNomeContainingIgnoreCase(String nome);

    // Search by name or ID
    @Query("SELECT p FROM Produto p WHERE " +
            "(:filtro IS NULL OR p.nome LIKE %:filtro% OR CAST(p.produtoId AS string) LIKE %:filtro%)")
    List<Produto> findByNomeOrId(@Param("filtro") String filtro);
}