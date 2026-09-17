package com.hashtag.ngo.example.bank.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Point de branchement entre Cucumber et Spring Boot.
 * <p>
 * {@code @CucumberContextConfiguration} indique à cucumber-spring que cette
 * classe porte la configuration du contexte Spring à utiliser pour exécuter
 * les scénarios ; {@code @SpringBootTest} démarre l'application complète
 * (contrôleurs, sécurité, base H2 en mémoire...) sur un port aléatoire, afin
 * que les steps puissent effectuer de vrais appels HTTP via
 * {@code TestRestTemplate}.
 * <p>
 * Cette classe ne doit contenir aucune méthode de test : c'est uniquement
 * un point de configuration, partagé par toutes les classes de step
 * definitions du projet.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {
}
