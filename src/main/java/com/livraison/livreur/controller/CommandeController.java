package com.livraison.livreur.controller;

import com.livraison.livreur.model.Commande;
import com.livraison.livreur.model.CommandeStatus;
import com.livraison.livreur.model.TypeColis;
import com.livraison.livreur.model.User;
import com.livraison.livreur.model.UserRole;
import com.livraison.livreur.security.UserPrincipal;
import com.livraison.livreur.service.CommandeService;
import com.livraison.livreur.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CommandeController {

    private final CommandeService commandeService;
    private final UserService userService;

    // --- Espace Client ---

    @GetMapping("/client/orders")
    public String listOrders(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        User user = userService.findByEmail(principal.getEmail());
        List<Commande> orders = commandeService.getClientCommandes(user);
        model.addAttribute("orders", orders);
        model.addAttribute("username", user.getUsername());
        return "client/my_orders";
    }

    @GetMapping("/client/orders/new")
    public String orderForm(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("username", principal.getUsername());
        model.addAttribute("typesColis", TypeColis.values());
        return "client/commande_form";
    }

    @PostMapping("/client/orders/new")
    public String createOrder(@RequestParam String nomExpediteur,
                               @RequestParam String adresseRamassage,
                               @RequestParam String codePostalRamassage,
                               @RequestParam String villeRamassage,
                               @RequestParam String nomDestinataire,
                               @RequestParam String adresseLivraison,
                               @RequestParam String telephoneDestinataire,
                               @RequestParam String codePostalVilleDestinataire,
                               @RequestParam TypeColis typeColis,
                               @RequestParam Double poidsKg,
                               @RequestParam(required = false) Double valeurDeclaree,
                               @AuthenticationPrincipal UserPrincipal principal,
                               Model model) {
        try {
            User user = userService.findByEmail(principal.getEmail());
            commandeService.createCommande(user, nomExpediteur, adresseRamassage, codePostalRamassage,
                    villeRamassage, nomDestinataire, adresseLivraison, telephoneDestinataire,
                    codePostalVilleDestinataire, typeColis, poidsKg, valeurDeclaree);
            return "redirect:/client/espace?success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", principal.getUsername());
            model.addAttribute("typesColis", TypeColis.values());
            return "client/commande_form";
        }
    }

    // --- Espace Admin ---

    @GetMapping("/admin/orders")
    public String manageOrders(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        List<Commande> orders = commandeService.getAllCommandes();
        model.addAttribute("orders", orders);
        model.addAttribute("username", principal.getUsername());
        model.addAttribute("livreurs", userService.findByRole(UserRole.LIVREUR));
        return "admin/manage-orders";
    }

    @PostMapping("/admin/orders/{id}/validate")
    public String validateOrder(@PathVariable Long id) {
        commandeService.updateStatut(id, CommandeStatus.VALIDEE);
        return "redirect:/admin/orders?validated";
    }

    @PostMapping("/admin/orders/{id}/close")
    public String closeOrder(@PathVariable Long id) {
        commandeService.updateStatut(id, CommandeStatus.CLOTUREE);
        return "redirect:/admin/orders?closed";
    }
}
