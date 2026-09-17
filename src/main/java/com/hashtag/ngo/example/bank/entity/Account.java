package com.hashtag.ngo.example.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import org.hibernate.annotations.ConcreteProxy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Racine de la hiérarchie des comptes bancaires.
 * <p>
 * Il s'agit d'une classe <b>scellée</b> (Java 17, JEP 409) : le mot-clé
 * {@code permits} énumère exhaustivement les deux seules sous-classes
 * autorisées, {@link CheckingAccount} (compte courant) et
 * {@link SavingsAccount} (compte épargne). Cela garantit, dès la
 * compilation, qu'aucune autre nature de compte ne peut apparaître dans le
 * modèle métier — un avantage par rapport à une classe abstraite classique,
 * ouverte à l'extension par n'importe qui.
 * <p>
 * La stratégie d'héritage JPA choisie est {@code SINGLE_TABLE} : toutes les
 * variantes de compte sont stockées dans une seule table "accounts", une
 * colonne technique "account_type" (le discriminant) indiquant la
 * sous-classe réelle de chaque ligne. C'est la stratégie la plus simple et
 * la plus performante (pas de jointure) pour une hiérarchie à deux niveaux
 * comme celle-ci.
 * <p>
 * Remarque technique : par défaut, Hibernate génère au démarrage un proxy
 * dynamique (sous-classe créée à la volée) pour chaque entité, y compris la
 * racine abstraite d'une hiérarchie, afin de pouvoir représenter une
 * référence non chargée (ex. {@code entityManager.getReference(...)}).
 * Or le JVM interdit qu'une classe scellée soit étendue par une classe hors
 * de la liste {@code permits} : Hibernate échoue donc à créer ce proxy pour
 * {@code Account}. L'annotation {@link ConcreteProxy} indique à Hibernate de
 * toujours utiliser le proxy d'une sous-classe concrète ({@code
 * CheckingAccount}/{@code SavingsAccount}, toutes deux {@code final} — cas
 * que Hibernate sait nativement gérer sans proxy) plutôt que de tenter de
 * sous-classer la racine abstraite elle-même.
 */
@Entity
@Table(name = "accounts")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "account_type", discriminatorType = DiscriminatorType.STRING)
@ConcreteProxy
public abstract sealed class Account permits CheckingAccount, SavingsAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Titulaire du compte. */
    @Column(nullable = false)
    private String owner;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Constructeur protégé requis par la spécification JPA (proxy/réflexion). */
    protected Account() {
    }

    protected Account(String owner, BigDecimal initialBalance) {
        this.owner = owner;
        this.balance = initialBalance;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Vérifie qu'un montant financier est utilisable (dépôt ou retrait).
     * Factorisée ici car la règle est identique pour tous les types de
     * compte ; seule la règle de solde minimal après retrait diffère.
     */
    protected static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "Le montant doit être strictement positif : " + amount);
        }
    }

    /**
     * Dépose un montant sur le compte. Le comportement est identique pour
     * tous les types de compte, il est donc implémenté une seule fois ici.
     */
    public void deposit(BigDecimal amount) {
        validateAmount(amount);
        this.balance = this.balance.add(amount);
    }

    /**
     * Retire un montant du compte. La règle de solde minimal autorisé après
     * retrait dépend du type de compte (découvert pour un compte courant,
     * aucun découvert pour un compte épargne) : chaque sous-classe fournit
     * donc sa propre implémentation.
     */
    public abstract void withdraw(BigDecimal amount);

    /** Type métier du compte (utilisé pour l'exposition dans l'API). */
    public abstract AccountType getType();

    public Long getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    protected void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
