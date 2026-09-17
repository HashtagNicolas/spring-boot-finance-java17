package com.hashtag.ngo.example.bank.cucumber;

import com.hashtag.ngo.example.bank.entity.AccountRepository;
import com.hashtag.ngo.example.bank.entity.SavingsAccount;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Définitions des étapes ("step definitions") des scénarios du fichier
 * {@code comptes.feature}.
 * <p>
 * Les étapes qui appellent l'API le font via de vraies requêtes HTTP en
 * utilisant {@link RestClient} (le client HTTP synchrone de Spring
 * Framework 6/7) vers le serveur Tomcat réellement démarré par
 * {@code @SpringBootTest(webEnvironment = RANDOM_PORT)} (voir
 * {@link CucumberSpringConfiguration}) : cela permet de tester en une seule
 * fois les contrôleurs, la sécurité JWT, la couche métier et la persistance
 * JPA/H2, exactement comme le ferait un client réel.
 * <p>
 * Note technique : Spring Boot 4 a retiré {@code TestRestTemplate} du
 * module {@code spring-boot-test}. Contrairement à {@code retrieve()} (qui
 * lève une exception sur les statuts 4xx/5xx), on utilise ici
 * {@code exchange(...)} afin de récupérer telle quelle n'importe quelle
 * réponse HTTP (y compris 400/401/404), ce qui est nécessaire pour vérifier
 * les scénarios d'erreur.
 * <p>
 * Seule l'étape "les intérêts sont appliqués" accède directement à
 * {@link AccountRepository} : il n'existe pas d'endpoint REST dédié au
 * calcul des intérêts (ce n'est pas une opération déclenchée par un
 * client, mais une règle métier interne), cette étape illustre donc un
 * test BDD ciblant directement la couche métier/entité plutôt que l'API
 * HTTP.
 * <p>
 * Cette classe n'est pas annotée {@code @Component} : cucumber-spring
 * l'instancie lui-même comme un bean du contexte Spring, ce qui permet d'y
 * utiliser {@code @Autowired} normalement.
 */
public class AccountStepDefinitions {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ScenarioContext scenarioContext;

    private RestClient restClient;

    private RestClient restClient() {
        if (restClient == null) {
            restClient = RestClient.builder().baseUrl("http://localhost:" + port).build();
        }
        return restClient;
    }

    // --- Authentification ---------------------------------------------------

    // Note : en français, Gherkin reconnaît "Étant donné que" comme un seul
    // mot-clé (au même titre que "Étant donné" seul) ; le "que " du fichier
    // .feature est donc déjà consommé par le mot-clé et ne fait pas partie
    // du texte de l'étape ci-dessous.
    @Given("je m'authentifie avec l'utilisateur {string} et le mot de passe {string}")
    public void je_m_authentifie(String username, String password) throws Exception {
        Map<String, String> body = Map.of("username", username, "password", password);
        ResponseEntity<String> response = send(HttpMethod.POST, "/auth/token", body, null);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        JsonNode json = objectMapper.readTree(response.getBody());
        scenarioContext.setToken(json.get("token").asText());
    }

    // --- Création de comptes -------------------------------------------------

    @Given("un compte courant pour {string} avec un solde initial de {int} et un découvert autorisé de {int}")
    public void un_compte_courant_existe(String owner, int initialBalance, int overdraftLimit) throws Exception {
        creerCompteCourant(owner, initialBalance, overdraftLimit);
    }

    @When("je crée un compte courant pour {string} avec un solde initial de {int} et un découvert autorisé de {int}")
    public void je_cree_un_compte_courant(String owner, int initialBalance, int overdraftLimit) throws Exception {
        creerCompteCourant(owner, initialBalance, overdraftLimit);
    }

    private void creerCompteCourant(String owner, int initialBalance, int overdraftLimit) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("owner", owner);
        body.put("type", "COURANT");
        body.put("initialBalance", initialBalance);
        body.put("overdraftLimit", overdraftLimit);
        creerCompteEtMemoriserId(body);
    }

    // Note : le paramètre de taux est capturé en {string}, pas {double}.
    // Cucumber interprète {double} selon la locale déduite de "# language: fr"
    // en tête du fichier .feature, où "." est un séparateur de milliers (pas
    // décimal) : "0.05" y serait alors lu comme 5.0 au lieu de 0.05.
    // Double.parseDouble reste indépendant de toute locale.
    @Given("un compte épargne pour {string} avec un solde initial de {int} et un taux d'intérêt de {string}")
    public void un_compte_epargne_existe(String owner, int initialBalance, String interestRate) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("owner", owner);
        body.put("type", "EPARGNE");
        body.put("initialBalance", initialBalance);
        body.put("interestRate", Double.parseDouble(interestRate));
        creerCompteEtMemoriserId(body);
    }

    private void creerCompteEtMemoriserId(Map<String, Object> body) throws Exception {
        ResponseEntity<String> response = send(HttpMethod.POST, "/accounts", body, scenarioContext.getToken());
        scenarioContext.setLastResponse(response);
        if (response.getStatusCode().value() == 201) {
            JsonNode json = objectMapper.readTree(response.getBody());
            scenarioContext.setCurrentAccountId(json.get("id").asLong());
        }
    }

    // --- Dépôt / retrait -------------------------------------------------------

    @When("je dépose {int} sur ce compte")
    public void je_depose_sur_ce_compte(int amount) throws Exception {
        operationSurCompte("deposit", amount);
    }

    @When("je retire {int} de ce compte")
    public void je_retire_de_ce_compte(int amount) throws Exception {
        operationSurCompte("withdraw", amount);
    }

    private void operationSurCompte(String operation, int amount) throws Exception {
        Map<String, Object> body = Map.of("amount", amount);
        String path = "/accounts/" + scenarioContext.getCurrentAccountId() + "/" + operation;
        ResponseEntity<String> response = send(HttpMethod.POST, path, body, scenarioContext.getToken());
        scenarioContext.setLastResponse(response);
    }

    // --- Intérêts (règle métier interne, testée directement sur l'entité) -----

    @When("les intérêts sont appliqués sur ce compte")
    public void les_interets_sont_appliques() {
        SavingsAccount account = (SavingsAccount) accountRepository
                .findById(scenarioContext.getCurrentAccountId())
                .orElseThrow();
        account.applyInterest();
        accountRepository.save(account);
    }

    // --- Consultation ------------------------------------------------------------

    // Nécessaire après "les intérêts sont appliqués sur ce compte" : cette
    // étape modifie l'entité directement en base (pas d'appel HTTP), donc la
    // dernière réponse HTTP en mémoire est encore celle de la création du
    // compte. Ce step relit l'état à jour du compte via l'API avant que
    // "le solde du compte est" ne vérifie son solde.
    @When("je consulte ce compte")
    public void je_consulte_ce_compte() throws Exception {
        String path = "/accounts/" + scenarioContext.getCurrentAccountId();
        scenarioContext.setLastResponse(send(HttpMethod.GET, path, null, scenarioContext.getToken()));
    }

    // --- Appels non authentifiés / mal authentifiés ----------------------------
    //
    // Note : dans une Cucumber Expression, "/" introduit une alternative
    // ("a/b" == "a" ou "b"), il doit donc être échappé en "\/" pour être
    // traité comme un simple caractère littéral dans "GET /accounts".

    @When("j'appelle GET \\/accounts sans jeton d'authentification")
    public void j_appelle_sans_jeton() throws Exception {
        scenarioContext.setLastResponse(send(HttpMethod.GET, "/accounts", null, null));
    }

    @When("j'appelle GET \\/accounts avec le jeton invalide {string}")
    public void j_appelle_avec_jeton_invalide(String invalidToken) throws Exception {
        scenarioContext.setLastResponse(send(HttpMethod.GET, "/accounts", null, invalidToken));
    }

    @When("j'appelle GET \\/accounts avec mon jeton")
    public void j_appelle_avec_mon_jeton() throws Exception {
        scenarioContext.setLastResponse(send(HttpMethod.GET, "/accounts", null, scenarioContext.getToken()));
    }

    // --- Vérifications ("Alors") -------------------------------------------------

    @Then("la réponse a le statut {int}")
    public void la_reponse_a_le_statut(int expectedStatus) {
        assertThat(scenarioContext.getLastResponse().getStatusCode().value()).isEqualTo(expectedStatus);
    }

    @Then("^le solde du compte(?: créé)? est ([0-9]+(?:\\.[0-9]+)?)$")
    public void le_solde_du_compte_est(String expectedBalance) throws Exception {
        JsonNode json = objectMapper.readTree(scenarioContext.getLastResponse().getBody());
        BigDecimal actual = new BigDecimal(json.get("balance").asText());
        assertThat(actual).isEqualByComparingTo(new BigDecimal(expectedBalance));
    }

    @Then("le code d'erreur retourné est {string}")
    public void le_code_d_erreur_retourne_est(String expectedCode) throws Exception {
        JsonNode json = objectMapper.readTree(scenarioContext.getLastResponse().getBody());
        assertThat(json.get("code").asText()).isEqualTo(expectedCode);
    }

    // --- Utilitaires -------------------------------------------------------------

    /**
     * Envoie une requête HTTP vers le serveur de test et renvoie la réponse
     * telle quelle, quel que soit son statut (200, 400, 401, 404...).
     * {@code body} vaut {@code null} pour une requête sans corps (ex. GET),
     * {@code token} vaut {@code null} pour une requête sans en-tête
     * {@code Authorization}.
     */
    private ResponseEntity<String> send(HttpMethod method, String path, Object body, String token) throws Exception {
        RestClient.RequestBodySpec spec = restClient().method(method).uri(path);
        if (token != null) {
            spec = spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        if (body != null) {
            spec = spec.contentType(MediaType.APPLICATION_JSON).body(objectMapper.writeValueAsString(body));
        }
        return spec.exchange((request, response) ->
                ResponseEntity.status(response.getStatusCode()).body(response.bodyTo(String.class)));
    }
}
