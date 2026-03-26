# Sprint 7 — Répartition optimale des passagers (Multi‑véhicules)

## 🎯 Objectif du Sprint
Mettre en place un algorithme permettant de répartir les passagers de plusieurs réservations sur plusieurs véhicules **en maximisant le remplissage**, avec possibilité de **fragmenter une réservation sur plusieurs véhicules**.

---

## 🧠 Récapitulatif du fonctionnement

- Les réservations sont regroupées par **fenêtre temporelle (temps d'attente)**.
- L’algorithme travaille sur **tout le groupe**, pas réservation par réservation.
- Un véhicule est rempli progressivement en choisissant toujours **le client qui optimise le remplissage**.
- Une réservation peut être **divisée sur plusieurs véhicules**.
- Les passagers non assignés sont **reportés à une prochaine fenêtre**.

---

## 📌 Exemple du Sprint 7

### Données
- Véhicules :
  - V1 : 8 places
  - V2 : 3 places

- Réservations :
  - R1 : 6 passagers
  - R2 : 4 passagers
  - R3 : 3 passagers

---

### Étape 1 — V1
- R1 (6) → V1 → reste 2 places

Choix optimal pour 2 places :
- R2 : 2 - 4 = -2
- R3 : 2 - 3 = -1 ✅

→ On prend R3 :
- 2 passagers dans V1
- 1 passager restant de R3

---

### Étape 2 — V2
Restants :
- R2 : 4
- R3 : 1

Pour 3 places :
- R2 : 3 - 4 = -1 ✅
- R3 : 3 - 1 = +2

→ On prend R2 :
- 3 passagers dans V2
- 1 restant

---

### Résultat
- V1 : 6 (R1) + 2 (R3)
- V2 : 3 (R2)
- Restants : 1 (R2) + 1 (R3)

---

## 🔄 Changements majeurs introduits

### 1. Passage à une allocation globale
Avant : 1 réservation → 1 véhicule  
Maintenant : traitement **par groupe complet**

---

### 2. Fragmentation des réservations
Une réservation peut être répartie sur plusieurs véhicules.

---

### 3. Nouvelle logique d’optimisation
Critère principal :
> Minimiser (places_restantes - passagers)

Priorité :
- valeur la plus proche de 0
- priorité aux valeurs négatives
- égalité → ordre d’arrivée

---

### 4. Persistance partielle
- Ajout du nombre de passagers assignés par véhicule
- Suivi du nombre total assigné dans une réservation

---

### 5. Gestion des passagers restants
Les passagers non placés ne bloquent pas :
→ ils sont reportés à une prochaine fenêtre

---

# 🚀 TODO LIST — SPRINT 7

## Rôles
- **Team Lead** : Code review, validation, merge, déploiement : Malala ETU003211
- **Dev1 (BackOffice)** : DB, modèles, DAO, algorithme : Alexandra ETU003306
- **Dev2 (BackOffice)** : Intégration, services, UI, tests : Tojo ETU003362

---

## 🔧 TL — Team Lead
1. Assigner les tâches via `GIT_SETUP_AND_TODO.md`
2. Valider la stratégie :
   - allocation globale par fenêtre
   - persistance partielle
3. Revoir les PR et valider les tests
4. Gérer les merges : `main → staging → release`

---

## 🧠 Dev1 — Backend Core : Alexandra ETU003306

### 1. DB & Migration
- Modifier `reservation_vehicule` :
  - autoriser 1 réservation → N véhicules
  - ajouter `passengers_assigned INT`

- Modifier `reservations` :
  - ajouter `assigned_count INT`
  - `remaining` doit être calculé dynamiquement (non stocké en base)

---

### 2. Models
- `Reservation` :
  - assignedCount, remaining
  - public int getRemaining() {
    return this.nombrePersonnes - this.assignedCount;
}

- `ReservationVehicule` :
  - passengersAssigned

---

### 3. DAO
Ajouter :
- updateAssignedCount()
- insertReservationVehicule()
- findByReservationId()
- sumAssignedByVehicule()

---

⚠️ IMPORTANT (RÈGLE GLOBALE)
❌ NE PAS créer de nouveau service inutile
✔ Utiliser les services existants :
GroupingService
AssignationService (gardé mais optionnel)
VehiculeSelectionService
✔ Ajouter uniquement la méthode métier centrale dans le service le plus logique (probablement GroupingService)
### 4. AllocationService (cœur métier)

👉 Implémentation dans :

✔ GroupingService (recommandé car déjà orchestration des groupes)

Méthode à ajouter :
allocateForGroup(
    Date date,
    Time windowStart,
    List<Reservation> reservations,
    List<Vehicule> vehicules
)
🧠 PRINCIPES D’ARCHITECTURE
❌ Aucun nouveau Service créé
✔ Réutilisation des services existants uniquement
✔ DAO utilisé uniquement pour :
initialisation capacité
persistance finale
✔ logique métier 100% en mémoire pendant allocation
🔥 ALGORITHME SPRINT 7 (VERSION PROPRE)
1. Initialisation capacité (1 seule fois)

Pour chaque véhicule :

remainingCapacity = vehicule.capacite - occupied(DB)
2. Préparation données
Trier reservations :
DESC nombrePersonnes
3. Allocation (SIMULATION MÉMOIRE)

Pour chaque véhicule :

Tant que capacité disponible > 0 :

Calcul du score :

score = remainingCapacity - reservation.remaining
🎯 Priorité sélection
score le plus proche de 0
score négatif prioritaire (overfill contrôlé)
égalité :
dateArrivee
heureArrivee
4. Assignation
✔ complète
remainingCapacity >= reservation.remaining
✔ partielle (Sprint 7 obligatoire)
remainingCapacity < reservation.remaining

→ split :

assignation partielle véhicule
mise à jour reservation.remaining
5. Mise à jour mémoire (PAS DB)
remainingCapacity--
assignedCount++
tracking en liste locale
6. Passage véhicule suivant
continuer jusqu’à saturation
puis véhicule suivant
7. Résultat final

Retour :

- List<ReservationVehicule>
- reservations restantes
- état final des véhicules
8. PERSISTENCE (après algo uniquement)
insertReservationVehicule()
updateAssignedCount()

### 5. Tests unitaires
- cas exemple Sprint 7
- overflow
- égalité
- fragmentation multiple

---

### 6. Explication des colonnes ajoutes:

1. reservations.assigned_count : nombre total de passagers déjà affectés (somme des passengers_assigned pour cette réservation). Mise à jour transactionnelle à chaque affectation partielle/complète. Sert pour l’affichage UI et pour déterminer si la réservation est totalement assignée. Valeur par défaut 0.

2. reservations.remaining : passagers restants = nombre_personnes - assigned_count. Peut être calculable mais stockée pour requêtes rapides et affichage. Toujours maintenue transactionnellement; initialisée à nombre_personnes.

3. reservation_vehicule.passengers_assigned : nombre de passagers de la même reservation embarqués dans ce véhicule précis. Permet la fragmentation (1 réservation → N lignes reservation_vehicule). Valeur par défaut 0. Influence le calcul d’occupation d’un véhicule (DAO doit sommer ce champ).

4. Contrainte d’intégrité (logique) : pour chaque reservation la somme des reservation_vehicule.passengers_assigned ≤ reservation.nombre_personnes. Lors de l’affectation, appliquer vérification + transaction pour maintenir invariant.

5. Index recommandés : indexer reservation_vehicule(id_vehicule), reservation.remaining, éventuellement (id_reservation) pour accès rapide et calculs.

6. Impact DAOs / calculs : remplacer les sommes basées sur nombre de réservations par sommes de passengers_assigned :

7. ReservationVehiculeDAO.getOccupiedCapacityForDateTime doit sommer passengers_assigned.
VehiculeSelectionService et GroupingService doivent utiliser capacités libres = vehicule.capacite - SUM(passengers_assigned).
Statuts réservations : ajouter ASSIGNE_PARTIEL (ou équivalent) pour indiquer qu’une réservation a des passagers assignés et des passagers non assignés ; transitions :

8. assigned_count == 0 → EN_ATTENTE / NON_ASSIGNE
0 < assigned_count < nombre_personnes → ASSIGNE_PARTIEL
assigned_count == nombre_personnes → ASSIGNE
Compatibilité vehicule_trajet / available_from : inchangées sémantiquement — lors de calculs de départ/retour, ne tenir compte que des réservations avec passengers_assigned > 0 (pour éviter trajets vides). kilometrage calculé sur hôtels réellement visités.

9. Backfill migration : script de migration doit :

ajouter colonnes avec valeurs par défaut,
si des lignes reservation_vehicule existent, initialiser reservation.assigned_count = SUM(passengers_assigned) (ou COUNT(*) * nombre_personnes si pas de passengers_assigned historique),
remaining = nombre_personnes - assigned_count.
Transactions & atomicité : toutes modifications d’affectation (insert reservation_vehicule, update passengers_assigned, update reservations.assigned_count/remaining, update statuts) doivent être faites en transaction unique pour éviter sur‑assignation.

10. UI / affichage : pages de traçabilité et listes doivent montrer par réservation : nombre_personnes, assigned_count, remaining, et pour chaque association id_vehicule → passengers_assigned.

## 🔗 Dev2 — Intégration : Tojo ETU003362

### 1. GroupingService
- regrouper par fenêtre
- appeler AllocationService

---

### 2. Persistance
- insert dans reservation_vehicule
- update assigned_count, et remaining
- gérer statuts :
  - NON_ASSIGNE
  - EN_ATTENTE
  - ASSIGNE_PARTIEL
  - ASSIGNE

---

### 3. VehiculeTrajet
- basé uniquement sur passagers réellement transportésx
- Seules les réservations avec passengers_assigned > 0 doivent être prises en compte

---

### 4. available_from
- recalcul après assignation

---

### 5. Controllers
- déclencher l’allocation via traitement de groupe
- pas à la création directe

---

### 6. JSP / UI
Afficher : dans tracabilite
- par réservation :
  - total / assigné / restant
- par véhicule :
  - passagers par réservation par trajet

---

### 7. Tests d’intégration
- pipeline complet :
  - grouping → allocation → persist → trajet

---

## ✅ Critères d’acceptation
- allocation globale correcte
- remplissage optimisé
- fragmentation fonctionnelle
- cohérence des données persistées
- UI reflète correctement la répartition

---

## 📦 Commits & PR
- commits atomiques
- PR séparées :
  1. DB + Models
  2. AllocationService
  3. Intégration + UI
