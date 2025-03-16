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

    @GetMapping("/menu")
    public String menu(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            Optional<User> usuarioLogadoOpt = userService.findByEmail(userDetails.getUsername());

            if (usuarioLogadoOpt.isPresent()) {
                model.addAttribute("usuarioLogado", usuarioLogadoOpt.get());
            }
        }

        return "menu"; // Retorna para menu.html
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("user") User user, Model model) {
        Optional<User> userData = userRepository.findByEmail(user.getEmail());
        if (userData.isPresent()) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (encoder.matches(user.getPassword(), userData.get().getPassword())) {
                return "home";
            } else {
                model.addAttribute("errorMessage", "Senha incorreta.");
                return "login";
            }
        } else {
            model.addAttribute("errorMessage", "Usuário não encontrado.");
            return "login";
        }
    }



    @GetMapping("/listarUsuarios")
    public String listarUsuarios(Model model) {
        List<User> usuarios = userRepository.findAll(); // Busca todos os usuários do banco
        model.addAttribute("usuarios", usuarios);
        return "listarUsuarios";
    }

    @PostMapping("/registerUser")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        String result = null;

        System.out.println(user);
        if (!user.getPassword().equals(user.getConfPassword())) { // Verifica se as senhas são diferentes
            model.addAttribute("errorMessage", "As senhas não coincidem.");
            return "cadastro"; // Retorna para a mesma página com a mensagem de erro
        }

        try {
            userService.registeruser(user);
            return "login"; // Se tudo estiver correto, vai para login
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao cadastrar usuário.");
            return "cadastro";
        }
    }


}