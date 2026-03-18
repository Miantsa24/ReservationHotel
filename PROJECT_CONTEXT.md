# Contexte du Projet Hôtel

## Description Générale
Application web pour gérer les réservations d'hôtel pour les clients arrivant à l'aéroport. Un client réserve un hôtel avant son arrivée en donnant dates/heures d'arrivée, nombre de personnes et hôtel choisi. Le système liste les réservations (front-office) et permet l'insertion d'hôtels et de réservations (back-office).

## Technologies Utilisées
- **Backend** : Spring MVC.
- **Framework Personnalisé** : Dossier intégré avec FrontServlet (gestion des routes, annotations comme @Controller, @UrlAnnotation, @GetMapping, @PostMapping, @Json pour JSON, ModelView pour vues).
- **Vues** : JSP.
- **Base de Données** : MySQL. Hôtels insérés via script SQL (pas d'interface).
- **Filtre** : Par date d'arrivée seulement.

## Fonctionnalités
- **Front-office** : Page liste réservations avec filtre date. Utilise le framework pour récupérer données (Sprint 9 pour JSON, adapté pour page via API contrôleur).
- **Back-office** : Script insertion hôtels. Formulaire insertion réservation (utilise ID hôtel du script).

## Équipe
- **Team Lead (TL)** : Code reviews, assignation tâches, todo lists, merges, déploiements.
- **Dev1 (Back-office)** : Gestion hôtels et formulaires.
- **Dev2 (Front-office)** : Liste réservations et filtre.

## Workflow Git
- Branches : `main` (TL), `staging` (déploiement local), `release` (déploiement Render).
- Devs créent `feature/[nom]` à partir de `main`, PR vers `main`.
- TL : Review, merge si OK, sinon rejette.
- Déploiement : TL merge vers `staging`, déploie local. Si erreurs, devs créent `fix/[nom]`, PR. Si OK, merge vers `release`, déploiement web.

## Sprints
- **Sprint 1** : Implémentation basique front-office et back-office.
- Sprints suivants : Améliorations (voir todo lists du TL).

## Notes
- Intégrer le framework personnalisé dans le projet.
- Utiliser annotations du framework pour contrôleurs.
- Déploiements : Local (staging), puis Render (release).




Compréhension (bref)

Règle centrale Sprint 5 : regrouper les réservations dont les heures d'arrivée tombent dans la fenêtre "temps_attente" d'un véhicule (ex. 08:00–08:30) et considérer le groupe comme une seule unité pour le départ.
Heures de départ des véhicules : heure du dernier vol inclus dans le groupe (pas la première). Si aucun vol supplémentaire dans la fenêtre, départ = heure du dernier vol rencontré.
Un véhicule peut prendre plusieurs réservations si la capacité cumulée ≤ capacité véhicule.
Si une réservation arrive en dehors de la fenêtre (ex. 08:35) elle peut être affectée à un véhicule disponible alors, mais pas aux véhicules du groupe 08:00–08:30 déjà fermés.
Les règles antérieures restent valides : tri descendant par taille, prioriser véhicules déjà utilisés (packing), carburant (Diesel > Essence > ...), random tie-break.


Valider règles: Confirmer ancrage de la fenêtre (fenêtre = départ du premier vol étendue par temps_attente du véhicule ou autre) et règles existantes à conserver.
Spécification: Décrire l'algorithme de groupement (trier par heure_arrivee, construire fenêtres, départ = heure_arrivee du dernier vol du groupe) et critères d'eligibilité véhicule (available_from, capacité, règles carburant/capacité existantes).
Implémentation (GroupingService): Méthode publique qui, pour une date donnée, retourne groupes de réservations (liste d'IDs + heure_depart calculée).
Affectation véhicules: Pour chaque groupe, exécuter algorithme de packing (tri desc. par nombrePersonnes, greedy first-fit respecting capacity et règles antérieures). Marquer réservations comme ASSIGNE / NON_ASSIGNE / EN_ATTENTE selon résultat.
Persistance: Créer VehiculeTrajet pour chaque véhicule utilisé, mettre à jour reservation_vehicule.vehicule_trajet_id, mettre à jour vehicules.available_from et statuts, dans une transaction atomique.
Tests: Écrire tests unitaires reproduisant l'exemple (V1 8, V2 5, vols 08:00(6),08:15(4),08:20(2)) et cas limites (capacité insuffisante, indisponibilité).
Validation finale: Lancer tests, corriger, puis préparer commit/PR.

