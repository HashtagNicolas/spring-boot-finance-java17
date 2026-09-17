package com.hashtag.ngo.example.bank.api;

/** Réponse contenant le jeton JWT à utiliser pour les appels authentifiés. */
public record TokenResponse(String token) {
}
