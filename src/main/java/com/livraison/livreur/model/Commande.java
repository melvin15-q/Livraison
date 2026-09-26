package com.livraison.livreur.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "commandes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(nullable = false)
    private LocalDateTime dateCommande;

    @Column(nullable = false)
    private Double montantTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommandeStatus statut;

    // --- Expéditeur ---
    private String nomExpediteur;
    private String adresseRamassage;
    private String codePostalRamassage;
    private String villeRamassage;

    // --- Destinataire ---
    private String nomDestinataire;
    private String adresseLivraison;
    private String telephoneDestinataire;
    private String codePostalVilleDestinataire;

    // --- Colis ---
    @Enumerated(EnumType.STRING)
    private TypeColis typeColis;
    private Double poidsKg;
    private Double valeurDeclaree;
    private Double distanceKm;
}
