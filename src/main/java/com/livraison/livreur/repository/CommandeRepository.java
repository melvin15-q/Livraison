package com.livraison.livreur.repository;

import com.livraison.livreur.model.Commande;
import com.livraison.livreur.model.CommandeStatus;
import com.livraison.livreur.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {
    List<Commande> findByClient(User client);
    List<Commande> findByClientOrderByDateCommandeDesc(User client);
    List<Commande> findByStatutIn(List<CommandeStatus> statuts);
}
