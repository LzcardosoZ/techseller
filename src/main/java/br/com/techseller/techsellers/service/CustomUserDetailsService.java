package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.Cliente;
import br.com.techseller.techsellers.entity.User;
import br.com.techseller.techsellers.repository.ClienteRepository;
import br.com.techseller.techsellers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Primary
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ClienteRepository clienteRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository, ClienteRepository clienteRepository) {
        this.userRepository = userRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Tenta encontrar o usuário na tabela de users
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return createUserDetailsFromUser(userOpt.get());
        }
        // Se não encontrar, tenta na tabela de clientes
        Optional<Cliente> clientOpt = clienteRepository.findByEmail(email);
        if (clientOpt.isPresent()) {
            return createUserDetailsFromCliente(clientOpt.get());
        }
        // Se nenhum for encontrado, lança a exceção
        throw new UsernameNotFoundException("Usuário não encontrado: " + email);
    }

    private UserDetails createUserDetailsFromUser(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),       // Email como username
                user.getPassword(),    // Senha criptografada
                user.getAuthorities()  // Autorização conforme definido no seu objeto User
        );
    }

    private UserDetails createUserDetailsFromCliente(Cliente cliente) {
        // Define uma autoridade padrão para clientes (ex.: ROLE_CLIENTE)
        List<GrantedAuthority> authorities = List.of(() -> "ROLE_CLIENTE");
        return new org.springframework.security.core.userdetails.User(
                cliente.getEmail(),
                cliente.getSenha(),  // Certifique-se que a senha do cliente também esteja criptografada
                authorities
        );
    }
}
