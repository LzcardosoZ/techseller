package br.com.techseller.techsellers.entity;

import jakarta.persistence.*;
import lombok.Data;


// Classe para endereços (embutida na mesma tabela ou separada)
@Entity
@Data
public class Endereco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cep;
    private String logradouro;
    private String complemento;
    private String bairro;

    @Column(name = "cidade")
    private String cidade;

    private String uf;
    private String numero;

    @Column(name = "padrao")
    private Boolean padrao = false;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

}
