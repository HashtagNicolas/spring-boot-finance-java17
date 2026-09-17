package com.hashtag.ngo.example.bank.entity;

/**
 * Levée lorsqu'un montant de dépôt ou de retrait est invalide (nul, absent
 * ou négatif ou nul). Un montant financier doit toujours être strictement
 * positif.
 */
public class InvalidAmountException extends RuntimeException {

    public InvalidAmountException(String message) {
        super(message);
    }
}
