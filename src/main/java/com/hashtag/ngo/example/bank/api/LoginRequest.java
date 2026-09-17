package com.hashtag.ngo.example.bank.api;

/** Corps de requête pour l'obtention d'un jeton JWT. */
public record LoginRequest(String username, String password) {
}
