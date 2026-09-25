package com.livraison.livreur.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username; // Nom complet affiché (maquette "Nom complet") — pas d'unicité requise

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
}
