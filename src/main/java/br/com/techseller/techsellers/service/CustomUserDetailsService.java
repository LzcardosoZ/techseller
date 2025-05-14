package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.Cliente;
import br.com.techseller.techsellers.entity.User;
import br.com.techseller.techsellers.repository.ClienteRepository;
import br.com.techseller.techsellers.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ClienteRepository clienteRepository;

    public CustomUserDetailsService(UserRepository userRepository, ClienteRepository clienteRepository) {
        this.userRepository = userRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("🔍 Tentando autenticar o e-mail: " + email);

        Optional<User> userOpt = userRepository.findByEmail(email);
        Optional<Cliente> clienteOpt = clienteRepository.findByEmail(email);

        if (userOpt.isPresent() && clienteOpt.isPresent()) {
            System.out.println("🚨 Conflito: e-mail duplicado em 'users' e 'clientes'.");
            throw new UsernameNotFoundException("Conflito de e-mail.");
        }

        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            System.out.println("👤 Login como CLIENTE: " + cliente.getEmail());
            return createUserDetailsFromCliente(cliente);
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("🧑‍💼 Login como USER (Admin/Estoquista): " + user.getEmail());
            return user;  // ⬅️ ESSENCIAL: permite usar auth.getPrincipal() como User
        }

        System.out.println("❌ Nenhum usuário encontrado com o e-mail: " + email);
        throw new UsernameNotFoundException("Usuário não encontrado.");
    }

    private UserDetails createUserDetailsFromCliente(Cliente cliente) {
        List<GrantedAuthority> authorities = List.of(() -> "ROLE_CLIENTE");
        return new org.springframework.security.core.userdetails.User(
                cliente.getEmail(),
                cliente.getSenha(),
                authorities
        );
    }
}
