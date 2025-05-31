package br.com.techseller.techsellers.entity;

import br.com.techseller.techsellers.enums.TipoEndereco;
import jakarta.persistence.*;
import jakarta.validation.Valid;
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
    @Valid
    private List<Endereco> enderecos = new ArrayList<>();

    // Endereço de faturamento (padrao = true)
    public Endereco getEnderecoFaturamento() {
        return enderecos.stream()
                .filter(e -> TipoEndereco.FATURAMENTO.equals(e.getTipo()))
                .findFirst()
                .orElse(null);
    }


    // Endereços de entrega (padrao = false)
    public List<Endereco> getEnderecosEntrega() {
        return enderecos.stream()
                .filter(e -> TipoEndereco.ENTREGA.equals(e.getTipo()))
                .collect(Collectors.toList());
    }

    public Endereco getEnderecoPadrao() {
        return getEnderecoFaturamento();
    }
}
