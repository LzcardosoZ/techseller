package br.com.techseller.techsellers.controller;

import br.com.techseller.techsellers.entity.User;
import br.com.techseller.techsellers.repository.UserRepository;
import br.com.techseller.techsellers.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @GetMapping("/login_cliente")
    public String exibirLoginCliente(Model model) {
        // Se desejar passar alguma informação para a view, adicione atributos ao model
        return "login_cliente";
    }

    // Página Home após login bem-sucedido
    @GetMapping("/home")
    public String home(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login?logout";
        }

        Optional<User> usuarioLogadoOpt = userService.findByEmail(userDetails.getUsername());
        usuarioLogadoOpt.ifPresent(user -> model.addAttribute("usuarioLogado", user));

        return usuarioLogadoOpt.isPresent() ? "home" : "redirect:/login?error";
    }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("user", new User());
        return "cadastroAdm";
    }

    @GetMapping("/usuarios/{user_id}")
    public String editarUsuario(@PathVariable("user_id") Long user_id, Model model) {
        try {
            User usuario = userService.buscarPorId(user_id);
            model.addAttribute("usuario", usuario);
            return "editar_usuario";
        } catch (RuntimeException e) {
            return "redirect:/listarUsuarios";
        }
    }

    @PostMapping("/usuarios/{user_id}")
    public String atualizarUsuario(
            @PathVariable Long user_id,
            @ModelAttribute User usuario,
            RedirectAttributes redirectAttributes) {

        User usuarioExistente = userService.buscarPorId(user_id);

        if (usuarioExistente != null) {
            //Atualiza campos básicos do formulário
            if (usuario.getNomeUsuario() != null && !usuario.getNomeUsuario().isBlank()) {
                usuarioExistente.setNomeUsuario(usuario.getNomeUsuario());
            }

            if (usuario.getCpf() != null && !usuario.getCpf().isBlank()) {
                usuarioExistente.setCpf(usuario.getCpf());
            }

            if (usuario.getGrupo() != null) {
                usuarioExistente.setGrupo(usuario.getGrupo());
            }

            // Atualiza senha se informada
            if (usuario.getNewPassword() != null && !usuario.getNewPassword().isBlank()) {
                if (!usuario.getNewPassword().equals(usuario.getConfNewPassword())) {
                    redirectAttributes.addFlashAttribute("error", "As senhas não coincidem!");
                    return "redirect:/usuarios/" + user_id;
                }

                String senhaCriptografada = passwordEncoder.encode(usuario.getNewPassword());
                usuarioExistente.setPassword(senhaCriptografada);
            }

            userService.atualizarUsuario(user_id, usuarioExistente);
        }
        redirectAttributes.addFlashAttribute("success", "Usuário atualizado com sucesso!");
        return "redirect:/listarUsuarios";
    }

    @PostMapping("/usuarios/alterarStatus/{user_id}")
    public String alterarStatusUsuario(@PathVariable("user_id") Long userId) {
        User usuario = userService.buscarPorId(userId);
        if (usuario != null) {
            usuario.setStatus(!usuario.isStatus());
            userService.atualizarUsuario(userId, usuario);
        }
        return "redirect:/listarUsuarios";
    }

    @PostMapping("/registerUser")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        try {
            userService.registeruser(user);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "cadastroAdm";
        }
    }
}