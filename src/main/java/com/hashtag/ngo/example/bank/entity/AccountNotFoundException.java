package com.hashtag.ngo.example.bank.entity;

/**
 * Levée lorsqu'aucun compte ne correspond à l'identifiant demandé.
 * <p>
 * Placée dans la couche "entity" (et non "bean") afin de respecter la règle
 * d'architecture vérifiée par ArchUnit : aucune couche ne doit dépendre
 * d'une couche située au-dessus d'elle. En plaçant les exceptions métier au
 * niveau le plus bas, elles restent accessibles aussi bien depuis "bean"
 * que depuis "api" sans créer de dépendance remontante.
 */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String message) {
        super(message);
    }
}
