package com.livraison.livreur.service;

import com.livraison.livreur.dto.ActivityItem;
import com.livraison.livreur.model.Commande;
import com.livraison.livreur.model.Livraison;
import com.livraison.livreur.model.UserRole;
import com.livraison.livreur.repository.CommandeRepository;
import com.livraison.livreur.repository.LivraisonRepository;
import com.livraison.livreur.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final CommandeRepository commandeRepository;
    private final LivraisonRepository livraisonRepository;
    private final UserRepository userRepository;

    public long totalCommandes() {
        return commandeRepository.count();
    }

    public long commandesEnCours() {
        return commandeRepository.findByStatutIn(
                List.of(com.livraison.livreur.model.CommandeStatus.EN_COURS_DE_LIVRAISON,
                        com.livraison.livreur.model.CommandeStatus.VALIDEE)).size();
    }

    public long commandesTerminees() {
        return commandeRepository.findByStatutIn(
                List.of(com.livraison.livreur.model.CommandeStatus.LIVREE)).size();
    }

    public long totalLivreurs() {
        return userRepository.countByRole(UserRole.LIVREUR);
    }

    public long livreursActifs() {
        // "Actif" = a au moins une livraison en cours (ASSIGNEE ou RECUPEREE)
        return livraisonRepository.findByStatutIn(
                        List.of(com.livraison.livreur.model.LivraisonStatus.ASSIGNEE,
                                com.livraison.livreur.model.LivraisonStatus.RECUPEREE))
                .stream()
                .map(l -> l.getLivreur().getId())
                .distinct()
                .count();
    }

    public long totalClients() {
        return userRepository.countByRole(UserRole.CLIENT);
    }

    /** Revenu cumulé des commandes livrées ce mois-ci. */
    public double revenuMoisCourant() {
        LocalDateTime debutMois = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        return commandeRepository.findByStatutIn(List.of(com.livraison.livreur.model.CommandeStatus.LIVREE))
                .stream()
                .filter(c -> c.getDateCommande().isAfter(debutMois))
                .mapToDouble(Commande::getMontantTotal)
                .sum();
    }

    /** Les commandes les plus récentes, avec le livreur assigné si disponible. */
    public List<ActivityItem> dernieresLivraisons(int limite) {
        List<ActivityItem> items = new ArrayList<>();

        List<Commande> recentes = commandeRepository.findAll().stream()
                .sorted(Comparator.comparing(Commande::getDateCommande).reversed())
                .limit(limite)
                .toList();

        for (Commande c : recentes) {
            String livreurNom = "— En attente";
            var livraisonOpt = livraisonRepository.findByCommande(c);
            if (livraisonOpt.isPresent()) {
                livreurNom = livraisonOpt.get().getLivreur().getUsername();
            }
            String zone = c.getCodePostalVilleDestinataire() != null
                    ? c.getCodePostalVilleDestinataire()
                    : "Zone non renseignée";

            items.add(new ActivityItem(c.getId(), c.getClient().getUsername(), livreurNom, zone, c.getStatut().name()));
        }
        return items;
    }

    public List<com.livraison.livreur.model.User> livreursDisponibles() {
        return userRepository.findByRole(UserRole.LIVREUR);
    }
}
