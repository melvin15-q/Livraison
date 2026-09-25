package com.livraison.livreur.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "livraisons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Livraison {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "commande_id", nullable = false, unique = true)
    private Commande commande;

    @ManyToOne
    @JoinColumn(name = "livreur_id", nullable = false)
    private User livreur;

    @Column(nullable = false)
    private LocalDateTime dateAssignation;

    private LocalDateTime dateLivraison;

    @Column(nullable = false)
    private String adresseLivraison;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LivraisonStatus statut;
}
