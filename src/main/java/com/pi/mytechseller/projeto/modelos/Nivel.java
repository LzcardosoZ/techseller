package com.pi.mytechseller.projeto.modelos;

import jakarta.persistence.*;

<<<<<<< HEAD
@Entity
@Table(name = "Nivel")
public class Nivel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
=======
import java.util.Objects;

@Entity
@Table(name = "Nivel")
public class Nivel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Nivel")
    private Long id;

    @Column(name = "Descricao", nullable = false)
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
    private String descricao;

    // Getters and Setters
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
<<<<<<< HEAD
=======

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nivel nivel = (Nivel) o;
        return id != null && id.equals(nivel.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
}