package com.livraison.livreur.service;

import com.livraison.livreur.model.Commande;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Envoie des notifications WhatsApp via l'API Meta Cloud (appel HTTP direct, pas de
 * SDK nécessaire). Remplace l'intégration Twilio initiale — Twilio n'a pas d'offre
 * gratuite au Cameroun, alors que l'API Meta est directement accessible en mode test
 * sans carte bancaire.
 *
 * Toutes les notifications partent vers un seul numéro fixe pour l'instant (celui de
 * l'admin/testeur) — voir notification.whatsapp.destinataire dans application.properties.
 *
 * ⚠️ Ne fonctionne qu'une fois meta.whatsapp.phone-number-id et
 * meta.whatsapp.access-token renseignés. Tant qu'ils sont vides, les envois sont
 * simplement affichés dans la console — ça ne bloque jamais la création d'une commande.
 */
@Service
public class WhatsAppNotificationService {

    @Value("${meta.whatsapp.phone-number-id:}")
    private String phoneNumberId;

    @Value("${meta.whatsapp.access-token:}")
    private String accessToken;

    @Value("${notification.whatsapp.destinataire:}")
    private String destinataire;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void notifierNouvelleCommande(Commande commande) {
        String message = String.format(
                "🚚 Nouvelle commande #%d\nClient : %s\nDestinataire : %s\nDe : %s\nÀ : %s\nMontant estimé : %.0f F",
                commande.getId(), commande.getClient().getUsername(), commande.getNomDestinataire(),
                commande.getVilleRamassage(), commande.getCodePostalVilleDestinataire(), commande.getMontantTotal());
        envoyer(message);
    }

    public void notifierLivraisonEffectuee(Commande commande) {
        String message = String.format(
                "✅ Commande #%d livrée\nDestinataire : %s\nMerci d'avoir utilisé Livreur !",
                commande.getId(), commande.getNomDestinataire());
        envoyer(message);
    }

    private void envoyer(String message) {
        if (phoneNumberId.isBlank() || accessToken.isBlank() || destinataire.isBlank()) {
            System.out.println(">> [WhatsApp non configuré] Message qui aurait été envoyé : " + message);
            return;
        }

        try {
            // Numéro au format E.164 sans le "+" pour l'API Meta (ex: 237682754954)
            String to = destinataire.startsWith("+") ? destinataire.substring(1) : destinataire;

            String jsonBody = """
                    {
                      "messaging_product": "whatsapp",
                      "to": "%s",
                      "type": "text",
                      "text": { "body": "%s" }
                    }
                    """.formatted(to, message.replace("\"", "\\\"").replace("\n", "\\n"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://graph.facebook.com/v21.0/" + phoneNumberId + "/messages"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println(">> WhatsApp envoyé avec succès à " + destinataire);
            } else {
                System.err.println(">> Échec de l'envoi WhatsApp (" + response.statusCode() + ") : " + response.body());
            }
        } catch (Exception e) {
            System.err.println(">> Erreur lors de l'envoi WhatsApp : " + e.getMessage());
        }
    }
}
