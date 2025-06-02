package br.com.techseller.techsellers.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LinksUteisController {

    @GetMapping("/contato")
    public String contato() {
        return "contato";
    }

    @GetMapping("/politica-privacidade")
    public String politica() {
        return "politica-privacidade";
    }

    @GetMapping("/termos-uso")
    public String termosUso() {
        return "termos-uso";
    }


}
