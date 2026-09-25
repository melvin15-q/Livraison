package com.livraison.livreur.controller;

import com.livraison.livreur.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String nomComplet,
                            @RequestParam String email,
                            @RequestParam String password,
                            @RequestParam String confirmPassword,
                            @RequestParam String telephone,
                            Model model) {

        // Vérification de la confirmation de mot de passe avant tout accès à la base
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Les deux mots de passe ne correspondent pas.");
            model.addAttribute("nomComplet", nomComplet);
            model.addAttribute("email", email);
            model.addAttribute("telephone", telephone);
            return "register";
        }

        try {
            userService.registerClient(nomComplet, email, password, telephone);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("nomComplet", nomComplet);
            model.addAttribute("email", email);
            model.addAttribute("telephone", telephone);
            return "register";
        }
    }
}
