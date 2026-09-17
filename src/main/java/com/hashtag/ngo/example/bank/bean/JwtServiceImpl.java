package com.hashtag.ngo.example.bank.bean;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Implémentation de {@link JwtService} basée sur la bibliothèque JJWT
 * (API 0.12.x : {@code Jwts.builder()} / {@code Jwts.parser()}).
 * <p>
 * Le secret de signature et la durée de validité sont externalisés dans
 * {@code application.yml} (propriétés {@code app.jwt.secret} et
 * {@code app.jwt.expiration-ms}) : voir ce fichier pour l'avertissement sur
 * le caractère "démo" du secret fourni.
 */
@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtServiceImpl(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public String generateToken(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Jeton absent, mal formé, expiré ou dont la signature ne correspond pas :
            // dans tous ces cas, il est simplement considéré comme invalide.
            return false;
        }
    }

    @Override
    public String extractSubject(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
