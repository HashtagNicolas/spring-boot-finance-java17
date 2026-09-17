package com.hashtag.ngo.example.bank.entity;

/**
 * Levée par {@link Account#withdraw(java.math.BigDecimal)} lorsqu'un retrait
 * ferait passer le solde en dessous de la limite autorisée pour ce type de
 * compte (zéro pour un compte épargne, le découvert autorisé pour un compte
 * courant).
 */
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}
