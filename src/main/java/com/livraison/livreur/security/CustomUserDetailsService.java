package com.livraison.livreur.security;

import com.livraison.livreur.model.User;
import com.livraison.livreur.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Point de jonction entre Spring Security et PostgreSQL : à chaque tentative
 * de connexion, Spring Security appelle loadUserByUsername avec la valeur
 * saisie dans le champ "email" du formulaire de login (voir SecurityConfig).
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Aucun compte pour cet email : " + email));
        return new UserPrincipal(user);
    }
}
