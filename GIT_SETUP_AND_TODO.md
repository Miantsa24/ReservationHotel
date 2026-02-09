# Guide d'Environnement Git et Todo List pour le Projet Hôtel

## 1. Environnement Git

### Branches Principales
- **main** : Branche principale, gérée par le Team Lead (TL). Toutes les merges finales arrivent ici.
- **staging** : Branche pour les déploiements locaux. Créée à partir de `main`.
- **release** : Branche pour les déploiements sur serveur (Render). Créée à partir de `main`.

### Workflow Git
1. **TL** : Crée les branches `staging` et `release` via l'interface GitHub/GitLab (dans le repository, aller dans "Branches" > "New branch").
2. **Développeurs** : Pour chaque fonctionnalité (feature), créer une branche `feature/[nom-feature]` à partir de `main`.
3. **Pull Requests (PR)** : Les devs font des PR vers `main`. TL fait le code review :
   - Si OK : Merge la PR.
   - Si erreurs : Rejette la PR, dev corrige et refait la PR.
4. **Déploiement** :
   - TL merge vers `staging`, déploie localement.
   - Si erreurs : Devs créent branche `fix/[nom-fix]` (cherry-pick via interface), PR vers `main`, puis nouveau merge vers `staging`.
   - Si OK : Devs créent `release`, TL déploie sur Render.
5. **Protection des branches** : Via l'interface GitHub/GitLab, protéger `main`, `staging`, `release` pour que seuls les merges via PR approuvées soient autorisés. TL doit approuver.

### Instructions pour Créer les Branches (via Interface)
- Allez dans le repository GitHub/GitLab.
- Cliquez sur "Branches" ou "View all branches".
- Cliquez "New branch".
- Nommez : `staging` (source : `main`), puis `release` (source : `main`).
- Pour les devs : Créez `feature/[nom]` à partir de `main`.

## 2. Todo List pour Sprint 1

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



