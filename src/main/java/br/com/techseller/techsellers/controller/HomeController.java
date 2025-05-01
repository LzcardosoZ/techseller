package br.com.techseller.techsellers.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    //EndPoint de Acesso a HomePage
    @GetMapping("/")
    public String redirecionarParaLoja() {
        return "redirect:/loja";
    }
}
