package com.hashtag.ngo.example.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

/**
 * Compte épargne ("CompteEpargne").
 * <p>
 * Aucun découvert n'est autorisé : le solde ne peut jamais devenir négatif.
 * En contrepartie, le compte produit des intérêts au taux annuel
 * {@link #interestRate} (ex. {@code 0.02} pour 2 %), calculés par
 * {@link #applyInterest()}.
 */
@Entity
@DiscriminatorValue("EPARGNE")
public final class SavingsAccount extends Account {

    // Nullable : avec l'héritage SINGLE_TABLE, cette colonne est partagée par
    // toutes les lignes de la table "accounts", y compris celles des comptes
    // courants (CheckingAccount) pour lesquelles elle n'a pas de sens.
    @Column(name = "interest_rate", precision = 6, scale = 4)
    private BigDecimal interestRate;

    protected SavingsAccount() {
        // Constructeur requis par JPA.
    }

    public SavingsAccount(String owner, BigDecimal initialBalance, BigDecimal interestRate) {
        super(owner, initialBalance);
        this.interestRate = interestRate;
    }

    @Override
    public void withdraw(BigDecimal amount) {
        validateAmount(amount);
        if (getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    "Retrait refusé : solde insuffisant sur le compte épargne %s".formatted(getId()));
        }
        setBalance(getBalance().subtract(amount));
    }

    /**
     * Crédite le compte des intérêts calculés sur le solde courant
     * (solde × taux annuel). Réutilise {@link Account#deposit(BigDecimal)}
     * pour bénéficier de la même validation de montant.
     */
    public void applyInterest() {
        BigDecimal interest = getBalance().multiply(interestRate);
        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            deposit(interest);
        }
    }

    @Override
    public AccountType getType() {
        return AccountType.EPARGNE;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }
}
