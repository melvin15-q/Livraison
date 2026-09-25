package com.livraison.livreur.service;

import com.livraison.livreur.model.User;
import com.livraison.livreur.model.UserRole;
import com.livraison.livreur.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
    }

    public java.util.List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * Inscrit un nouveau client. Le rôle est toujours forcé à CLIENT ici :
     * un compte ADMIN ne doit jamais pouvoir être créé depuis un formulaire public.
     * L'email est l'identifiant unique de connexion ; le nom complet n'a pas besoin
     * d'être unique (deux personnes peuvent porter le même nom).
     */
    public User registerClient(String nomComplet, String email, String rawPassword, String telephone) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }

        User user = User.builder()
                .username(nomComplet)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .telephone(telephone)
                .role(UserRole.CLIENT)
                .build();

        return userRepository.save(user);
    }

    /**
     * Crée un compte livreur — réservé à l'admin (aucun formulaire public d'inscription
     * livreur, contrairement au client). Même contrainte d'unicité sur l'email.
     */
    public User registerLivreur(String nomComplet, String email, String rawPassword, String telephone) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }

        User user = User.builder()
                .username(nomComplet)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .telephone(telephone)
                .role(UserRole.LIVREUR)
                .build();

        return userRepository.save(user);
    }
}
