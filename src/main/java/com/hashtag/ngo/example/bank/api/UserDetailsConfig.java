package com.hashtag.ngo.example.bank.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Déclare l'utilisateur de démonstration en mémoire ({@code demo} /
 * {@code demo123}) et l'encodeur de mot de passe associé.
 * <p>
 * Séparée de {@link SecurityConfig} volontairement : {@link SecurityConfig}
 * a besoin d'injecter {@link JwtAuthenticationFilter} dans son constructeur,
 * et {@link JwtAuthenticationFilter} a lui-même besoin de
 * {@link UserDetailsService}. Si ce bean était défini par une méthode
 * {@code @Bean} de {@code SecurityConfig}, Spring devrait construire
 * l'instance de {@code SecurityConfig} pour pouvoir l'invoquer — mais cette
 * construction exige déjà {@code JwtAuthenticationFilter}, qui exige à son
 * tour {@code UserDetailsService} : une dépendance circulaire. Isoler ces
 * beans dans une classe de configuration indépendante rompt ce cycle.
 */
@Configuration
public class UserDetailsConfig {

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails demoUser = User.withUsername("demo")
                .password(passwordEncoder.encode("demo123"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(demoUser);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
