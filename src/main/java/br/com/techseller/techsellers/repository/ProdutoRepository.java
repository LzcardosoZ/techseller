package br.com.techseller.techsellers.repository;

import br.com.techseller.techsellers.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Busca por nome (ignorando case)
    List<Produto> findByNomeContainingIgnoreCase(String nome);

    // Busca por nome ou ID
    @Query("SELECT p FROM Produto p WHERE " +
            "(:filtro IS NULL OR p.nome LIKE %:filtro% OR CAST(p.produtoId AS string) LIKE %:filtro%)")
    List<Produto> findByNomeOrId(@Param("filtro") String filtro);

    // Verifica se produto existe (novo método adicionado)
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Produto p WHERE p.produtoId = :id")
    boolean existsById(@Param("id") Long id);

    // Busca por ID com LEFT JOIN FETCH para imagens (otimização)
    @Query("SELECT p FROM Produto p LEFT JOIN FETCH p.imagens WHERE p.produtoId = :id")
    Optional<Produto> findByIdWithImagens(@Param("id") Long id);
}