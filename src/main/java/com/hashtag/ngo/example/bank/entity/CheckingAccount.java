package com.hashtag.ngo.example.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

/**
 * Compte courant ("CompteCourant").
 * <p>
 * Autorise un découvert jusqu'à {@link #overdraftLimit} : le solde peut donc
 * devenir négatif, mais pas en dessous de {@code -overdraftLimit}.
 */
@Entity
@DiscriminatorValue("COURANT")
public final class CheckingAccount extends Account {

    // Nullable : avec l'héritage SINGLE_TABLE, cette colonne est partagée par
    // toutes les lignes de la table "accounts", y compris celles des comptes
    // épargne (SavingsAccount) pour lesquelles elle n'a pas de sens.
    @Column(name = "overdraft_limit", precision = 19, scale = 2)
    private BigDecimal overdraftLimit;

    protected CheckingAccount() {
        // Constructeur requis par JPA.
    }

    public CheckingAccount(String owner, BigDecimal initialBalance, BigDecimal overdraftLimit) {
        super(owner, initialBalance);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public void withdraw(BigDecimal amount) {
        validateAmount(amount);
        BigDecimal balanceAfterWithdrawal = getBalance().subtract(amount);
        BigDecimal lowestAllowedBalance = overdraftLimit.negate();
        if (balanceAfterWithdrawal.compareTo(lowestAllowedBalance) < 0) {
            throw new InsufficientFundsException(
                    "Retrait refusé : le découvert autorisé (%s) serait dépassé pour le compte %s"
                            .formatted(overdraftLimit, getId()));
        }
        setBalance(balanceAfterWithdrawal);
    }

    @Override
    public AccountType getType() {
        return AccountType.COURANT;
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }
}
