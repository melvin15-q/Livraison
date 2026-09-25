package com.livraison.livreur.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Une ligne du flux "Dernières livraisons" du tableau de bord admin. */
@Getter
@AllArgsConstructor
public class ActivityItem {
    private final Long id;
    private final String client;
    private final String livreur;
    private final String zone;
    private final String statut;
}
