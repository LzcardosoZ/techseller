package br.com.techseller.techsellers.dto;

import lombok.Data;

@Data
public class EnderecoViaCepDTO {
    private String cep;
    private String logradouro;
    private String complemento;
    private String bairro;
    private String localidade; // será mapeado manualmente para "cidade"
    private String uf;
}
