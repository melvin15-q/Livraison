package com.livraison.livreur.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Après une connexion réussie, redirige l'utilisateur vers son espace :
 * /admin/dashboard (ADMIN), /client/espace (CLIENT), /livreur/deliveries (LIVREUR).
 */
@Component
public class RoleBasedAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {

        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String targetUrl;
        if (authorities.contains("ROLE_ADMIN")) {
            targetUrl = "/admin/dashboard";
        } else if (authorities.contains("ROLE_LIVREUR")) {
            targetUrl = "/livreur/deliveries";
        } else {
            targetUrl = "/client/espace";
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
