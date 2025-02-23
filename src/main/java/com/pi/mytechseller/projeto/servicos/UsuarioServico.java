package com.pi.mytechseller.projeto.servicos;

import com.pi.mytechseller.projeto.modelos.Usuario;
import com.pi.mytechseller.projeto.repositorios.UsuarioRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServico {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServico(UsuarioRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario salvarUsuario(Usuario usuario) {
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha())); // Encripta a senha antes de salvar
        return usuarioRepositorio.save(usuario);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepositorio.findByEmail(email);
    }

    public boolean validarSenha(String senhaDigitada, String senhaEncriptada) {
        return passwordEncoder.matches(senhaDigitada, senhaEncriptada);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepositorio.findAll();
    }
}
