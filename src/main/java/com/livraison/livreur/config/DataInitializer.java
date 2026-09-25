package com.livraison.livreur.config;

import com.livraison.livreur.model.User;
import com.livraison.livreur.model.UserRole;
import com.livraison.livreur.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crée un compte administrateur par défaut au démarrage, uniquement s'il n'existe
 * pas encore. Permet de tester /admin/orders immédiatement.
 *
 * ⚠️ Identifiants de test — à changer ou supprimer avant toute mise en production.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@livreur.local";
    private static final String ADMIN_PASSWORD = "admin123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
            User admin = User.builder()
                    .username("Administrateur")
                    .email(ADMIN_EMAIL)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .telephone("690000000")
                    .role(UserRole.ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println(">> Compte admin créé : email=" + ADMIN_EMAIL + " / password=" + ADMIN_PASSWORD);
        }
    }
}
