package com.hashtag.ngo.example.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de l'application Spring Boot.
 * <p>
 * {@code @SpringBootApplication} active le scan des composants dans le
 * package {@code com.hashtag.ngo.example.bank} et ses sous-packages
 * ({@code entity}, {@code bean}, {@code api}), l'auto-configuration Spring
 * Boot (Tomcat embarqué, JPA/Hibernate, Spring Security, springdoc...) et la
 * configuration par annotations.
 */
@SpringBootApplication
public class BankApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankApplication.class, args);
    }
}
