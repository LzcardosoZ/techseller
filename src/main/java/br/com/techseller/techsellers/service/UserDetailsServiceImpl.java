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
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String tipo = request.getParameter("tipo");

        if ("cliente".equalsIgnoreCase(tipo)) {
            // Tenta logar como cliente
            Cliente cliente = clienteRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado"));

            // Segurança extra: bloqueia se email existe em user
            if (userRepository.findByEmail(email).isPresent()) {
                throw new UsernameNotFoundException("Acesso negado para cliente.");
            }

            return org.springframework.security.core.userdetails.User
                    .withUsername(cliente.getEmail())
                    .password(cliente.getSenha())
                    .authorities("ROLE_CLIENTE")
                    .build();

        } else {
            // Tenta logar como user (admin/estoquista)
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

            // Segurança extra: bloqueia se email existe em cliente
            if (clienteRepository.findByEmail(email).isPresent()) {
                throw new UsernameNotFoundException("Acesso negado para administrador.");
            }

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .authorities("ROLE_" + user.getGrupo().name())
                    .build();
        }
    }


}
