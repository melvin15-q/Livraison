package com.livraison.livreur.controller;

import com.livraison.livreur.model.LivraisonStatus;
import com.livraison.livreur.model.User;
import com.livraison.livreur.security.UserPrincipal;
import com.livraison.livreur.service.CommandeService;
import com.livraison.livreur.service.LivraisonService;
import com.livraison.livreur.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LivraisonController {

    private final LivraisonService livraisonService;
    private final CommandeService commandeService;
    private final UserService userService;

    // --- Espace Admin ---

    @PostMapping("/admin/orders/{id}/assign")
    public String assignLivreur(@PathVariable Long id,
                                @RequestParam Long livreurId,
                                Model model) {
        try {
            User livreur = userService.findById(livreurId);
            livraisonService.assignLivreur(id, livreur);
            commandeService.updateStatut(id, com.livraison.livreur.model.CommandeStatus.EN_COURS_DE_LIVRAISON);
            return "redirect:/admin/orders?assigned";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/orders?error";
        }
    }

    // --- Espace Livreur ---

    @GetMapping("/livreur/deliveries")
    public String listDeliveries(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        User livreur = userService.findByEmail(principal.getEmail());
        List<com.livraison.livreur.model.Livraison> deliveries = livraisonService.getLivraisonsByLivreur(livreur);
        model.addAttribute("deliveries", deliveries);
        model.addAttribute("username", livreur.getUsername());
        return "livreur/my_deliveries";
    }

    @PostMapping("/livreur/deliveries/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam LivraisonStatus status) {
        livraisonService.updateLivraisonStatut(id, status);
        return "redirect:/livreur/deliveries?updated";
    }

    @GetMapping("/livreur/parametres")
    public String parametres(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("username", principal.getUsername());
        model.addAttribute("email", principal.getUser().getEmail());
        model.addAttribute("telephone", principal.getUser().getTelephone());
        return "livreur/parametres";
    }
}
