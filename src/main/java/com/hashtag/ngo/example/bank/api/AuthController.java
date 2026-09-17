package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.bean.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur d'authentification : échange des identifiants contre un jeton
 * JWT. Cet endpoint est le seul, avec Swagger UI, ouvert sans authentification
 * (voir {@link SecurityConfig}).
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentification", description = "Obtention d'un jeton JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/token")
    @SecurityRequirements // pas de jeton requis pour obtenir un jeton
    @Operation(summary = "S'authentifier",
            description = "Vérifie les identifiants et renvoie un jeton JWT valable pour appeler /accounts/**. "
                    + "Utilisateur de démonstration : demo / demo123.")
    public TokenResponse token(@RequestBody LoginRequest request) {
        // Délègue la vérification du mot de passe à Spring Security ; lève
        // une AuthenticationException (-> 401, voir GlobalExceptionHandler)
        // si les identifiants sont incorrects.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        return new TokenResponse(jwtService.generateToken(request.username()));
    }
}
