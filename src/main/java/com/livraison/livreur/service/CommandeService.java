package com.livraison.livreur.service;

import com.livraison.livreur.model.Commande;
import com.livraison.livreur.model.CommandeStatus;
import com.livraison.livreur.model.TypeColis;
import com.livraison.livreur.model.User;
import com.livraison.livreur.repository.CommandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommandeService {

    private static final double TARIF_BASE = 1000.0;      // FCFA
    private static final double TARIF_PAR_KG = 300.0;      // FCFA / kg au-delà de 1kg

    private final CommandeRepository commandeRepository;

    /**
     * Calcule une estimation tarifaire simple à partir du poids déclaré.
     * Formule volontairement basique (tarif de base + surcharge au kg) : à affiner
     * plus tard avec une vraie grille tarifaire (distance, zone, type de colis...).
     */
    public double estimerTarif(Double poidsKg) {
        double poids = poidsKg == null ? 1.0 : poidsKg;
        double surcharge = Math.max(0, poids - 1.0) * TARIF_PAR_KG;
        return TARIF_BASE + surcharge;
    }

    @Transactional
    public Commande createCommande(User client,
                                    String nomExpediteur, String adresseRamassage,
                                    String codePostalRamassage, String villeRamassage,
                                    String nomDestinataire, String adresseLivraison,
                                    String telephoneDestinataire, String codePostalVilleDestinataire,
                                    TypeColis typeColis, Double poidsKg, Double valeurDeclaree) {

        Commande commande = Commande.builder()
                .client(client)
                .dateCommande(LocalDateTime.now())
                .montantTotal(estimerTarif(poidsKg))
                .statut(CommandeStatus.EN_ATTENTE)
                .nomExpediteur(nomExpediteur)
                .adresseRamassage(adresseRamassage)
                .codePostalRamassage(codePostalRamassage)
                .villeRamassage(villeRamassage)
                .nomDestinataire(nomDestinataire)
                .adresseLivraison(adresseLivraison)
                .telephoneDestinataire(telephoneDestinataire)
                .codePostalVilleDestinataire(codePostalVilleDestinataire)
                .typeColis(typeColis)
                .poidsKg(poidsKg)
                .valeurDeclaree(valeurDeclaree)
                .build();
        return commandeRepository.save(commande);
    }

    public List<Commande> getClientCommandes(User client) {
        return commandeRepository.findByClientOrderByDateCommandeDesc(client);
    }

    public List<Commande> getAllCommandes() {
        return commandeRepository.findAll();
    }

    public List<Commande> getCommandesEnCours() {
        return commandeRepository.findByStatutIn(
                List.of(CommandeStatus.VALIDEE, CommandeStatus.EN_COURS_DE_LIVRAISON));
    }

    public List<Commande> getCommandesTerminees() {
        return commandeRepository.findByStatutIn(
                List.of(CommandeStatus.LIVREE, CommandeStatus.ANNULEE));
    }

    @Transactional
    public void updateStatut(Long id, CommandeStatus status) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));
        commande.setStatut(status);
        commandeRepository.save(commande);
    }

    public Commande getCommandeById(Long id) {
        return commandeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));
    }
}
