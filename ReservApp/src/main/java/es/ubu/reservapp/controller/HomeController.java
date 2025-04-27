package es.ubu.reservapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.ubu.reservapp.model.entities.Usuario;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
    	model.addAttribute("usuario", new Usuario());
        return "login";
    }
}

