package com.hashtag.ngo.example.bank.api;

import java.time.LocalDateTime;

/**
 * Format uniforme des réponses d'erreur de l'API, produit par
 * {@link GlobalExceptionHandler}.
 *
 * @param timestamp date/heure de l'erreur
 * @param status    code de statut HTTP (ex. 404, 400, 500)
 * @param code      code d'erreur applicatif stable, utilisable par un client (ex. "COMPTE_INTROUVABLE")
 * @param message   message lisible décrivant l'erreur
 */
public record ErrorResponse(LocalDateTime timestamp, int status, String code, String message) {
}
