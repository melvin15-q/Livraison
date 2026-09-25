package com.livraison.livreur.config;

import com.livraison.livreur.security.CustomUserDetailsService;
import com.livraison.livreur.security.RoleBasedAuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final RoleBasedAuthenticationSuccessHandler successHandler;

    /**
     * Encode les mots de passe avec BCrypt : jamais de mot de passe en clair en base.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Ressources publiques : accueil, connexion, inscription, assets statiques
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                // Espace Admin : réservé au rôle ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Espace Client : réservé au rôle CLIENT
                .requestMatchers("/client/**").hasRole("CLIENT")
                // Espace Livreur : réservé au rôle LIVREUR
                .requestMatchers("/livreur/**").hasRole("LIVREUR")
                // Tout le reste nécessite d'être authentifié
                .anyRequest().authenticated()
            )
            .formLogin((FormLoginConfigurer<HttpSecurity> form) -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler)
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("livreurRememberMeKey")
                .rememberMeParameter("remember-me")
                .tokenValiditySeconds(14 * 24 * 60 * 60)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
