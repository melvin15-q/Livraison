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

    // Barème par distance — fourni par Daril
    private static final double TARIF_0_2KM = 1000.0;
    private static final double TARIF_2_5KM = 1500.0;
    private static final double TARIF_5_8KM = 2000.0;
    private static final double TARIF_8_12KM = 2500.0;
    private static final double TARIF_PAR_KM_AU_DELA = 200.0; // au-delà de 12km, non couvert par le barème fourni — extrapolation à ajuster

    // Supplément poids, ajouté par-dessus le tarif distance (hypothèse à valider : le barème fourni ne précise pas ce point)
    private static final double SEUIL_POIDS_GRATUIT_KG = 2.0;
    private static final double SURCHARGE_PAR_KG = 200.0;

    private final CommandeRepository commandeRepository;
    private final WhatsAppNotificationService whatsAppNotificationService;

    /**
     * Frais de base selon la distance, d'après le barème :
     * 0–2km: 1000F · 2–5km: 1500F · 5–8km: 2000F · 8–12km: 2500F
     * Au-delà de 12km (non couvert par le barème), extrapolation à +200F/km — à ajuster si besoin.
     */
    private double fraisDistance(double distanceKm) {
        if (distanceKm <= 2) return TARIF_0_2KM;
        if (distanceKm <= 5) return TARIF_2_5KM;
        if (distanceKm <= 8) return TARIF_5_8KM;
        if (distanceKm <= 12) return TARIF_8_12KM;
        return TARIF_8_12KM + (distanceKm - 12) * TARIF_PAR_KM_AU_DELA;
    }

    /**
     * Estimation tarifaire = frais de distance (barème) + supplément au poids
     * au-delà de 2kg. Le supplément poids est une hypothèse de ma part, le
     * barème fourni ne couvrant que la distance — à confirmer/ajuster.
     */
    public double estimerTarif(Double distanceKm, Double poidsKg) {
        double distance = distanceKm == null ? 0 : distanceKm;
        double poids = poidsKg == null ? 0 : poidsKg;
        double surchargePoids = Math.max(0, poids - SEUIL_POIDS_GRATUIT_KG) * SURCHARGE_PAR_KG;
        return fraisDistance(distance) + surchargePoids;
    }

    @Transactional
    public Commande createCommande(User client,
                                    String nomExpediteur, String adresseRamassage,
                                    String codePostalRamassage, String villeRamassage,
                                    String nomDestinataire, String adresseLivraison,
                                    String telephoneDestinataire, String codePostalVilleDestinataire,
                                    TypeColis typeColis, Double poidsKg, Double valeurDeclaree,
                                    Double distanceKm) {

        Commande commande = Commande.builder()
                .client(client)
                .dateCommande(LocalDateTime.now())
                .montantTotal(estimerTarif(distanceKm, poidsKg))
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
                .distanceKm(distanceKm)
                .build();
        Commande saved = commandeRepository.save(commande);

        whatsAppNotificationService.notifierNouvelleCommande(saved);

        return saved;
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
