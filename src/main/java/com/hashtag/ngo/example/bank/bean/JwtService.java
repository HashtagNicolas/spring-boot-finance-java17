package com.hashtag.ngo.example.bank.bean;

/**
 * Service de gestion des jetons JWT (JSON Web Token).
 * <p>
 * Volontairement découplé de tout contrôleur ou filtre HTTP : ce service ne
 * connaît ni {@code HttpServletRequest} ni Spring MVC. Il peut donc être
 * réutilisé tel quel pour générer ou valider un jeton dans un tout autre
 * contexte, par exemple un client qui aurait besoin d'appeler d'autres
 * microservices avec un jeton signé par cette même application.
 */
public interface JwtService {

    /** Génère un nouveau jeton signé pour le sujet donné (ex. un nom d'utilisateur). */
    String generateToken(String subject);

    /** Indique si le jeton est structurellement valide, signé correctement et non expiré. */
    boolean validateToken(String token);

    /** Extrait le sujet (ex. nom d'utilisateur) porté par un jeton valide. */
    String extractSubject(String token);
}
