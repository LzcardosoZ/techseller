package br.com.techseller.techsellers.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nomeCompleto;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private String genero;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Endereco> enderecos = new ArrayList<>();


    // Endereço de faturamento (padrao = true)
    public Endereco getEnderecoFaturamento() {
        return enderecos.stream()
                .filter(e -> Boolean.TRUE.equals(e.getPadrao()))
                .findFirst()
                .orElse(null);
    }

    // Endereços de entrega (padrao = false)
    public List<Endereco> getEnderecosEntrega() {
        return enderecos.stream()
                .filter(e -> e.getPadrao() == null || !e.getPadrao())
                .collect(Collectors.toList());
    }

    public Endereco getEnderecoPadrao() {
        return enderecos.stream()
                .filter(e -> Boolean.TRUE.equals(e.getPadrao()))
                .findFirst()
                .orElse(null);
    }


}
