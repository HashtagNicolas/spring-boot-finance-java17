package com.hashtag.ngo.example.bank.bean;

import com.hashtag.ngo.example.bank.entity.Account;
import com.hashtag.ngo.example.bank.entity.AccountType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Logique métier de gestion des comptes bancaires.
 * <p>
 * Convention de nommage du projet : les interfaces de la couche "bean" se
 * terminent par "Service" et leur implémentation par "ServiceImpl" (voir
 * {@link AccountServiceImpl}) — vérifié automatiquement par ArchUnit.
 * <p>
 * Choix d'architecture : cette interface manipule directement l'entité
 * {@link Account} plutôt que des DTO comme {@code AccountRequest}/
 * {@code AccountResponse}. En effet, ces DTO (ainsi que le mapper MapStruct)
 * appartiennent à la couche "api" ; si le service en dépendait, on créerait
 * une dépendance remontante bean -> api, interdite par la règle
 * d'architecture en couches vérifiée par ArchUnit
 * ({@code ArchitectureTest}). La conversion entité <-> DTO est donc réalisée
 * à la frontière de l'API, dans {@code AccountController}.
 */
public interface AccountService {

    Account createAccount(String owner, AccountType type, BigDecimal initialBalance,
                           BigDecimal overdraftLimit, BigDecimal interestRate);

    Account getAccount(Long id);

    List<Account> getAllAccounts();

    Account deposit(Long id, BigDecimal amount);

    Account withdraw(Long id, BigDecimal amount);
}
