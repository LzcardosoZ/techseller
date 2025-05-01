package br.com.techseller.techsellers.service;


import br.com.techseller.techsellers.entity.Cliente;
import java.util.Optional;

public interface ClienteService {
    Cliente cadastrarCliente(Cliente cliente);
    boolean emailExiste(String email);
    boolean cpfExiste(String cpf);
    Optional<Cliente> buscarPorEmail(String email);
    boolean emailJaCadastrado(String email);
    void atualizarCliente(Cliente cliente);
    void alterarSenha(Cliente cliente, String novaSenha);

}