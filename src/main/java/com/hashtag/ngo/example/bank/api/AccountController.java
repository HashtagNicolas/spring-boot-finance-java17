package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.bean.AccountService;
import com.hashtag.ngo.example.bank.entity.Account;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Contrôleur REST exposant la gestion des comptes bancaires.
 * <p>
 * Convention de nommage du projet : les contrôleurs se terminent par
 * "Controller" (vérifié par ArchUnit). Ce contrôleur est le seul endroit de
 * l'application où l'on convertit entre l'entité {@link Account} (couche
 * "entity", manipulée par la couche "bean") et les DTO {@link AccountRequest}
 * / {@link AccountResponse} (couche "api"), via {@link AccountMapper}.
 */
@RestController
@RequestMapping("/accounts")
@Tag(name = "Comptes", description = "Création et opérations sur les comptes bancaires")
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public AccountController(AccountService accountService, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    @PostMapping
    @Operation(summary = "Créer un compte", description = "Crée un compte courant ou un compte épargne.")
    public ResponseEntity<AccountResponse> create(@RequestBody AccountRequest request) {
        Account account = accountService.createAccount(
                request.owner(), request.type(), request.initialBalance(),
                request.overdraftLimit(), request.interestRate());
        return ResponseEntity.status(HttpStatus.CREATED).body(accountMapper.toResponse(account));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter un compte par son identifiant")
    public AccountResponse get(@PathVariable Long id) {
        return accountMapper.toResponse(accountService.getAccount(id));
    }

    @GetMapping
    @Operation(summary = "Lister tous les comptes")
    public List<AccountResponse> getAll() {
        return accountService.getAllAccounts().stream().map(accountMapper::toResponse).toList();
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Déposer un montant sur un compte")
    public AccountResponse deposit(@PathVariable Long id, @RequestBody AmountRequest request) {
        return accountMapper.toResponse(accountService.deposit(id, request.amount()));
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Retirer un montant d'un compte")
    public AccountResponse withdraw(@PathVariable Long id, @RequestBody AmountRequest request) {
        return accountMapper.toResponse(accountService.withdraw(id, request.amount()));
    }
}
