package br.com.techseller.techsellers.controller;

import br.com.techseller.techsellers.entity.User;
import br.com.techseller.techsellers.repository.UserRepository;
import br.com.techseller.techsellers.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String login(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "login";
    }

    @GetMapping("/listarUsuarios")
    public String listarUsuarios(Model model) {
        List<User> usuarios = userService.listarUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "listarUsuarios"; // Certifique-se de que há um template correspondente em templates/
    }

    @GetMapping("/home")
    public String home(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> usuarioLogadoOpt = userService.findByEmail(userDetails.getUsername());

        usuarioLogadoOpt.ifPresent(user -> model.addAttribute("usuarioLogado", user));

        return usuarioLogadoOpt.isPresent() ? "home" : "redirect:/login?error";
    }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("user", new User());
        return "cadastro";
    }

    @PostMapping("/registerUser")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        try {
            userService.registeruser(user);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "cadastro";
        }
    }
}
