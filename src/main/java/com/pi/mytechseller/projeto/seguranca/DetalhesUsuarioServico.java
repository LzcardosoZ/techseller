package com.pi.mytechseller.projeto.seguranca;

import com.pi.mytechseller.projeto.modelos.Usuario;
import com.pi.mytechseller.projeto.repositorios.UsuarioRepositorio;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class DetalhesUsuarioServico implements UserDetailsService {
    private final UsuarioRepositorio usuarioRepositorio;

    public DetalhesUsuarioServico(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        if (usuario.getNivel() == null) {
            throw new UsernameNotFoundException("Usuário sem permissão definida.");
        }

        // Define a permissão com base no nível do usuário
        GrantedAuthority autoridade = new SimpleGrantedAuthority("ROLE_" + usuario.getNivel().getDescricao().toUpperCase());

        return new User(
                usuario.getEmail(),
                usuario.getSenha(),
                Collections.singletonList(autoridade)
        );
    }
}