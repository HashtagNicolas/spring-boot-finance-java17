package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.entity.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Représentation d'un compte renvoyée par l'API.
 * <p>
 * {@code overdraftLimit} vaut {@code null} pour un compte épargne, et
 * {@code interestRate} vaut {@code null} pour un compte courant : seul le
 * champ pertinent pour le type de compte est renseigné.
 */
public record AccountResponse(
        Long id,
        String owner,
        AccountType type,
        BigDecimal balance,
        BigDecimal overdraftLimit,
        BigDecimal interestRate,
        LocalDateTime createdAt
) {
}
