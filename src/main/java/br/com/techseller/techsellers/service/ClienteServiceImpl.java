package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.dto.EnderecoViaCepDTO;
import br.com.techseller.techsellers.entity.Cliente;
import br.com.techseller.techsellers.entity.Endereco;
import br.com.techseller.techsellers.enums.TipoEndereco;
import br.com.techseller.techsellers.repository.ClienteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Override
    public Cliente cadastrarCliente(Cliente cliente) {
        if (emailExiste(cliente.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
        if (cpfExiste(cliente.getCpf())) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }

        cliente.setSenha(passwordEncoder.encode(cliente.getSenha()));

        log.info("Cliente antes da validação de endereços: {}", cliente);

        validarEnderecos(cliente);

        // Vincula os endereços ao cliente e valida tipo FATURAMENTO
        boolean encontrouFaturamento = false;
        for (Endereco endereco : cliente.getEnderecos()) {
            endereco.setCliente(cliente);

            if (TipoEndereco.FATURAMENTO.equals(endereco.getTipo())) {
                if (encontrouFaturamento) {
                    throw new IllegalArgumentException("Mais de um endereço de faturamento encontrado.");
                }
                encontrouFaturamento = true;
            }
        }

        if (!encontrouFaturamento) {
            throw new IllegalArgumentException("É obrigatório definir um endereço de faturamento.");
        }

        log.info("Endereços prontos para salvar:");
        for (Endereco endereco : cliente.getEnderecos()) {
            log.info("{}", endereco);
        }

        try {
            entityManager.persist(cliente);
            entityManager.flush();
            return cliente;
        } catch (Exception e) {
            log.error("Erro ao salvar cliente no banco: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar cliente: " + e.getMessage());
        }
    }

    @Override
    public void atualizarCliente(Cliente clienteAtualizado) {
        Cliente cliente = clienteRepository.findById(clienteAtualizado.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        cliente.setNomeCompleto(clienteAtualizado.getNomeCompleto());
        cliente.setDataNascimento(clienteAtualizado.getDataNascimento());
        cliente.setGenero(clienteAtualizado.getGenero());

        if (clienteAtualizado.getSenha() != null
                && !clienteAtualizado.getSenha().isBlank()
                && !clienteAtualizado.getSenha().startsWith("$2a$")) {
            cliente.setSenha(passwordEncoder.encode(clienteAtualizado.getSenha()));
        }

        // ✅ Corrigido para evitar ConcurrentModificationException
        if (clienteAtualizado.getEnderecos() != null && !clienteAtualizado.getEnderecos().isEmpty()) {
            List<Endereco> novosEnderecos = new ArrayList<>();
            for (Endereco novoEndereco : clienteAtualizado.getEnderecos()) {
                novoEndereco.setCliente(cliente);
                novosEnderecos.add(novoEndereco);
            }
            cliente.getEnderecos().addAll(novosEnderecos);
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
        log.info("Iniciando validação de endereços para cliente: {}", cliente.getEmail());

        for (Endereco endereco : cliente.getEnderecos()) {
            log.info("Endereço recebido do formulário: {}", endereco);

            EnderecoViaCepDTO viaCep = buscarEnderecoViaCep(endereco.getCep());

            // Preenche os campos do endereço com os dados da API ViaCEP
            endereco.setLogradouro(viaCep.getLogradouro());
            endereco.setBairro(viaCep.getBairro());
            endereco.setCidade(viaCep.getCidade());
            endereco.setUf(viaCep.getUf());

            // Mantém vínculo com o cliente
            endereco.setCliente(cliente);

            log.info("Endereço após preenchimento via ViaCEP: {}", endereco);
        }
    }




    private Endereco validarCep(String cep) {
        if (cep == null || cep.isBlank()) {
            log.warn("CEP vazio recebido.");
            throw new IllegalArgumentException("CEP não pode ser vazio");
        }

        String cepNumerico = cep.replaceAll("[^0-9]", "");

        if (cepNumerico.length() != 8) {
            log.warn("CEP inválido: {}", cep);
            throw new IllegalArgumentException("CEP deve conter 8 dígitos");
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://viacep.com.br/ws/" + cepNumerico + "/json/";

        log.info("Buscando endereço para o CEP: {}", cepNumerico);

        try {
            ResponseEntity<EnderecoViaCepDTO> response = restTemplate.getForEntity(url, EnderecoViaCepDTO.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Erro na resposta do ViaCEP: status={}, body=null", response.getStatusCode());
                throw new IllegalArgumentException("CEP não encontrado");
            }

            EnderecoViaCepDTO viaCep = response.getBody();

            log.info("Endereço retornado: {}, {}, {}", viaCep.getLogradouro(), viaCep.getBairro(), viaCep.getCidade());

            // Constrói um Endereco (entidade) a partir do DTO
            Endereco endereco = new Endereco();
            endereco.setCep(viaCep.getCep());
            endereco.setLogradouro(viaCep.getLogradouro());
            endereco.setComplemento(viaCep.getComplemento());
            endereco.setBairro(viaCep.getBairro());
            endereco.setCidade(viaCep.getCidade());
            endereco.setUf(viaCep.getUf());

            return endereco;

        } catch (HttpClientErrorException e) {
            log.error("Erro HTTP ao consultar CEP: {}", e.getMessage());
            throw new IllegalArgumentException("Erro ao consultar CEP: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao consultar CEP: {}", e.getMessage());
            throw new IllegalArgumentException("Serviço de CEP indisponível no momento");
        }
    }


    @Override
    public void alterarSenha(Cliente cliente, String novaSenha) {
        cliente.setSenha(passwordEncoder.encode(novaSenha));
        clienteRepository.save(cliente);
    }

    private EnderecoViaCepDTO buscarEnderecoViaCep(String cep) {
        if (cep == null || cep.isBlank()) {
            throw new IllegalArgumentException("CEP não pode ser vazio");
        }

        String cepNumerico = cep.replaceAll("[^0-9]", "");
        if (cepNumerico.length() != 8) {
            throw new IllegalArgumentException("CEP deve conter 8 dígitos");
        }

        String url = "https://viacep.com.br/ws/" + cepNumerico + "/json/";

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<EnderecoViaCepDTO> response = restTemplate.getForEntity(url, EnderecoViaCepDTO.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalArgumentException("Erro ao buscar o CEP: resposta inválida da API");
            }

            return response.getBody();

        } catch (Exception e) {
            throw new IllegalArgumentException("Erro ao consultar o CEP: " + e.getMessage());
        }
    }

    @Override
    public Optional<Cliente> buscarPorEmailComEnderecos(String email) {
        return clienteRepository.buscarPorEmailComEnderecos(email);
    }


}