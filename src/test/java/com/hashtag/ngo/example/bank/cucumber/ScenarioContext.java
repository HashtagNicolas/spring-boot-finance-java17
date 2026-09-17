package com.hashtag.ngo.example.bank.cucumber;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * État partagé entre les différentes étapes (Given/When/Then) d'un même
 * scénario Cucumber : le jeton JWT obtenu, l'identifiant du compte
 * actuellement manipulé, et la dernière réponse HTTP reçue.
 * <p>
 * Comme cette classe est un {@code @Component} Spring situé dans un
 * sous-package du package de base de l'application (scanné par
 * {@code @SpringBootApplication}), cucumber-spring lui applique
 * automatiquement le pseudo-scope "cucumber-glue" : une nouvelle instance
 * est créée pour chaque scénario, évitant toute fuite d'état entre deux
 * scénarios exécutés à la suite.
 */
@Component
public class ScenarioContext {

    private String token;
    private Long currentAccountId;
    private ResponseEntity<String> lastResponse;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getCurrentAccountId() {
        return currentAccountId;
    }

    public void setCurrentAccountId(Long currentAccountId) {
        this.currentAccountId = currentAccountId;
    }

    public ResponseEntity<String> getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(ResponseEntity<String> lastResponse) {
        this.lastResponse = lastResponse;
    }
}
