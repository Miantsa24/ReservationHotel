## Rôles:
- **Sprint1: 
   Team Lead : Malala ETU003211
   Dev 1 (BackOffice) : Tojo ETU003362
   Dev 2 (FrontOffice) : Alexandra ETU003306

## Todo List pour Sprint 1

### Rôles et Responsabilités
- **TL (Team Lead)** : Code reviews, assignation des tâches, création du todo list, merges, déploiements.
- **Dev1 (Back-office)** : Gestion des hôtels et formulaires d'insertion.
- **Dev2 (Front-office)** : Liste des réservations avec filtre.

### Tâches pour Sprint 1

#### TL (Team Lead)
1. Créer les branches `staging` et `release` via l'interface Git.
2. Assigner les tâches aux devs via issues ou commentaires dans le repository.
3. Après réception des PR des devs :
   - Faire code review.
   - Si OK : Merger vers `main`, puis vers `staging`.
   - Déployer localement (tester l'application).
   - Si erreurs : Demander aux devs de créer une branche `fix/[nom]` et refaire PR.
4. Une fois staging OK : Merger vers `release`, déployer sur Render.

#### Dev1 (Back-office)
1. **Créer branche** : `feature/backoffice-hotels` à partir de `main`.
2. **Script base de données** : Créer un script SQL pour insérer les hôtels dans MySQL (table `hotels` avec colonnes comme `id`, `nom`, etc.). Pas d'interface, juste script.
3. **Formulaire d'insertion de réservation** :
   - Créer un contrôleur avec méthode pour afficher le formulaire (utilisant @GetMapping ou @UrlAnnotation du framework).
   - Le formulaire doit permettre d'entrer : dates/heures d'arrivée, nombre de personnes, hôtel (utiliser l'ID d'un hôtel du script).
   - Utiliser JSP pour la vue du formulaire.
   - Soumettre via POST, sauvegarder en base (table `reservations` avec liaison à `hotels`).
4. **Commiter et pousser** : Commiter les changements, pousser la branche.
5. **Pull Request** : Faire PR vers `main` pour review par TL.

#### Dev2 (Front-office)
1. **Créer branche** : `feature/frontoffice-reservations` à partir de `main`.
2. **Contrôleur pour liste** : Créer un contrôleur avec méthode pour récupérer les réservations (utiliser le Sprint 9 du framework pour JSON, mais adapter pour appeler une API interne dans le contrôleur).
3. **Page JSP** : Créer une page JSP pour afficher la liste des réservations (récupérer les données du contrôleur via ModelView).
4. **Filtre par date** : Ajouter un filtre sur la page (formulaire simple pour filtrer par date d'arrivée seulement). Le filtre doit recharger la page avec les réservations filtrées.
5. **Intégration framework** : Assurer que le framework personnalisé est intégré (dossier avec FrontServlet et annotations) pour gérer les routes et retourner les données.
6. **Commiter et pousser** : Commiter les changements, pousser la branche.
7. **Pull Request** : Faire PR vers `main` pour review par TL.

### Notes Générales
- Utiliser Spring MVC avec le framework personnalisé (FrontServlet pour routes, annotations pour contrôleurs).
- Vues : JSP.
- Base : MySQL, hôtels via script.
- Filtre : Date d'arrivée seulement.
- Après Sprint 1 : TL crée le todo pour Sprint 2, etc.


=================================================================================================================================================


## Rôles:
- **Sprint2: 
   Team Lead : Alexandra ETU003306
   Dev 1 (BackOffice) : Malala ETU003211
   Dev 2 (FrontOffice) : Tojo ETU003362

## Todo List pour Sprint 2

### Rôles et Responsabilités
- **TL (Team Lead)** : Code reviews, assignation des tâches, création du todo list, merges, déploiements.
- **Dev1 (Back-office)** : CRUD véhicules + paramètres..
- **Dev2 (Front-office)** : API liste véhicules protégée par token.

### Tâches pour Sprint 1

#### TL (Team Lead)
1. Assigner les tâches aux devs via le todo list dans `GIT_SETUP_AND_TODO.md`
2. suppression des 2 features: `feature/backoffice-hotels` et `feature/frontoffice-reservations`
3. Après réception des PR des devs :
   - Faire code review.
   - Si OK : Merger vers `main`, puis vers `staging`.
   - Déployer localement (tester l'application).
   - Si erreurs : Demander aux devs de créer une branche `fix/[nom]` et refaire PR.
4. Une fois staging OK : Merger vers `release`, déployer sur Render.

#### Dev1 (Back-office)
1. **Créer branche** : `feature/backoffice-vehicules` à partir de `main`.
2. **Script base de données** : modification de la base de donnée pour la facilitation du framework et la suivie du sprint suivant demander:
      -ajout de la table `vehicules` avec colonnes `id`, `marque`, `capacite`, `typeCarburant`(D,Es,H,El)
      -ajout de la table `token` avec colonnes `id`, `token`, `heure_expiration`
      -modification de la table `hotel` avec colonnes `id`, `code`, `nom `
      -modification de la table `reservation` avec colonnes `id`, `id_hotel`, `heure_arrivee `, `date_arrivee `, `nombre_personne `
3. **Formulaire d'insertion de voiture** :
      -créer un CRUD pour les vehicules: 
         *IMPORTANT*: modification des models: hotel et reservation
      -formulaire de création de voiture, qui doit contenir: marque, capacite, typeCarburant, vitesse_moyenne, temps_attente
      -créer une méthode backend (gestion token) qui permet de:
         + Génère un token aléatoire
         + Insère en base avec expiration (ex: 2 heures)
         *IMPORTANT*: interface non requise
4. **Commiter et pousser** : Commiter les changements, pousser la branche.
5. **Pull Request** : Faire PR vers `main` pour review par TL.
6. **Regle metier**: Un token est valide si Il existe en base et Son heure d’expiration n’est pas dépassée.

#### Dev2 (Front-office)
1. **Créer branche** : `feature/frontoffice-vehicules-api` à partir de `main`.
2. **Contrôleur pour la verification du token** : Créer un contrôleur qui verifie que:
      -le token existe
      -le tokenn’est pas expiré
      *IMPORTANT*: + si valide => Retourner liste véhicules
                   + si invalide => Retourner erreur 401 avec message d'erreur clair
3. **Page JSP** : Créer une page JSP pour afficher la liste des vehicules (avec le fonctionnalités: Appel API, Envoi du token, Affichage liste véhicules).
5. **Intégration framework** : Assurer que le framework personnalisé est intégré (dossier avec FrontServlet et annotations) pour gérer les routes et retourner les données.
6. **Commiter et pousser** : Commiter les changements, pousser la branche.
7. **Pull Request** : Faire PR vers `main` pour review par TL.