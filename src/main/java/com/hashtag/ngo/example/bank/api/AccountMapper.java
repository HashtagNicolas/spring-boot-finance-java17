package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.entity.Account;
import com.hashtag.ngo.example.bank.entity.CheckingAccount;
import com.hashtag.ngo.example.bank.entity.SavingsAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

/**
 * Mapper MapStruct entre l'entité {@link Account} (et ses sous-classes) et
 * les DTO de l'API.
 * <p>
 * {@code componentModel = "spring"} demande à MapStruct de générer
 * l'implémentation ({@code AccountMapperImpl}) sous forme de {@code @Bean}
 * Spring, injectable comme n'importe quel autre service.
 * <p>
 * Les champs communs (id, owner, balance, createdAt) sont mappés
 * automatiquement par MapStruct grâce à la correspondance des noms. Les
 * champs spécifiques à une sous-classe ({@code overdraftLimit},
 * {@code interestRate}) sont calculés par des méthodes par défaut utilisant
 * le <i>pattern matching pour instanceof</i> (Java 17, JEP 394) : plus
 * besoin de "cast" explicite après le test de type.
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "type", expression = "java(account.getType())")
    @Mapping(target = "overdraftLimit", expression = "java(extractOverdraftLimit(account))")
    @Mapping(target = "interestRate", expression = "java(extractInterestRate(account))")
    AccountResponse toResponse(Account account);

    default BigDecimal extractOverdraftLimit(Account account) {
        return account instanceof CheckingAccount checking ? checking.getOverdraftLimit() : null;
    }

    default BigDecimal extractInterestRate(Account account) {
        return account instanceof SavingsAccount savings ? savings.getInterestRate() : null;
    }
}
