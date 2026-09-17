package com.hashtag.ngo.example.bank.entity;

/**
 * Type d'un compte bancaire.
 * <p>
 * Utilisé à la fois comme discriminant JPA (colonne "account_type") et
 * comme valeur exposée dans l'API pour indiquer au client la nature du
 * compte manipulé.
 */
public enum AccountType {

    /** Compte courant : autorise un découvert jusqu'à une limite fixée. */
    COURANT,

    /** Compte épargne : pas de découvert, mais produit des intérêts. */
    EPARGNE
}
