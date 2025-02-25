package com.pi.mytechseller.projeto.modelos;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
<<<<<<< HEAD
    private Long id;

    @Column(nullable = false)
=======
    @Column(name = "ID_Categoria")
    private Long id;

    @Column(name = "Descricao", nullable = false)
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
    private String descricao;

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categoria categoria = (Categoria) o;
<<<<<<< HEAD
        return Objects.equals(id, categoria.id);
    }

=======
        return id != null && id.equals(categoria.id);
    }


>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}