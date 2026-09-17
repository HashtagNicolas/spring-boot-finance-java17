package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.bean.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre exécuté une seule fois par requête (voir {@link OncePerRequestFilter}),
 * chargé de lire l'en-tête {@code Authorization: Bearer <jeton>}, de le
 * valider via {@link JwtService} et, s'il est valide, de peupler le
 * {@link SecurityContextHolder} afin que Spring Security considère la
 * requête comme authentifiée pour le reste de la chaîne de filtres.
 * <p>
 * Ce filtre ne rejette jamais directement une requête : en l'absence de
 * jeton valide, il laisse simplement la requête continuer sans
 * authentification, et c'est la règle {@code anyRequest().authenticated()}
 * de {@link SecurityConfig} qui se chargera de renvoyer un 401 si
 * l'endpoint appelé exige d'être authentifié.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());

            if (jwtService.validateToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = jwtService.extractSubject(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
