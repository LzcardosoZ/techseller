package br.com.techseller.techsellers.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

// Classe para endereços (embutida na mesma tabela ou separada)
@Embeddable
@Data
public class Endereco {
    private String cep;
    private String logradouro;
    private String complemento;
    private String bairro;

    @Column(name = "localidade") // A API ViaCEP retorna "localidade" para cidade
    private String cidade;

    private String uf;
    private String numero;

    @Column(name = "padrao")
    private Boolean padrao = false;


}