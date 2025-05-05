package br.com.techseller.techsellers.service;

import br.com.techseller.techsellers.entity.Cliente;
import br.com.techseller.techsellers.entity.User;
import br.com.techseller.techsellers.repository.ClienteRepository;
import br.com.techseller.techsellers.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final ClienteRepository clienteRepository;

    public UserDetailsServiceImpl(UserRepository userRepository, ClienteRepository clienteRepository) {
        this.userRepository = userRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Tentando autenticar o e-mail: " + email);

        Optional<User> userOpt = userRepository.findByEmail(email);
        Optional<Cliente> clienteOpt = clienteRepository.findByEmail(email);

        if (userOpt.isPresent() && clienteOpt.isPresent()) {
            throw new UsernameNotFoundException("Conflito de e-mail: existe tanto em 'users' quanto em 'clientes'.");
        }

        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            System.out.println("Cliente encontrado: " + cliente.getEmail());
            return org.springframework.security.core.userdetails.User
                    .withUsername(cliente.getEmail())
                    .password(cliente.getSenha())
                    .authorities("ROLE_CLIENTE")
                    .build();
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("Admin/Estoquista encontrado: " + user.getEmail());
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .authorities("ROLE_" + user.getGrupo().name())
                    .build();
        }

        throw new UsernameNotFoundException("Usuário não encontrado: " + email);
    }


}




