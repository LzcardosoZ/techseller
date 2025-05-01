package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.utils.Coordenadas;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class FreteServiceImpl implements FreteService {

    @Value("${opencage.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Consulta a API do OpenCage para obter coordenadas geográficas (latitude, longitude) de um CEP.
     */
    @Override
    public Coordenadas obterCoordenadas(String cep) {
        String url = "https://api.opencagedata.com/geocode/v1/json?q=" + cep + "&key=" + apiKey;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        try {
            JSONObject json = new JSONObject(response.getBody());
            JSONObject geometry = json.getJSONArray("results")
                    .getJSONObject(0)
                    .getJSONObject("geometry");

            double lat = geometry.getDouble("lat");
            double lng = geometry.getDouble("lng");
            return new Coordenadas(lat, lng);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao obter coordenadas para o CEP: " + cep, e);
        }
    }

    /**
     * Calcula a distância entre dois pontos geográficos usando a fórmula de Haversine.
     */
    @Override
    public double calcularDistancia(Coordenadas origem, Coordenadas destino) {
        final int R = 6371; // Raio da Terra em km
        double latDistance = Math.toRadians(destino.getLatitude() - origem.getLatitude());
        double lonDistance = Math.toRadians(destino.getLongitude() - origem.getLongitude());

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(origem.getLatitude()))
                * Math.cos(Math.toRadians(destino.getLatitude()))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Calcula o valor do frete com base em um valor fixo por quilômetro.
     */
    @Override
    public BigDecimal calcularValorFrete(double distanciaKm) {
        BigDecimal precoPorKm = new BigDecimal("0.003"); // R$ 0,25 por km

        return precoPorKm.multiply(BigDecimal.valueOf(distanciaKm)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Retorna uma estimativa de dias úteis para entrega baseada na distância e modalidade.
     */
    @Override
    public String estimarPrazoEntrega(double distanciaKm, String modalidade) {
        if ("Econômico".equalsIgnoreCase(modalidade)) {
            if (distanciaKm <= 500) return "3 dias úteis";
            if (distanciaKm <= 1000) return "5 dias úteis";
            if (distanciaKm <= 3000) return "7 dias úteis";
            return "10 dias úteis";
        } else if ("FastExpress".equalsIgnoreCase(modalidade)) {
            if (distanciaKm <= 500) return "2 dias úteis";
            if (distanciaKm <= 1000) return "3 dias úteis";
            if (distanciaKm <= 3000) return "5 dias úteis";
            return "7 dias úteis";
        } else { // FullExpress
            if (distanciaKm <= 500) return "1 dia útil";
            if (distanciaKm <= 1000) return "2 dias úteis";
            if (distanciaKm <= 3000) return "3 dias úteis";
            return "5 dias úteis";
        }
    }
}
