package com.livraison.livreur.repository;

import com.livraison.livreur.model.Commande;
import com.livraison.livreur.model.Livraison;
import com.livraison.livreur.model.LivraisonStatus;
import com.livraison.livreur.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LivraisonRepository extends JpaRepository<Livraison, Long> {
    List<Livraison> findByLivreur(User livreur);
    Optional<Livraison> findByCommande(Commande commande);
    List<Livraison> findByStatutIn(List<LivraisonStatus> statuts);
    List<Livraison> findByStatut(LivraisonStatus statut);
}
