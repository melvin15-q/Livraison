package com.livraison.livreur.dto;

import com.livraison.livreur.model.Commande;
import com.livraison.livreur.model.Livraison;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Associe une commande à sa livraison (si elle a déjà été assignée à un livreur). */
@Getter
@AllArgsConstructor
public class SuiviItem {
    private final Commande commande;
    private final Livraison livraison; // peut être null si pas encore assignée
}
