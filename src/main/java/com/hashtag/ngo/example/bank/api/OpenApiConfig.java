package com.hashtag.ngo.example.bank.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Déclaration globale de la documentation OpenAPI (springdoc), exposée en
 * JSON sur {@code /v3/api-docs} et sous forme d'interface graphique sur
 * {@code /swagger-ui.html}.
 * <p>
 * Le schéma de sécurité {@code bearerAuth} fait apparaître le bouton
 * "Authorize" dans Swagger UI : il suffit d'y coller le jeton obtenu via
 * {@code POST /auth/token} (sans le préfixe "Bearer", ajouté automatiquement)
 * pour que tous les appels suivants depuis Swagger UI l'incluent.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "API de comptes bancaires",
                version = "1.0.0",
                description = """
                        API pédagogique de gestion de comptes bancaires (Spring Boot 4.1 / Java 17).

                        Pour tester les endpoints protégés :
                        1. Authentifiez-vous via POST /auth/token (utilisateur de démonstration : demo / demo123).
                        2. Copiez le jeton reçu dans la réponse.
                        3. Cliquez sur le bouton "Authorize" ci-dessus et collez le jeton (sans le préfixe "Bearer").
                        """
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
