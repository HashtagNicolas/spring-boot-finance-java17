# language: fr
Fonctionnalité: Gestion des comptes bancaires
  En tant que client de la banque
  Je veux créer des comptes, y déposer et en retirer de l'argent
  Afin de gérer mes finances au quotidien

  Contexte:
    Étant donné que je m'authentifie avec l'utilisateur "demo" et le mot de passe "demo123"

  Scénario: Création d'un compte courant
    Quand je crée un compte courant pour "Alice" avec un solde initial de 100 et un découvert autorisé de 50
    Alors la réponse a le statut 201
    Et le solde du compte créé est 100

  Scénario: Dépôt sur un compte courant
    Étant donné un compte courant pour "Bob" avec un solde initial de 100 et un découvert autorisé de 0
    Quand je dépose 50 sur ce compte
    Alors la réponse a le statut 200
    Et le solde du compte est 150

  Scénario: Retrait autorisé sur un compte courant
    Étant donné un compte courant pour "Chloe" avec un solde initial de 100 et un découvert autorisé de 0
    Quand je retire 40 de ce compte
    Alors la réponse a le statut 200
    Et le solde du compte est 60

  Scénario: Retrait refusé pour solde insuffisant sur un compte épargne
    Étant donné un compte épargne pour "David" avec un solde initial de 100 et un taux d'intérêt de "0.02"
    Quand je retire 150 de ce compte
    Alors la réponse a le statut 400
    Et le code d'erreur retourné est "SOLDE_INSUFFISANT"

  Scénario: Calcul des intérêts sur un compte épargne
    Étant donné un compte épargne pour "Emma" avec un solde initial de 1000 et un taux d'intérêt de "0.05"
    Quand les intérêts sont appliqués sur ce compte
    Et je consulte ce compte
    Alors le solde du compte est 1050.00

  Scénario: Appel de l'API sans jeton d'authentification
    Quand j'appelle GET /accounts sans jeton d'authentification
    Alors la réponse a le statut 401

  Scénario: Appel de l'API avec un jeton invalide
    Quand j'appelle GET /accounts avec le jeton invalide "ceci-nest-pas-un-jeton-valide"
    Alors la réponse a le statut 401

  Scénario: Appel de l'API avec un jeton valide
    Quand j'appelle GET /accounts avec mon jeton
    Alors la réponse a le statut 200
