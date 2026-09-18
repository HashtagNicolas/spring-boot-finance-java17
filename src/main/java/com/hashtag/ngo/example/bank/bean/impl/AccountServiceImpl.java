package com.hashtag.ngo.example.bank.bean.impl;

import com.hashtag.ngo.example.bank.bean.AccountService;
import com.hashtag.ngo.example.bank.entity.Account;
import com.hashtag.ngo.example.bank.entity.AccountNotFoundException;
import com.hashtag.ngo.example.bank.entity.AccountRepository;
import com.hashtag.ngo.example.bank.entity.AccountType;
import com.hashtag.ngo.example.bank.entity.CheckingAccount;
import com.hashtag.ngo.example.bank.entity.SavingsAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation de {@link AccountService}.
 * <p>
 * Injection par interface : ce service dépend de {@link AccountRepository}
 * (interface Spring Data), jamais d'une implémentation concrète.
 */
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public Account createAccount(String owner, AccountType type, BigDecimal initialBalance,
                                  BigDecimal overdraftLimit, BigDecimal interestRate) {
        // "switch expression" (Java 17) : exhaustif sur l'enum AccountType,
        // sans "default" nécessaire puisque toutes les valeurs sont couvertes.
        Account account = switch (type) {
            case COURANT -> new CheckingAccount(
                    owner, initialBalance, Optional.ofNullable(overdraftLimit).orElse(BigDecimal.ZERO));
            case EPARGNE -> new SavingsAccount(
                    owner, initialBalance, Optional.ofNullable(interestRate).orElse(BigDecimal.ZERO));
        };
        return accountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccount(Long id) {
        return findOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    @Transactional
    public Account deposit(Long id, BigDecimal amount) {
        Account account = findOrThrow(id);
        account.deposit(amount);
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public Account withdraw(Long id, BigDecimal amount) {
        Account account = findOrThrow(id);
        account.withdraw(amount);
        return accountRepository.save(account);
    }

    private Account findOrThrow(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Compte introuvable : " + id));
    }
}
