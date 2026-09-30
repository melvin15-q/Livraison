package com.livraison.livreur.dto;

import com.livraison.livreur.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Un livreur avec son statut de disponibilité calculé en temps réel. */
@Getter
@AllArgsConstructor
public class LivreurStatutItem {
    private final User livreur;
    private final boolean occupe; // true = a une livraison ASSIGNEE ou RECUPEREE en cours
}
