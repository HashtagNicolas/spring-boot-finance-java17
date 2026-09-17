# API de comptes bancaires

Projet pédagogique **Spring Boot 4.1 / Java 17** : une API REST de gestion
de comptes bancaires (comptes courants et comptes épargne), sécurisée par
jeton JWT, documentée avec Swagger UI, et testée avec Cucumber (BDD) et
ArchUnit (règles d'architecture).

Le but de ce dépôt n'est pas de couvrir tous les cas d'usage d'une vraie
banque, mais de servir de support d'apprentissage : chaque choix technique
(records, classes scellées, architecture en couches, sécurité JWT...) est
commenté dans le code et expliqué ici.

---

## 1. Présentation

- **Domaine** : deux types de comptes bancaires partagent une même racine
  métier :
  - **compte courant** (`CheckingAccount`) : autorise un découvert jusqu'à
    une limite fixée à la création ;
  - **compte épargne** (`SavingsAccount`) : jamais de découvert, mais
    produit des intérêts calculés sur son solde.
- **API REST** : création de compte, consultation, dépôt, retrait.
- **Sécurité** : authentification par utilisateur/mot de passe qui délivre
  un jeton JWT ; ce jeton doit ensuite accompagner chaque appel aux
  endpoints de gestion des comptes.
- **Persistance** : base **H2 en mémoire**, aucune installation externe
  requise — les données sont réinitialisées à chaque démarrage.
- **Documentation** : générée automatiquement et consultable dans un
  navigateur via **Swagger UI**.

---

## 2. Prérequis et commandes

### Prérequis

- **JDK 17** (le projet compile avec `maven.compiler.release=17` et
  n'utilise aucune fonctionnalité au-delà de Java 17)
- **Maven** 3.9+ (un wrapper Maven n'est pas fourni ; utilisez votre
  installation locale : `mvn -version` doit répondre)

Aucune base de données, aucun serveur externe à installer : tout tourne en
mémoire.

### Lancer les tests

```bash
mvn clean test
```

Exécute en une seule commande :
- les scénarios **Cucumber** (`src/test/resources/features/comptes.feature`) ;
- les règles **ArchUnit** (`ArchitectureTest`).

### Démarrer l'application

```bash
mvn spring-boot:run
```

L'application démarre sur `http://localhost:8080`.

### Accéder à la documentation Swagger UI

Une fois l'application démarrée, ouvrez :

```
http://localhost:8080/swagger-ui.html
```

La spécification OpenAPI brute (JSON) est disponible sur `/v3/api-docs`.

### Obtenir un jeton (utilisateur de démonstration)

Un unique utilisateur de démonstration est déclaré en mémoire :

| Utilisateur | Mot de passe |
|---|---|
| `demo` | `demo123` |

```bash
curl -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "demo", "password": "demo123"}'
```

La réponse contient le jeton à utiliser dans le bouton **Authorize** de
Swagger UI, ou dans l'en-tête `Authorization: Bearer <jeton>` de vos
requêtes `curl`.

---

## 3. Structure du projet

```
src/main/java/com/hashtag/ngo/example/bank/
├── BankApplication.java        # point d'entrée Spring Boot
│
├── entity/                     # persistance JPA — la couche la plus basse,
│   │                           # ne dépend d'aucune autre couche du projet
│   ├── Account.java                  # racine scellée @Entity (SINGLE_TABLE)
│   ├── CheckingAccount.java          # compte courant (final @Entity)
│   ├── SavingsAccount.java           # compte épargne (final @Entity)
│   ├── AccountType.java              # enum COURANT / EPARGNE
│   ├── AccountRepository.java        # JpaRepository<Account, Long>
│   ├── AccountNotFoundException.java
│   ├── InsufficientFundsException.java
│   └── InvalidAmountException.java
│
├── bean/                       # logique métier — ne dépend que d'"entity"
│   ├── AccountService.java           # interface
│   ├── AccountServiceImpl.java       # implémentation
│   ├── JwtService.java               # interface (génération/validation JWT)
│   └── JwtServiceImpl.java           # implémentation (bibliothèque JJWT)
│
└── api/                        # REST — peut dépendre de "bean" et "entity"
    ├── AccountController.java        # endpoints /accounts
    ├── AuthController.java           # endpoint /auth/token
    ├── AccountRequest.java / AccountResponse.java / AmountRequest.java
    ├── LoginRequest.java / TokenResponse.java / ErrorResponse.java
    ├── AccountMapper.java            # mapping MapStruct entité <-> DTO
    ├── SecurityConfig.java           # chaîne de filtres Spring Security
    ├── JwtAuthenticationFilter.java  # lecture/validation du Bearer token
    ├── UserDetailsConfig.java        # utilisateur de démonstration
    ├── GlobalExceptionHandler.java   # traduction exceptions -> HTTP
    └── OpenApiConfig.java            # métadonnées Swagger / schéma Bearer

src/test/java/com/hashtag/ngo/example/bank/
├── cucumber/                    # tests d'acceptation (BDD)
│   ├── RunCucumberTest.java           # point d'entrée JUnit Platform
│   ├── CucumberSpringConfiguration.java
│   ├── ScenarioContext.java           # état partagé entre étapes d'un scénario
│   └── AccountStepDefinitions.java    # implémentation des étapes Gherkin
└── archunit/
    └── ArchitectureTest.java          # règles de dépendances et de nommage

src/test/resources/features/
└── comptes.feature              # scénarios en français
```

**Règle d'architecture (vérifiée par ArchUnit)** : les dépendances ne vont
que dans un sens, `api → bean → entity`. Une couche basse ne dépend jamais
d'une couche du dessus. C'est aussi pour cette raison que le mapping
MapStruct (`AccountMapper`) est injecté dans `AccountController` (couche
`api`) et non dans `AccountServiceImpl` (couche `bean`) : le service métier
manipule directement l'entité `Account`, et la conversion vers les DTO
`AccountRequest`/`AccountResponse` se fait uniquement à la frontière de
l'API.

---

## 4. Fonctionnalité métier ↔ nouveauté Java 17

| Fonctionnalité métier | Nouveauté Java 17 utilisée | Où la voir |
|---|---|---|
| Modéliser "un compte est soit courant, soit épargne, jamais autre chose" | **Classes scellées** (`sealed` / `permits`, JEP 409) | `Account.java` |
| Créer le bon type de compte selon le type demandé | **Switch expression** exhaustif sur un enum | `AccountServiceImpl.createAccount(...)` |
| Extraire le champ spécifique (découvert ou taux) selon le type réel du compte | **`instanceof` avec pattern matching** (JEP 394) | `AccountMapper.extractOverdraftLimit/InterestRate` |
| Transporter des données immuables entre les couches (requêtes/réponses HTTP, jeton, erreurs) | **Records** (JEP 395) | `AccountRequest`, `AccountResponse`, `TokenResponse`, `ErrorResponse`, `LoginRequest`, `AmountRequest` |
| Rédiger une documentation Swagger multi-lignes lisible dans le code | **Text blocks** (JEP 378) | `OpenApiConfig.java` |
| Manipuler des collections de comptes | `Stream.toList()`, `List.of()`, `Map.of()`, `Optional` | `AccountServiceImpl`, `AccountController` |

---

## 5. Record vs DTO classique

Toutes les données échangées à travers l'API (`AccountRequest`,
`AccountResponse`, `TokenResponse`, `LoginRequest`, `AmountRequest`,
`ErrorResponse`) sont des **records** plutôt que des classes avec des champs
`private`, des getters/setters et un constructeur manuscrit. Pourquoi :

- **Immutabilité par défaut** : un DTO n'a aucune raison d'être modifié
  après sa création (il représente un instantané d'une requête ou d'une
  réponse HTTP). Un record interdit toute mutation accidentelle après
  construction, sans discipline particulière à respecter.
- **Moins de code, moins de bugs** : constructeur, accesseurs,
  `equals()`/`hashCode()` et `toString()` sont générés automatiquement à
  partir de la seule déclaration des composants. Un DTO classique
  équivalent demanderait 5 à 10 fois plus de lignes pour le même résultat,
  avec le risque d'oublier de mettre à jour `equals()`/`hashCode()` après
  l'ajout d'un champ.
- **Intention claire** : lire `public record AccountResponse(Long id, ...)`
  suffit à comprendre que c'est une structure de données pure, sans
  comportement métier caché à l'intérieur — contrairement à une classe
  classique qui pourrait dissimuler de la logique dans ses accesseurs.
- **Compatible Jackson d'office** : la (dé)sérialisation JSON fonctionne
  sans configuration supplémentaire, un record étant reconnu nativement.

Les **entités JPA** (`Account` et ses sous-classes), elles, restent des
classes classiques (mutables, avec un identifiant et un cycle de vie géré
par Hibernate) : un record ne peut pas être une entité JPA, car JPA a besoin
de pouvoir modifier l'état d'un objet après sa construction (chargement
"lazy", proxys, etc.), ce qu'un record interdit par construction.

---

## 6. Les briques techniques du projet

### Cucumber (tests d'acceptation / BDD)

Les scénarios métier sont décrits en français dans
`src/test/resources/features/comptes.feature`, avec la syntaxe Gherkin
(`Étant donné` / `Quand` / `Alors`). Chaque ligne est reliée à une méthode
Java dans `AccountStepDefinitions.java` via une expression Cucumber
(`@Given("...")`, etc.).

Intérêt pédagogique : un scénario Cucumber se lit comme une spécification
métier ("étant donné un compte épargne avec 100 € et un taux de 2 %, quand
je retire 150 €, alors la réponse est refusée avec le code
SOLDE_INSUFFISANT") — utile pour valider le comportement attendu avec
quelqu'un qui ne lit pas forcément le code Java. `CucumberSpringConfiguration`
démarre l'application complète (`@SpringBootTest`, port aléatoire), donc les
scénarios effectuent de vrais appels HTTP contre un vrai serveur Tomcat et
une vraie base H2.

### MapStruct (mapping entité ↔ DTO)

`AccountMapper` est une interface annotée `@Mapper(componentModel = "spring")` :
à la compilation, l'annotation processor MapStruct génère automatiquement
une classe `AccountMapperImpl` qui fait la conversion `Account` → 
`AccountResponse`, sans qu'une seule ligne de ce code de mapping ne soit
écrite à la main. Cela évite le code répétitif et sujet aux erreurs d'un
mapping manuel (`response.setId(account.getId())`, ...), tout en restant du
code Java classique et debuggable (contrairement à une solution basée sur
la réflexion à l'exécution).

### ArchUnit (règles d'architecture testées automatiquement)

`ArchitectureTest.java` transforme des règles habituellement seulement
écrites dans une documentation ("la couche api ne doit jamais être appelée
par la couche bean") en **tests JUnit exécutés à chaque `mvn test`** :
- dépendances en couches : `api → bean → entity`, jamais l'inverse ;
- conventions de nommage : les contrôleurs REST se terminent par
  `Controller`, les interfaces de service de la couche `bean` par
  `Service`, leurs implémentations par `ServiceImpl`.

Si quelqu'un enfreint une de ces règles (par exemple en important un DTO de
l'API directement dans le service métier), le build échoue immédiatement au
lieu de laisser l'architecture se dégrader silencieusement.

### Spring Security + JWT

- `SecurityConfig` configure une chaîne de filtres **sans état** (`STATELESS`) :
  pas de session HTTP, CSRF désactivé (il ne protège que les
  authentifications par cookie de session, absentes ici).
- `POST /auth/token` et Swagger UI sont accessibles sans authentification ;
  tout le reste (`/accounts/**`) exige un jeton valide.
- `JwtService` (interface) / `JwtServiceImpl` (implémentation, bibliothèque
  JJWT) est **volontairement découplé** de Spring MVC : il ne connaît ni
  requêtes ni réponses HTTP, seulement des chaînes de caractères
  (`generateToken(subject)`, `validateToken(token)`,
  `extractSubject(token)`). Cela permet de le réutiliser tel quel dans un
  tout autre contexte — par exemple un client qui aurait besoin de générer
  ou valider des jetons pour appeler d'autres services, sans dépendre de ce
  projet web.
- `JwtAuthenticationFilter` est le seul endroit qui fait le pont entre HTTP
  (lecture de l'en-tête `Authorization`) et `JwtService` : il peuple le
  `SecurityContext` si le jeton est valide, et laisse Spring Security
  décider (401) si l'endpoint appelé exige une authentification absente.

### JPA, entités et H2

- `Account` est une classe **abstraite scellée** (`sealed ... permits
  CheckingAccount, SavingsAccount`), stratégie d'héritage
  `SINGLE_TABLE` : toutes les variantes de compte sont stockées dans une
  seule table `accounts`, avec une colonne discriminante `account_type`.
  C'est la stratégie la plus simple et la plus performante (pas de
  jointure) pour une hiérarchie à deux niveaux comme celle-ci.
- Les colonnes propres à une sous-classe (`overdraft_limit`,
  `interest_rate`) sont nullables : avec `SINGLE_TABLE`, elles sont
  partagées par toutes les lignes de la table, y compris celles de l'autre
  sous-classe où elles n'ont pas de sens.
- `AccountRepository extends JpaRepository<Account, Long>` fournit les
  opérations CRUD sans code supplémentaire.
- La base **H2** tourne en mémoire (`jdbc:h2:mem:bankdb`) : aucune
  installation, données réinitialisées à chaque redémarrage — idéal pour un
  projet pédagogique ou une CI.

### Swagger UI (tester l'API sans écrire de code)

`springdoc-openapi` génère la documentation OpenAPI à partir des annotations
des contrôleurs (`@Tag`, `@Operation`) et de `OpenApiConfig`
(`@OpenAPIDefinition`, `@SecurityScheme`). Sur `/swagger-ui.html`, vous
pouvez :
1. Appeler `POST /auth/token` directement depuis la page ;
2. Copier le jeton reçu ;
3. Cliquer sur **Authorize** en haut de la page et coller le jeton (le
   préfixe `Bearer` est ajouté automatiquement) ;
4. Appeler les endpoints `/accounts/**`, désormais authentifiés.

---

## 7. Exemple d'appels curl

```bash
# 1. Obtenir un jeton
TOKEN=$(curl -s -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "demo", "password": "demo123"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 2. Créer un compte courant (200 € de solde initial, 100 € de découvert autorisé)
curl -s -X POST http://localhost:8080/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"owner": "Alice", "type": "COURANT", "initialBalance": 200, "overdraftLimit": 100}'

# Réponse (exemple) :
# {"id":1,"owner":"Alice","type":"COURANT","balance":200,"overdraftLimit":100,"interestRate":null,"createdAt":"..."}

# 3. Déposer 50 € sur ce compte (id 1)
curl -s -X POST http://localhost:8080/accounts/1/deposit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount": 50}'

# Réponse : le compte avec un solde de 250 €
```

Sans jeton, ou avec un jeton invalide, ces appels vers `/accounts/**`
renvoient un **401** ; un retrait ou un dépôt invalide (montant négatif,
solde insuffisant) renvoie un **400** avec un corps d'erreur du type :

```json
{
  "timestamp": "2026-01-15T10:30:00",
  "status": 400,
  "code": "SOLDE_INSUFFISANT",
  "message": "Retrait refusé : solde insuffisant sur le compte épargne 3"
}
```
