package com.hashtag.ngo.example.bank.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;

/**
 * Configuration de la sécurité HTTP.
 * <p>
 * L'API est entièrement "stateless" : aucune session HTTP n'est créée, toute
 * requête vers {@code /accounts/**} doit porter un en-tête
 * {@code Authorization: Bearer <jeton>} validé par
 * {@link JwtAuthenticationFilter}. Le CSRF est désactivé car il ne s'applique
 * qu'aux authentifications par cookie de session, absentes ici.
 * <p>
 * L'utilisateur de démonstration ({@code demo} / {@code demo123}) est défini
 * dans {@link UserDetailsConfig}, une classe séparée : voir la Javadoc de
 * cette classe pour l'explication de ce choix (rupture d'une dépendance
 * circulaire entre cette configuration et {@link JwtAuthenticationFilter}).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Sans entry point explicite, Spring Security renvoie 403 (Forbidden)
                // pour une requête non authentifiée au lieu de 401 (Unauthorized) : on
                // force ici la réponse HTTP standard attendue par un client JWT.
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publics : obtenir un jeton, et consulter la documentation Swagger.
                        .requestMatchers(
                                "/auth/token",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // Tout le reste, notamment /accounts/**, exige un jeton valide.
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Expose le {@link AuthenticationManager} de Spring Security comme bean
     * injectable, nécessaire à {@link AuthController} pour vérifier les
     * identifiants lors de l'obtention d'un jeton.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
