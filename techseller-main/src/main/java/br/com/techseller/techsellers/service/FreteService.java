package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.utils.Coordenadas;

import java.math.BigDecimal;

public interface FreteService {
    Coordenadas obterCoordenadas(String cep);
    double calcularDistancia(Coordenadas origem, Coordenadas destino);
    BigDecimal calcularValorFrete(double distanciaKm);
    String estimarPrazoEntrega(double distanciaKm, String modalidade);
}
