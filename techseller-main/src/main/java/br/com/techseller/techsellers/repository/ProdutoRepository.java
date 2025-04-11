package br.com.techseller.techsellers.repository;

import br.com.techseller.techsellers.entity.Produto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @EntityGraph(attributePaths = {"imagens"})
    @Override
    Optional<Produto> findById(Long id);

    @Query("SELECT DISTINCT p FROM Produto p LEFT JOIN p.imagens i " +
            "WHERE i.imagemPrincipal = true OR i IS NULL")
    List<Produto> findAllWithPrincipalImage();

    @Query("SELECT p FROM Produto p LEFT JOIN p.imagens i " +
            "WHERE LOWER(p.nome) LIKE LOWER(concat('%', :nome, '%')) " +
            "AND (i.imagemPrincipal = true OR i IS NULL)")
    List<Produto> findByNomeContainingIgnoreCase(@Param("nome") String nome);

    @Query("SELECT p FROM Produto p LEFT JOIN p.imagens i " +
            "WHERE (:filtro IS NULL OR " +
            "LOWER(p.nome) LIKE LOWER(concat('%', :filtro, '%')) OR " +
            "CAST(p.produtoId AS string) LIKE :filtro) " +
            "AND (i.imagemPrincipal = true OR i IS NULL)")
    List<Produto> findByNomeOrId(@Param("filtro") String filtro);

    @EntityGraph(attributePaths = {"imagens"})
    @Query("SELECT p FROM Produto p LEFT JOIN FETCH p.imagens i " +
            "WHERE p.produtoId = :id " +
            "ORDER BY i.ordem")
    Optional<Produto> findComImagensOrdenadas(@Param("id") Long id);

    @Query("SELECT COUNT(i) FROM ImagemProduto i WHERE i.produto.produtoId = :produtoId")
    int countImagensByProdutoId(@Param("produtoId") Long produtoId);

    @Modifying
    @Query("UPDATE ImagemProduto i SET i.ordem = :ordem WHERE i.id IN :ids")
    void atualizarOrdemImagens(@Param("ids") List<Long> ids, @Param("ordem") Integer ordem);

    boolean existsByProdutoId(Long produtoId);

    @EntityGraph(attributePaths = {"imagens"})
    @Query("SELECT p FROM Produto p LEFT JOIN FETCH p.imagens WHERE p.produtoId = :produtoId")
    Optional<Produto> findByIdWithImagens(Long produtoId);

    @Query("SELECT p FROM Produto p LEFT JOIN p.imagens i " +
            "WHERE i.imagemPrincipal = true OR i IS NULL ORDER BY p.nome DESC")
    List<Produto> findAllWithPrincipalImageOrderedDesc();

    // 🔧 CORREÇÃO APLICADA AQUI:
    @Query("SELECT p FROM Produto p LEFT JOIN p.imagens i " +
            "WHERE (:filtro IS NULL OR " +
            "(LOWER(p.nome) LIKE LOWER(concat('%', :filtro, '%')) OR " +
            "CAST(p.produtoId AS string) LIKE :filtro)) " +
            "AND p.ativo = true " +
            "AND (i.imagemPrincipal = true OR i IS NULL)")
    List<Produto> findByNomeOrIdAndAtivo(@Param("filtro") String filtro);

    List<Produto> findByAtivoTrue();
}