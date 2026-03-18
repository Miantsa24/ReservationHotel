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


Plan d'implémentation recommandé (pas de code, étapes claires à suivre)

API/Service
Extraire de assignGroupsForDate la logique read-only en computeAssignmentsForDate(Date) qui retourne un DTO Proposal contenant :
groupes (liste d'IDs de réservations + departureTime),
pour chaque réservation : proposedVehiculeId (nullable) et motif si non assigné,
pour chaque véhicule utilisé : estimatedKilometrage, heureDepart, heureArrivee.
Implémenter persistAssignments(Proposal) qui :
ouvre une connexion, setAutoCommit(false),
écrit reservation_vehicule (INSERT), crée vehicule_trajet (INSERT), met reservation_vehicule.vehicule_trajet_id (UPDATE) et vehicules.available_from (UPDATE),
commit ou rollback en cas d'erreur.
Controller
Ajouter GET/POST endpoints :
GET /assignations/compute?date=...&heure=... → appelle computeAssignmentsForDate, rend assignation-detail.jsp en mode "proposition".
POST /assignations/confirm → reçoit proposal id/payload (ou ids), appelle persistAssignments, redirige vers résultat.
Vues (JSP)
assignation-detail.jsp : afficher la proposition par réservation (véhicule proposé, capacité restante), afficher résumé par véhicule (km estimé, heure départ/arrivée), bouton "Confirmer" et option "Ajuster manuellement".
assignation-result.jsp : afficher succès/échecs (par réservation) et lien vers traçabilité.
Tests
Intégration : scénario Sprint5 (V1/V2 + vols 08:00/08:15/08:20) — vérifier propositions et, après confirmation, vérifier vehicule_trajet (kilométrage, heures) et vehicules.available_from.
Cas d'échec : conflict concurrent (simulateur), rollback correctness.
Extras conseillés
Ajouter logging et métriques pour proposer vs persist.
Backward compatibility : conserver AssignationService.assignerCreneau (persist immédiat) pour automation, mais privilégier compute+confirm in UI.
Fichiers à modifier (pointage)

Service compute/persist: GroupingService.java
Nouveau DTO/Model: src/main/java/models/AssignmentProposal.java (ou inner classes)
Controller: AssignationController.java (ajouter endpoints compute/confirm)
JSPs: /WEB-INF/views/assignation-detail.jsp, /WEB-INF/views/assignation-result.jsp
Tests: src/test/java/... (ajouter scénarios d'intégration)
Assurez-vous d'utiliser VehiculeTrajetDAO existant pour les inserts (déjà présent).
Risques / points d'attention

Transactionnalité : impératif d'utiliser transaction unique lors de la persistance (éviter auto-commit par DAO séparés).
Concurrence : si compute→confirm prend du temps, d'autres assignations concurrentes peuvent rendre la proposition invalide — prévoir verrouillage optimistic (vérifier available_from/occupied avant commit) ou afficher un warning si données ont changé.
Taille JSON liste_reservation : déjà gérée (JSON array string), DB exige valid JSON pour colonne JSON.


Ce qui manque / points à implémenter (Dev2)

Vues/JSP : mettre à jour assignation-detail.jsp / assignation-result.jsp pour afficher liste_reservation, heure_depart, kilometrage_parcouru (actuellement detail expose la liste des réservations, mais pas la proposition de trajet).
Contrôleurs/services pour workflow compute+confirm : GroupingService n’expose pas encore une méthode publique computeAssignmentsForDate(...) (retourne une proposition sérialisable) ni une méthode persistAssignments(proposal) atomique (transactionnelle). Actuellement assignGroupsForDate fait compute+persist en une seule méthode.
Endpoint UI pour confirmation : un POST séparé persistAssignments (ou confirm) qui persiste la proposition approuvée.
Transaction / atomicité : persistance multi-inserts actuellement faite via DAO calls séparés (auto-commit); il est recommandé de regrouper la persistance finale dans une transaction unique pour éviter partial writes.
Ajustements UI/UX pour afficher la proposition (bouton confirmer/ajuster) et gestion d'échecs utilisateurs.
Tests d'intégration supplémentaires ciblant kilométrage, heure_depart/heure_arrivee, et champs vehicule_trajet persistés.
Recommandation sur Option B (compute vs persist)

Oui — Option B doit être implémentée par Dev2. Raison courte :
Option B correspond aux responsabilités Dev2 (controllers + vues + affichage métriques + UX de confirmation) et garantit sécurité métier (prévisualisation avant écriture), meilleure UX et permet corrections manuelles avant écriture.
Techniquement : GroupingService fournit déjà la logique de grouping + affectation incrémentale en mémoire ; il faut séparer clairement la phase "compute" (sans side effects) et la phase "persist" (atomic write), puis exposer ces deux étapes au contrôleur + JSP.
Conséquences : garder AssignationService.assignerCreneau comme action rapide « persist immédiat » si nécessaire, mais ajouter endpoints/UI pour le flux proposé (compute → review → confirm → persist).



Le contrôleur /assignations/compute place l'objet AssignmentProposal (service) dans le modèle ; la JSP affiche les données de ce DTO. Si vous préférez afficher les détails complets des réservations (ref client, hôtel, personnes) côté proposition, on peut charger un Map<Integer,Reservation> dans le modèle (reservationsMap) et compléter les lignes — je l'ai laissé simple pour éviter des requêtes supplémentaires côté JSP.

18n/formatage d'heure : j'affiche les Timestamps via toString() pour simplicité ; si vous voulez un format lisible (HH:mm), je peux adapter

eft la distance totale d'un vehicule ne devrait pas etre distance aller * 2, elle devrait etre distance aller + distance dernier hotel - aeroport (bidirectionnelle donc ou aeroport - dernier hotel), le vehicule ne doit pas reprendre exactement le meme trajet, il doit juste revenir vers l'aeroport

Il y a soucis, eft quand on a un groupe ici on a 3 reservations dans un groupe, tous les vehicules de ce groupe partent en meme temps, a l'heure du dernier vol, heure depart c'est a dire meme si vehicule 2 ne recoit pas le dernier vol du groupe, il doit attendre vehicule 1 et part en meme temps que lui,
L'heure de depart du groupe sera donc l'heure du dernier vol du groupe, et ne depend pas du vehicule

Dans la base pour les distances il y a:
MariaDB [hotel_db]> select * from distance;
+----+-----------+---------+-------+
| id | from      | to      | km    |
+----+-----------+---------+-------+
|  1 | A├⌐roport | Colbert | 10.00 |
|  2 | A├⌐roport | Hotel1  | 50.00 |
|  3 | A├⌐roport | Novotel | 20.00 |
|  4 | A├⌐roport | Ibis    | 10.00 |
|  5 | A├⌐roport | Lokanga | 25.00 |
|  6 | A├⌐roport | Trano   | 30.00 |
|  7 | Colbert   | Novotel |  8.00 |
|  8 | Colbert   | Ibis    |  6.00 |
|  9 | Colbert   | Lokanga | 12.00 |
| 10 | Colbert   | Trano   | 20.00 |
| 11 | Novotel   | Ibis    | 14.00 |
| 12 | Novotel   | Lokanga | 10.00 |
| 13 | Novotel   | Trano   | 16.00 |
| 14 | Ibis      | Lokanga | 18.00 |
| 15 | Ibis      | Trano   | 18.00 |
| 16 | Lokanga   | Trano   | 12.00 |
+----+-----------+---------+-------+
16 rows in set (0.001 sec)

MariaDB [hotel_db]>
Pour les reservations j'ai choisi Colbert, Ibis et Novotel

Pour vehicule 1 le trajet sera : Aeroport - Colbert -Novotel -Aeroport (le plus proche) et ces distances sont tous dans la base pourtant le km estime est toujours N/A et l'heure d'arrivee est vide
Et pour vehicule 2 : Aeroport - Ibis - Aeroport et cette distance aussi est dans la base et pourtant c'est vide et N/A