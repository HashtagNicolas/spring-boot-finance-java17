package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.entity.AccountNotFoundException;
import com.hashtag.ngo.example.bank.entity.InsufficientFundsException;
import com.hashtag.ngo.example.bank.entity.InvalidAmountException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Traduit les exceptions métier et techniques en réponses HTTP au format
 * uniforme {@link ErrorResponse}.
 * <p>
 * Distinction volontaire entre :
 * <ul>
 *     <li>les erreurs <b>fonctionnelles</b> ({@link AccountNotFoundException},
 *     {@link InsufficientFundsException}, {@link InvalidAmountException}) :
 *     message explicite, code d'erreur stable, statut HTTP adapté (404/400) ;</li>
 *     <li>les erreurs <b>techniques</b> (tout le reste) : statut 500, message
 *     générique ne divulguant ni la stack trace ni le message d'exception
 *     brut, qui pourrait révéler des détails d'implémentation sensibles.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "COMPTE_INTROUVABLE", ex.getMessage());
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        return build(HttpStatus.BAD_REQUEST, "SOLDE_INSUFFISANT", ex.getMessage());
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAmount(InvalidAmountException ex) {
        return build(HttpStatus.BAD_REQUEST, "MONTANT_INVALIDE", ex.getMessage());
    }

    /**
     * Identifiants invalides lors de POST /auth/token (levée par
     * {@code AuthenticationManager.authenticate(...)} dans {@link AuthController}).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "AUTHENTIFICATION_ECHOUEE", "Identifiants invalides.");
    }

    /**
     * Filet de sécurité pour toute exception non prévue : ne jamais exposer
     * {@code ex.getMessage()} ni la stack trace au client, seulement un
     * message générique. Le détail complet reste disponible côté serveur
     * dans les logs (non affiché ici pour garder l'exemple minimal).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "ERREUR_INTERNE", "Une erreur technique est survenue.");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message) {
        ErrorResponse body = new ErrorResponse(LocalDateTime.now(), status.value(), code, message);
        return ResponseEntity.status(status).body(body);
    }
}
