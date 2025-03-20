package br.com.techseller.techsellers.controller;

import br.com.techseller.techsellers.entity.Grupo;
import br.com.techseller.techsellers.entity.User;
import br.com.techseller.techsellers.repository.UserRepository;
import br.com.techseller.techsellers.service.UserService;
import br.com.techseller.techsellers.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Exibe a tela de login
    @GetMapping("/login")
    public String login(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "login";
    }

    // Página Home após login bem-sucedido
    @GetMapping("/home")
    public String home(Model model, @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        Optional<User> usuarioLogadoOpt = userService.findByEmail(userDetails.getUsername());

        if (usuarioLogadoOpt.isPresent()) {
            model.addAttribute("usuarioLogado", usuarioLogadoOpt.get());
            return "home"; // Renderiza a página home.html
        } else {
            return "redirect:/login?error"; // Redireciona caso o usuário não seja encontrado
        }
    }

    // Página de cadastro
    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("user", new User());
        return "cadastro";
    }

    @GetMapping("/listarUsuarios")
    public String listarUsuarios(@RequestParam(required = false) String username, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> usuarioLogadoOpt = userService.findByEmail(userDetails.getUsername());

        usuarioLogadoOpt.ifPresent(user -> model.addAttribute("usuarioLogado", user));

        List<User> usuarios;
        if (username != null && !username.isEmpty()) {
            usuarios = userRepository.findByUsernameContainingIgnoreCase(username);
        } else {
            usuarios = userRepository.findAll();
        }

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("nomePesquisa", username); // Para manter o valor no input

        return "listarUsuarios";
    }

    @GetMapping("/usuarios/{user_id}/editar")
    public String editarUsuario(@PathVariable("user_id") Long userId, Model model) {
        User user = userService.buscarPorId(userId);
        if (user == null) {
            throw new RuntimeException("Usuário não encontrado!");
        }
        model.addAttribute("usuario", user); // ✅ Passando o objeto corretamente
        return "editar_usuario";
    }



    @PostMapping("/usuarios/{user_id}")
    public String atualizarUsuario(@PathVariable Long user_id, @ModelAttribute User usuario) {
        User usuarioExistente = userService.buscarPorId(user_id);

        if (usuarioExistente != null) {
            if (usuario.getUsername() != null && !usuario.getUsername().isEmpty()) {
                usuarioExistente.setUsername(usuario.getUsername()); // ✅ Agora salva corretamente
            }
            if (usuario.getCpf() != null && !usuario.getCpf().isEmpty()) {
                usuarioExistente.setCpf(usuario.getCpf());
            }
            if (usuario.getGrupo() != null) {
                usuarioExistente.setGrupo(usuario.getGrupo());
            }

            usuarioExistente.setStatus(usuario.isStatus());

            userService.atualizarUsuario(user_id, usuarioExistente);
        }

        return "redirect:/listarUsuarios";
    }





    @PostMapping("/registerUser")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        if (!user.getPassword().equals(user.getConfPassword())) {
            model.addAttribute("errorMessage", "As senhas não coincidem.");
            return "cadastro";
        }

        try {
            userService.registeruser(user);
            return "redirect:/login"; // Se tudo estiver correto, redireciona para login
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao cadastrar usuário.");
            return "cadastro";
        }
    }


}