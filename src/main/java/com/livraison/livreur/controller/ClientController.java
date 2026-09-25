package com.livraison.livreur.controller;

import com.livraison.livreur.model.Commande;
import com.livraison.livreur.model.CommandeStatus;
import com.livraison.livreur.model.User;
import com.livraison.livreur.security.UserPrincipal;
import com.livraison.livreur.service.CommandeService;
import com.livraison.livreur.service.LivraisonService;
import com.livraison.livreur.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {

    private final UserService userService;
    private final CommandeService commandeService;
    private final LivraisonService livraisonService;

    @GetMapping("/espace")
    public String espace(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        User user = userService.findByEmail(principal.getEmail());
        List<Commande> commandes = commandeService.getClientCommandes(user);

        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("telephone", user.getTelephone());

        // La commande active la plus récente (validée ou en cours de livraison), s'il y en a une
        Commande active = commandes.stream()
                .filter(c -> c.getStatut() == CommandeStatus.EN_COURS_DE_LIVRAISON || c.getStatut() == CommandeStatus.VALIDEE)
                .findFirst()
                .orElse(null);
        model.addAttribute("commandeActive", active);
        if (active != null) {
            model.addAttribute("livraisonActive", livraisonService.getLivraisonByCommande(active).orElse(null));
        }

        model.addAttribute("historique", commandes.stream().limit(3).toList());
        return "client/espace";
    }

    @GetMapping("/suivi")
    public String suivi(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        User user = userService.findByEmail(principal.getEmail());
        List<Commande> enCours = commandeService.getClientCommandes(user).stream()
                .filter(c -> c.getStatut() == CommandeStatus.EN_COURS_DE_LIVRAISON || c.getStatut() == CommandeStatus.VALIDEE)
                .toList();
        model.addAttribute("username", user.getUsername());
        model.addAttribute("commandes", enCours);
        return "client/suivi";
    }

    @GetMapping("/parametres")
    public String parametres(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        User user = userService.findByEmail(principal.getEmail());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("telephone", user.getTelephone());
        return "client/parametres";
    }
}
