package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.Cliente;
import br.com.techseller.techsellers.entity.Endereco;
import br.com.techseller.techsellers.repository.ClienteRepository;
import br.com.techseller.techsellers.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Cliente cadastrarCliente(Cliente cliente) {
        if (emailExiste(cliente.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
        if (cpfExiste(cliente.getCpf())) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }

        cliente.setSenha(passwordEncoder.encode(cliente.getSenha()));

        validarEnderecos(cliente);

        return clienteRepository.save(cliente);
    }

    @Override
    public void atualizarCliente(Cliente clienteAtualizado) {
        Cliente cliente = clienteRepository.findById(clienteAtualizado.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        cliente.setNomeCompleto(clienteAtualizado.getNomeCompleto());
        cliente.setDataNascimento(clienteAtualizado.getDataNascimento());
        cliente.setGenero(clienteAtualizado.getGenero());

        if (clienteAtualizado.getSenha() != null && !clienteAtualizado.getSenha().isBlank()) {
            cliente.setSenha(passwordEncoder.encode(clienteAtualizado.getSenha()));
        }

        if (clienteAtualizado.getEnderecosEntrega() != null) {
            cliente.setEnderecosEntrega(clienteAtualizado.getEnderecosEntrega());
        }

        clienteRepository.save(cliente);
    }

    @Override
    public boolean emailExiste(String email) {
        return clienteRepository.existsByEmail(email);
    }

    @Override
    public boolean cpfExiste(String cpf) {
        return clienteRepository.existsByCpf(cpf);
    }

    @Override
    public Optional<Cliente> buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email);
    }

    @Override
    public boolean emailJaCadastrado(String email) {
        return clienteRepository.existsByEmail(email);
    }

    private void validarEnderecos(Cliente cliente) {
        Endereco enderecoFaturamento = validarCep(cliente.getEnderecoFaturamento().getCep());
        cliente.setEnderecoFaturamento(enderecoFaturamento);

        for (int i = 0; i < cliente.getEnderecosEntrega().size(); i++) {
            Endereco enderecoEntrega = validarCep(cliente.getEnderecosEntrega().get(i).getCep());
            cliente.getEnderecosEntrega().set(i, enderecoEntrega);
        }
    }

    private Endereco validarCep(String cep) {
        if (cep == null || cep.isBlank()) {
            throw new IllegalArgumentException("CEP não pode ser vazio");
        }

        String cepNumerico = cep.replaceAll("[^0-9]", "");

        if (cepNumerico.length() != 8) {
            throw new IllegalArgumentException("CEP deve conter 8 dígitos");
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://viacep.com.br/ws/" + cepNumerico + "/json/";

        try {
            ResponseEntity<Endereco> response = restTemplate.getForEntity(url, Endereco.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalArgumentException("CEP não encontrado");
            }

            Endereco endereco = response.getBody();
            endereco.setCep(cep);
            return endereco;

        } catch (HttpClientErrorException e) {
            throw new IllegalArgumentException("Erro ao consultar CEP: " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Serviço de CEP indisponível no momento");
        }
    }
    @Override
    public void alterarSenha(Cliente cliente, String novaSenha) {
        cliente.setSenha(passwordEncoder.encode(novaSenha));
        clienteRepository.save(cliente);
    }

}