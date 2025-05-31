package br.com.techseller.techsellers.entity;

import br.com.techseller.techsellers.enums.TipoEndereco;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
public class Endereco {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "CEP é obrigatório")
    private String cep;

    @NotBlank(message = "Logradouro é obrigatório")
    private String logradouro;

    @NotBlank(message = "Número é obrigatório")
    private String numero;

    @NotBlank(message = "Bairro é obrigatório")
    private String bairro;

    private String complemento;

    @JsonProperty("localidade")
    @Column(name = "cidade")
    @NotBlank(message = "Cidade é obrigatória")
    private String cidade;

    @JsonProperty("uf")
    @NotBlank(message = "UF é obrigatória")
    @Size (min = 2, max = 2, message = "UF deve ter duas letras")
    private String uf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEndereco tipo;

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
                ", tipo de endereco = =" + tipo +
                '}';
    }

}
