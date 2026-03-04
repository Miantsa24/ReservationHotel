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

=================================================================================================================================================


## Rôles:
- **Sprint 3:**
   Team Lead : Tojo ETU003362
   Dev 1 (BackOffice) : Malala ETU003211
   Dev 2 (BackOffice) : Alexandra ETU003306

## Todo List pour Sprint 3

### Rôles et Responsabilités
- **TL (Team Lead)** : Code reviews, assignation des tâches, création du todo list, merges, déploiements.
- **Dev1 (Back-office)** : Script BDD (table distance) + Page 1 (saisie date) + logique d'assignation automatique véhicule-réservation.
- **Dev2 (Back-office)** : Page 2 (affichage traçabilité véhicules) + calcul heure de retour à l'aéroport.

### Tâches pour Sprint 3

#### TL (Team Lead) : Tojo ETU003362
1. Assigner les tâches aux devs via le todo list dans `GIT_SETUP_AND_TODO.md`.
2. Suppression des anciennes features : `feature/backoffice-vehicules` et `feature/frontoffice-vehicules-api`.
3. Après réception des PR des devs :
   - Faire code review.
   - Si OK : Merger vers `main`, puis vers `staging`.
   - Déployer localement (tester l'application).
   - Si erreurs : Demander aux devs de créer une branche `fix/[nom]` et refaire PR.
4. Une fois staging OK : Merger vers `release`, déployer sur Render.

#### Dev1 (Back-office) : Malala ETU003211
1. **Créer branche** : `feature/backoffice-sprint3-dev1` à partir de `main`.
2. **Script base de données** : Ajouter la table `distance` avec les colonnes suivantes :
   - `id` (INT, PK, AUTO_INCREMENT)
   - `from` (VARCHAR — point de départ, ex: "Aéroport", "Hôtel Colbert")
   - `to` (VARCHAR — point d'arrivée, ex: "Hôtel Ibis", "Aéroport")
   - `km` (DECIMAL — distance en kilomètres)
   - Insérer des données de test cohérentes avec les hôtels existants (une seule entrée par paire de lieux, ex: Aéroport ↔ Hôtel X = 15km).
   - **Important** : La gestion bidirectionnelle (aller = retour) doit être faite côté backend dans la méthode de récupération de distance (pas de duplication dans la BDD).
3. **Assignation automatique véhicule-réservation** : Implémenter dans un SERVICE la logique qui assigne automatiquement un véhicule à chaque nouvelle réservation créée côté frontoffice :
   - Cette logique s'exécute automatiquement lors de la création d'une réservation (pas d'interface).
   - Créer une table associative `reservation_vehicule` avec les colonnes :
     * `id` (INT, PK, AUTO_INCREMENT)
     * `id_reservation` (INT, FK vers `reservation`)
     * `id_vehicule` (INT, FK vers `vehicules`)
   - Règles de sélection du véhicule :
     * Règle 1 : Choisir le véhicule dont la capacité est la plus proche du nombre de personnes demandé
                 (favoriser le véhicule avec le moins d'écart, sans laisser de capacité insuffisante).
     * Règle 2 : En cas d'égalité de capacité, prioriser le type de carburant : Diesel > Essence > Hybride > Électrique.
     * Règle 3 : En cas d'égalité de capacité ET de type carburant, sélectionner aléatoirement parmi les véhicules éligibles (seulement entre Diesel et Essence).
4. **Page 1 — Formulaire de saisie de date** :
   - Créer un contrôleur avec méthode @GetMapping pour afficher la Page 1.
   - La page JSP doit permettre de saisir uniquement une **date** (ex: 03/03/2026).
   - Soumettre via POST vers le contrôleur de Page 2.
5. **Commiter et pousser** : Commiter les changements, pousser la branche.
6. **Pull Request** : Faire PR vers `main` pour review par TL.

#### Dev2 (Back-office) : Alexandra ETU003306
1. **Créer branche** : `feature/backoffice-sprint3-dev2` à partir de `main`.
2. **Calcul de l'heure de retour à l'aéroport** : Implémenter une méthode utilitaire qui calcule l'heure de retour du véhicule à l'aéroport :
   - Récupérer la distance totale parcourue (aller vers tous les hôtels + retour à l'aéroport) depuis la table `distance`.
   - Utiliser la vitesse moyenne du véhicule (`vitesse_moyenne` de la table `vehicules`).
   - Formule : temps_trajet (h) = total_km / vitesse_moyenne → ajouter à l'heure de départ pour obtenir l'heure de retour.
3. **Page 2 — Affichage de la traçabilité des véhicules** :
   - Créer un contrôleur avec méthode @PostMapping pour recevoir la date de la Page 1.
   - Afficher en en-tête la date saisie (ex: "Traçabilité du 03/03/2026").
   - Récupérer depuis la base et afficher pour chaque véhicule ayant des réservations à cette date :
     * Le véhicule (marque, capacité, type carburant, vitesse moyenne).
     * Toutes les réservations assignées à ce véhicule pour cette date.
     * Tous les hôtels parcourus par ce véhicule cette date.
     * Date et heure de départ de l'aéroport (ramassage du 1er client = heure d'arrivée du 1er client à l'aéroport).
     * Date et heure de retour du véhicule à l'aéroport (calculée après le dernier hôtel).
4. **Commiter et pousser** : Commiter les changements, pousser la branche.
5. **Pull Request** : Faire PR vers `main` pour review par TL.

### Notes Générales
- Utiliser le framework personnalisé (FrontServlet + annotations) pour les routes.
- Vues : JSP uniquement.
- Base : MySQL.
- L'assignation véhicule-réservation (Dev1) se fait automatiquement dans le code backend lors de la création d'une réservation — aucune interface utilisateur n'est nécessaire pour cette logique.
- Simplification : 1 client = 1 hôtel = 1 véhicule (pas de gestion multi-clients dans un même véhicule ni multi-hôtels).
- La logique de sélection du véhicule (Dev1) doit être placée dans un SERVICE PARTAGÉ pour que le frontoffice puisse l'appeler lors de la création de réservation.
- Les deux devs doivent coordonner le nom et la signature des méthodes partagées AVANT de commencer le développement pour éviter les conflits lors du merge.
- Données de test : s'assurer que la table `distance` contient les trajets Aéroport ↔ Hôtels cohérents avec les hôtels existants (une seule entrée par paire, la bidirectionnalité est gérée côté code).


## Rôles:
- **Sprint 4:**
   Team Lead : Malala ETU003211
   Dev 1 (BackOffice) : Alexandra ETU003306
   Dev 2 (BackOffice) : Tojo ETU003362

## Todo List pour Sprint 4

### Rôles et Responsabilités
- **TL (Team Lead)** : Code reviews, assignation des tâches, création du todo list, merges, déploiements.
- **Dev1 (Back-office)** : Implémentation partie assignation multi-réservations, DAO, services et tests.
- **Dev2 (Back-office)** : Implémentation disponibilité véhicules, algorithme d'ordonnancement d'hôtels (routing) et intégration contrôleurs/JSP.

### Tâches pour Sprint 4

#### TL (Team Lead) : Malala ETU003211
1. Assigner les tâches aux devs via le todo list dans `GIT_SETUP_AND_TODO.md`.
2. Vérifier et valider les propositions d'architecture (champ `available_from` ou équivalent, ordering algorithm).
3. Après réception des PR des devs :
   - Faire code review.
   - Si OK : Merger vers `main`, puis vers `staging`.
   - Déployer localement (tester l'application).
   - Si erreurs : Demander aux devs de créer une branche `fix/[nom]` et refaire PR.
4. Une fois staging OK : Merger vers `release`, déployer sur Render.

#### Dev1 (Back-office) : Alexandra ETU003306
1. **Design & DB** : Proposer la migration SQL si nécessaire (ex: `ALTER TABLE vehicules ADD COLUMN available_from DATETIME NULL`).
2. **DAOs** : Mettre à jour `ReservationVehiculeDAO` et `VehiculeDAO` :
   - Méthode pour récupérer réservations d'un véhicule sur une date donnée.
   - Méthode pour calculer capacité occupée par véhicule pour une date/heure.
   - Lecture/écriture du champ disponibilité (`available_from`).
3. **Selection Service** : Étendre `VehiculeSelectionService` pour :
   - Autoriser plusieurs réservations par véhicule si capacité libre suffisante.
   - Calculer capacité libre = `capacité_véhicule - sum(capacités réservations assignées pour la même date+heure)`.
   - Prioriser traitement des réservations par ordre descendant `nombrePersonnes`.
   - Respecter règles existantes (carburant, aléatoire) pour départager véhicules.
4. **Validation réservation** : Retourner un message d'erreur clair si aucune allocation possible.
5. **Tests** : Écrire tests unitaires pour la nouvelle logique d'assignation multi-réservations.

#### Dev2 (Back-office) : Tojo ETU003362
1. **Disponibilité véhicules** : Implémenter mise à jour de `available_from` après assignation (utiliser `TracabiliteService.calculerHeureRetour`).
2. **Routing (ordre hôtels)** : Implémenter utilitaire pour ordonner les hôtels d'un véhicule :
   - Démarrer par l'hôtel le plus proche de l'aéroport.
   - Ensuite partir vers l'hôtel le plus proche du précédent (algorithme nearest-neighbour utilisant la table `distance`).
3. **Disponibilité à la réservation** : Filtrer véhicules éligibles selon `available_from` (doit être <= date+heure de la nouvelle réservation ou NULL).
4. **Controllers / UI** : Mettre à jour la création de réservation pour gérer le rejet/acceptation et afficher message utilisateur.
5. **Tests d'intégration** : Scénarios multi-réservations (ex: 2 clients même date+heure), scénario véhicule occupé.

### Notes et critères d'acceptation
- Un véhicule peut transporter plusieurs réservations si la capacité cumulée de ces réservations <= capacité du véhicule.
- La réservation avec le plus grand `nombrePersonnes` est assignée en priorité.
- Un véhicule est considéré non disponible tant qu'il n'est pas revenu à l'aéroport (`available_from` > dateDemande).
- Si aucune allocation possible, la création de réservation doit échouer avec un message d'erreur explicite.

### Données de test recommandées
- Distances : s'assurer que `distance` contient `Aéroport ↔ Colbert = 15` et autres trajets nécessaires.
- Scénarios :
  * Client A (2 pers, 2026-03-04 10:00), Client B (3 pers, 2026-03-04 10:00) → vérifier assignation au même véhicule si capa OK.
  * Véhicule occupé (available_from après demande) → nouvelle réservation rejetée pour ce véhicule.

### Commits / PR
- Commits atomiques par fonctionnalité.
- Ouvrir PR vers `main` une fois testé et demander review du TL et de l'autre dev.

