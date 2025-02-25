package com.pi.mytechseller.projeto.modelos;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
<<<<<<< HEAD
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false)
    private String telefone;

    @Column(nullable = false)
    private String endereco;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false)
    private String senha;

    @ManyToOne
    @JoinColumn(name = "id_nivel")
    private Nivel nivel;

    private boolean ativo;
=======
    @Column(name = "ID")
    private Long id;

    @Column(name = "Nome", nullable = false)
    private String nome;

    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Column(name = "CPF", nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(name = "Telefone", nullable = false)
    private String telefone;

    @Column(name = "Endereco", nullable = false)
    private String endereco;

    @Column(name = "Senha", nullable = false)
    private String senha;

    @Column(name = "Ativo", nullable = false)
    private Boolean ativo;


    @ManyToOne
    @JoinColumn(name = "ID_Nivel")
    private Nivel nivel;

>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

<<<<<<< HEAD
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

=======
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

<<<<<<< HEAD
=======
    public void setAtivo(Boolean ativo) { this.ativo = ativo;}

    public Boolean getAtivo() { return ativo; }

>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
    public Nivel getNivel() {
        return nivel;
    }

    public void setNivel(Nivel nivel) {
        this.nivel = nivel;
    }

<<<<<<< HEAD
    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

=======
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
<<<<<<< HEAD
        return Objects.equals(id, usuario.id);
=======
        return id != null && id.equals(usuario.id);
>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
<<<<<<< HEAD
=======

>>>>>>> 4b35fa2 (aprimorando as classes do pacote modelos)
}

