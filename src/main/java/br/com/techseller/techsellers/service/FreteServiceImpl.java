package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.dto.EnderecoViaCepDTO;
import br.com.techseller.techsellers.entity.Endereco;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class FreteServiceImpl implements FreteService {


    private final RestTemplate restTemplate = new RestTemplate();

    public BigDecimal calcularFretePorUF(String uf) {
        return switch (uf) {
            case "SP", "RJ", "MG" -> new BigDecimal("15.00");
            case "PR", "SC", "RS" -> new BigDecimal("20.00");
            case "BA", "PE", "CE" -> new BigDecimal("25.00");
            default -> new BigDecimal("30.00");
        };
    }

    public Endereco obterEnderecoPorCep(String cep) {
        EnderecoViaCepDTO dto = buscarEnderecoViaCep(cep);

        Endereco endereco = new Endereco();
        endereco.setCep(dto.getCep());
        endereco.setLogradouro(dto.getLogradouro());
        endereco.setComplemento(dto.getComplemento());
        endereco.setBairro(dto.getBairro());
        endereco.setCidade(dto.getCidade());
        endereco.setUf(dto.getUf());

        return endereco;
    }

    private EnderecoViaCepDTO buscarEnderecoViaCep(String cep) {
        try {
            String url = "https://viacep.com.br/ws/" + cep + "/json/";
            ResponseEntity<EnderecoViaCepDTO> response = restTemplate.getForEntity(url, EnderecoViaCepDTO.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao consultar o CEP: " + cep, e);
        }
    }

}
