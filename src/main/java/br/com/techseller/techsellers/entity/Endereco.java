package br.com.techseller.techsellers.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

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

    @JsonProperty("localidade") // ← necessário para preencher corretamente
    @Column(name = "cidade")
    private String cidade;

    @JsonProperty("uf") // ← ViaCEP envia exatamente "uf"
    private String uf;

    private String numero;

    @Column(name = "padrao")
    private Boolean padrao = false;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Override
    public String toString() {
        return "Endereco{" +
                "cep='" + cep + '\'' +
                ", logradouro='" + logradouro + '\'' +
                ", numero='" + numero + '\'' +
                ", complemento='" + complemento + '\'' +
                ", bairro='" + bairro + '\'' +
                ", cidade='" + cidade + '\'' +
                ", uf='" + uf + '\'' +
                ", padrao=" + padrao +
                '}';
    }

}
