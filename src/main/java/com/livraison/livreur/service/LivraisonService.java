package com.livraison.livreur.service;

import com.livraison.livreur.model.Commande;
import com.livraison.livreur.model.Livraison;
import com.livraison.livreur.model.LivraisonStatus;
import com.livraison.livreur.model.User;
import com.livraison.livreur.repository.CommandeRepository;
import com.livraison.livreur.repository.LivraisonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LivraisonService {

    private final LivraisonRepository livraisonRepository;
    private final CommandeRepository commandeRepository;
    private final WhatsAppNotificationService whatsAppNotificationService;

    /**
     * Assigne un livreur à une commande validée. L'adresse de livraison est reprise
     * directement de la commande (renseignée par le client à la création) — plus besoin
     * de la ressaisir côté admin.
     */
    @Transactional
    public Livraison assignLivreur(Long commandeId, User livreur) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));

        if (commande.getStatut() != com.livraison.livreur.model.CommandeStatus.VALIDEE) {
            throw new IllegalArgumentException("La commande doit être validée avant d'être assignée à un livreur.");
        }

        String adresse = commande.getAdresseLivraison() != null
                ? commande.getAdresseLivraison()
                : "Adresse non renseignée";

        Livraison livraison = Livraison.builder()
                .commande(commande)
                .livreur(livreur)
                .adresseLivraison(adresse)
                .dateAssignation(LocalDateTime.now())
                .statut(LivraisonStatus.ASSIGNEE)
                .build();

        return livraisonRepository.save(livraison);
    }

    public List<Livraison> getLivraisonsByLivreur(User livreur) {
        return livraisonRepository.findByLivreur(livreur);
    }

    public List<Livraison> getLivraisonsEnCours() {
        return livraisonRepository.findByStatutIn(
                List.of(LivraisonStatus.ASSIGNEE, LivraisonStatus.RECUPEREE));
    }

    public List<Livraison> getLivraisonsTerminees() {
        return livraisonRepository.findByStatut(LivraisonStatus.LIVREE);
    }

    @Transactional
    public void updateLivraisonStatut(Long id, LivraisonStatus status) {
        Livraison livraison = livraisonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livraison non trouvée"));
        livraison.setStatut(status);

        if (status == LivraisonStatus.LIVREE) {
            livraison.setDateLivraison(LocalDateTime.now());

            // Répercute sur la commande : c'est ce statut que le client voit
            // dans son espace et son historique.
            Commande commande = livraison.getCommande();
            commande.setStatut(com.livraison.livreur.model.CommandeStatus.LIVREE);
            commandeRepository.save(commande);

            whatsAppNotificationService.notifierLivraisonEffectuee(commande);
        }

        livraisonRepository.save(livraison);
    }

    public Optional<Livraison> getLivraisonByCommande(Commande commande) {
        return livraisonRepository.findByCommande(commande);
    }
}
