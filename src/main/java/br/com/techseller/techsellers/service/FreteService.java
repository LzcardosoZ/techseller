package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.Endereco;
import br.com.techseller.techsellers.utils.Coordenadas;

import java.math.BigDecimal;

public interface FreteService {
    BigDecimal calcularFretePorUF(String uf);
    Endereco obterEnderecoPorCep(String cep);
}
