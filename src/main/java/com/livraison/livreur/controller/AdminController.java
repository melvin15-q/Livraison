package com.livraison.livreur.controller;

import com.livraison.livreur.dto.SuiviItem;
import com.livraison.livreur.model.LivraisonStatus;
import com.livraison.livreur.model.UserRole;
import com.livraison.livreur.security.UserPrincipal;
import com.livraison.livreur.service.AdminDashboardService;
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
public class AdminController {

    private final AdminDashboardService dashboardService;
    private final CommandeService commandeService;
    private final LivraisonService livraisonService;
    private final UserService userService;

    @GetMapping("/admin/dashboard")
    public String dashboard(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("username", principal.getUsername());
        model.addAttribute("totalCommandes", dashboardService.totalCommandes());
        model.addAttribute("commandesEnCours", dashboardService.commandesEnCours());
        model.addAttribute("commandesTerminees", dashboardService.commandesTerminees());
        model.addAttribute("totalLivreurs", dashboardService.totalLivreurs());
        model.addAttribute("livreursActifs", dashboardService.livreursActifs());
        model.addAttribute("totalClients", dashboardService.totalClients());
        model.addAttribute("revenuMoisCourant", dashboardService.revenuMoisCourant());
        model.addAttribute("dernieresLivraisons", dashboardService.dernieresLivraisons(6));
        model.addAttribute("livreurs", dashboardService.livreursAvecStatut());
        return "admin/dashboard";
    }

    @GetMapping("/admin/suivi")
    public String suivi(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("username", principal.getUsername());

        List<SuiviItem> items = commandeService.getCommandesEnCours().stream()
                .map(c -> new SuiviItem(c, livraisonService.getLivraisonByCommande(c).orElse(null)))
                .toList();
        model.addAttribute("items", items);

        return "admin/suivi";
    }

    @PostMapping("/admin/suivi/{livraisonId}/status")
    public String updateLivraisonStatus(@PathVariable Long livraisonId,
                                         @RequestParam LivraisonStatus status) {
        livraisonService.updateLivraisonStatut(livraisonId, status);
        return "redirect:/admin/suivi?updated";
    }

    @GetMapping("/admin/historique")
    public String historique(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("username", principal.getUsername());
        model.addAttribute("commandes", commandeService.getCommandesTerminees());
        return "admin/historique";
    }

    @GetMapping("/admin/parametres")
    public String parametres(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("username", principal.getUsername());
        model.addAttribute("email", principal.getUser().getEmail());
        model.addAttribute("telephone", principal.getUser().getTelephone());
        model.addAttribute("livreurs", dashboardService.livreursAvecStatut());
        return "admin/parametres";
    }

    @PostMapping("/admin/parametres/livreurs")
    public String createLivreur(@RequestParam String nomComplet,
                                 @RequestParam String email,
                                 @RequestParam String password,
                                 @RequestParam String telephone,
                                 @AuthenticationPrincipal UserPrincipal principal,
                                 Model model) {
        try {
            userService.registerLivreur(nomComplet, email, password, telephone);
            return "redirect:/admin/parametres?livreurCree";
        } catch (Exception e) {
            model.addAttribute("username", principal.getUsername());
            model.addAttribute("email", principal.getUser().getEmail());
            model.addAttribute("telephone", principal.getUser().getTelephone());
            model.addAttribute("livreurs", dashboardService.livreursAvecStatut());
            model.addAttribute("errorLivreur", e.getMessage());
            return "admin/parametres";
        }
    }
}
