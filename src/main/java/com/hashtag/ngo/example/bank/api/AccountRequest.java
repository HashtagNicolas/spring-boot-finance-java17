package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.entity.AccountType;

import java.math.BigDecimal;

/**
 * Corps de la requête de création d'un compte.
 * <p>
 * {@code overdraftLimit} n'a de sens que pour un compte {@code COURANT} et
 * {@code interestRate} que pour un compte {@code EPARGNE} ; le champ non
 * pertinent est simplement ignoré (une valeur par défaut de zéro est
 * appliquée côté service).
 *
 * @param owner          titulaire du compte
 * @param type           type de compte à créer
 * @param initialBalance solde initial du compte
 * @param overdraftLimit découvert autorisé (compte courant uniquement)
 * @param interestRate   taux d'intérêt annuel, ex. 0.02 pour 2 % (compte épargne uniquement)
 */
public record AccountRequest(
        String owner,
        AccountType type,
        BigDecimal initialBalance,
        BigDecimal overdraftLimit,
        BigDecimal interestRate
) {
}
