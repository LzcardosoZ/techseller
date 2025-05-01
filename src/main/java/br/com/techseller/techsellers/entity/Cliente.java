package br.com.techseller.techsellers.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "clientes") // Opcional: customiza o nome da tabela
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nomeCompleto;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf; // Formatado: "000.000.000-00"

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha; // Será criptografada antes de salvar

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private String genero; // "Masculino", "Feminino", "Outro"

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "endereco_faturamento_id")
    private Endereco enderecoFaturamento;


    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Endereco> enderecosEntrega;



    // Getters e Setters (ou use Lombok @Data)
    // Construtores (pode usar @NoArgsConstructor e @AllArgsConstructor)
}

