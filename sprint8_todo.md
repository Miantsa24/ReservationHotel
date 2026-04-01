# Sprint 8 — Priorisation des réservations non assignées

## 🎯 Objectif du Sprint
Prioriser les **passagers non assignés** (ceux qui n'ont pas pu être placés lors d'une fenêtre précédente) lorsqu'un véhicule revient à l'aéroport et ouvre une nouvelle fenêtre d'attente.

---

## 🧠 Récapitulatif du fonctionnement

- Les **non assignés** des fenêtres précédentes ont une **priorité absolue**
- L'heure de début d'une nouvelle fenêtre = **heure de retour du véhicule** à l'aéroport
- Après les non assignés, application du **tri descendant** par nombre de passagers
- Ensuite, application de la **règle d'optimisation Sprint 7** (remplissage optimal)
- **Véhicule plein** → départ immédiat sans attendre la fin de la fenêtre
- **Véhicule non plein** → attendre la fin du délai, puis partir
- Les passagers non casés deviennent **non assignés** pour la fenêtre suivante

---

## 📌 Exemples du Sprint 8

### Cas 1 — Non assignés remplissent exactement le véhicule
```
09:00 → Retour V1 (10 places)
        10 non assignés montent immédiatement
        V1 plein → Départ immédiat
```

### Cas 2 — Non assignés ne remplissent pas le véhicule
```
09:00 → Retour V1 (15 places)
        10 non assignés montent immédiatement
        Places restantes : 5
        Fenêtre d'attente : 09:00 – 09:30
              ↓
        Nouvelles réservations disponibles ?
        OUI → Monter jusqu'à 5 passagers supplémentaires
        NON → Départ à 09:30 avec 10 passagers
```

### Cas 3 — Report fenêtre 1 → fenêtre 2 (exemple complet)

**Fenêtre 1 (08:00–08:20) :**
- V1 (8 places) : 6 passagers R1 + 2 passagers R3 → plein
- V2 (3 places) : 3 passagers R2 → plein
- **Non assignés** : 1 passager R2 + 1 passager R3

**Fenêtre 2 (09:45–10:15) — Retour V1 :**
1. 2 non assignés montent en priorité → reste 13 places
2. r4 (7 passagers) → reste 6 places
3. r5 (5 passagers) → reste 1 place
4. r6 (3 passagers) → 1 seul monte, 2 reportés

**Résultat** : V1 part plein (15/15), 2 passagers de r6 reportés à fenêtre 3

---

## 🔄 Changements majeurs introduits

### 1. Boucle métier continue
Avant (Sprint 7) : réservations → allocation → reste (oublié)
Maintenant : reste → retour véhicule → allocation → reste → ...

---

### 2. Méthode `traiterRetourVehicule()`
> **C'est le cœur du Sprint 8**

Cette méthode :
1. Détecte l'événement métier (V1.available_from = 09:45)
2. Cherche les passagers oubliés (`remaining > 0`)
3. Crée une nouvelle fenêtre (start = heure retour)
4. Construit l'allocation avec priorité : [non assignés] + [nouvelles réservations]
5. Relance `allocateForGroup(...)` du Sprint 7
6. Résultat : certains montent, d'autres restent pour le prochain retour

---

### 3. Priorité absolue des non assignés
Les passagers `remaining > 0` des fenêtres précédentes passent **AVANT** les nouvelles réservations.

---

### 4. Fenêtre dynamique
L'heure de début de fenêtre n'est plus fixe :
- **Nouvelle règle** : `windowStart = vehicule.available_from`

---

## 🚀 TODO LIST — SPRINT 8

## Rôles
- **Team Lead** : Code review, validation, merge, déploiement : Malala ETU003211
- **Dev1 (BackOffice)** : DB, modèles, DAO, algorithme : Alexandra ETU003306
- **Dev2 (BackOffice)** : Intégration, services, UI, tests : Tojo ETU003362

---

## 🔧 TL — Team Lead
1. Assigner les tâches via `GIT_SETUP_AND_TODO.md`
2. Valider la stratégie :
   - Boucle métier continue
   - Priorité non assignés
   - Fenêtre dynamique basée sur `available_from`
3. Revoir les PR et valider les tests
4. Gérer les merges : `main → staging → release`

---

## 🧠 Dev1 — Backend Core : Alexandra ETU003306

### 1. DB & Migration
- Vérifier que les colonnes Sprint 7 sont fonctionnelles :
  - `reservations.assigned_count`
  - `reservation_vehicule.passengers_assigned`
- Ajouter si nécessaire :
  - `reservations.priority_order INT DEFAULT 0` (ordre de priorité pour non assignés)
  - `reservations.window_origin_id INT` (référence à la fenêtre d'origine)

---

### 2. DAO — Nouvelles méthodes

#### ReservationDAO
```java
// Récupérer les réservations non assignées (remaining > 0)
List<Reservation> findUnassignedPassengers(Date date);

// Récupérer les non assignés pour une fenêtre donnée
List<Reservation> findUnassignedForWindow(Date date, Time windowEnd);

// Mettre à jour l'ordre de priorité
void updatePriorityOrder(int reservationId, int priority);
```

#### VehiculeDAO
```java
// Récupérer les véhicules disponibles à partir d'une heure
List<Vehicule> findAvailableFrom(Date date, Time time);

// Récupérer le prochain véhicule disponible
Vehicule findNextAvailable(Date date, Time afterTime);
```

---

### 3. Modification de `allocateForGroup()` — GroupingService

#### Nouvelle signature
```java
AllocationResult allocateForGroup(
    Date date,
    Time windowStart,
    List<Reservation> unassignedPriority,  // NON ASSIGNÉS EN PREMIER
    List<Reservation> newReservations,      // NOUVELLES RÉSERVATIONS
    List<Vehicule> vehicules
)
```

#### Logique modifiée
```
1. PRIORITÉ ABSOLUE : non assignés d'abord
   - Pour chaque véhicule :
     - D'abord placer les unassignedPriority
     - Puis appliquer l'algorithme Sprint 7 sur newReservations

2. Ordre des non assignés :
   - Ordre FIFO (premier arrivé, premier servi)
   - OU par ancienneté de la fenêtre d'origine

3. Ensuite seulement :
   - Tri descendant par nombre de passagers
   - Scoring Sprint 7 (minimiser places_restantes - passagers)
```

---

### 4. Nouvelle méthode `traiterRetourVehicule()` — GroupingService

> **MÉTHODE CENTRALE DU SPRINT 8**

```java
public AllocationResult traiterRetourVehicule(int vehiculeId, Date date, Time returnTime) {
    // 1. Récupérer le véhicule
    Vehicule v = vehiculeDAO.findById(vehiculeId);
    
    // 2. Vérifier qu'il est disponible
    if (v.getAvailableFrom().after(returnTime)) {
        return null; // pas encore disponible
    }
    
    // 3. Récupérer les non assignés (PRIORITÉ)
    List<Reservation> unassigned = reservationDAO.findUnassignedPassengers(date);
    
    // 4. Définir la nouvelle fenêtre
    Time windowStart = returnTime;
    Time windowEnd = addMinutes(returnTime, v.getTempsAttente());
    
    // 5. Récupérer les nouvelles réservations dans cette fenêtre
    List<Reservation> newReservations = reservationDAO.findInWindow(date, windowStart, windowEnd);
    
    // 6. Lancer l'allocation avec priorité
    return allocateForGroup(date, windowStart, unassigned, newReservations, List.of(v));
}
```

---

### 5. Tests unitaires Dev1

#### GroupingServiceSprint8Dev1Test
```java
@Test
void testUnassignedHavePriority() {
    // 2 non assignés + 5 nouvelles réservations
    // Vérifier que les 2 non assignés sont placés EN PREMIER
}

@Test
void testVehicleFullWithUnassignedOnly() {
    // 10 non assignés, véhicule 10 places
    // Véhicule plein → départ immédiat
}

@Test
void testVehiclePartiallyFilled() {
    // 5 non assignés, véhicule 15 places
    // Reste 10 places pour nouvelles réservations
}

@Test
void testChainedWindows() {
    // Fenêtre 1 → reste 2
    // Fenêtre 2 → ces 2 sont prioritaires
    // Fenêtre 2 → reste 3
    // Fenêtre 3 → ces 3 sont prioritaires
}

@Test
void testTraiterRetourVehicule() {
    // Simuler retour véhicule à 09:45
    // Vérifier création nouvelle fenêtre
    // Vérifier appel allocateForGroup avec bons paramètres
}
```

---

## 🔗 Dev2 — Intégration : Tojo ETU003362

### 1. Intégration de `traiterRetourVehicule()` dans le flux

#### Option A — Appel automatique
```java
// Dans VehiculeTrajetService, après création du trajet retour
public void onVehiculeReturn(int vehiculeId, Date date, Time returnTime) {
    groupingService.traiterRetourVehicule(vehiculeId, date, returnTime);
}
```

#### Option B — Endpoint manuel (pour tests/debug)
```java
// AssignationController
@PostMapping("/retour-vehicule")
public String traiterRetour(
    @RequestParam int vehiculeId,
    @RequestParam Date date,
    @RequestParam Time returnTime
) {
    AllocationResult result = groupingService.traiterRetourVehicule(vehiculeId, date, returnTime);
    // Persister le résultat
    groupingService.persistAllocationResult(result);
    return "redirect:/tracabilite";
}
```

---

### 2. Modification du départ véhicule

#### Règle : Départ immédiat si plein
```java
// Dans GroupingService ou AssignationService
public void checkAndTriggerDeparture(Vehicule v, AllocationResult result) {
    int occupiedCapacity = result.getTotalAssigned();
    
    if (occupiedCapacity >= v.getCapacite()) {
        // Véhicule plein → départ immédiat
        triggerDeparture(v, result, LocalTime.now());
    } else {
        // Attendre fin de fenêtre
        scheduleDepartureAtWindowEnd(v, result);
    }
}
```

---

### 3. Mise à jour `available_from` après départ

```java
// Après le départ d'un véhicule
public void updateAvailableFrom(Vehicule v, VehiculeTrajet trajet) {
    Time returnTime = calculateReturnTime(trajet);
    v.setAvailableFrom(returnTime);
    vehiculeDAO.updateAvailableFrom(v.getId(), returnTime);
    
    // SPRINT 8 : Déclencher traitement retour
    // (peut être schedulé ou appelé immédiatement pour simulation)
}
```

---

### 4. VehiculeTrajet — Pas de changement majeur

- Conserver la logique Sprint 7
- Seules les réservations avec `passengers_assigned > 0` créent des trajets
- Le trajet retour déclenche `traiterRetourVehicule()`

---

### 5. Controllers

#### AssignationController — Nouveaux endpoints
```java
// Déclencher manuellement le traitement d'un retour véhicule
@PostMapping("/traiter-retour")
public String traiterRetourVehicule(
    @RequestParam int vehiculeId,
    @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Date date,
    @RequestParam @DateTimeFormat(pattern="HH:mm") Time returnTime,
    Model model
) {
    AllocationResult result = groupingService.traiterRetourVehicule(vehiculeId, date, returnTime);
    groupingService.persistAllocationResult(result);
    model.addAttribute("result", result);
    return "assignation-detail";
}

// Visualiser les non assignés actuels
@GetMapping("/non-assignes")
public String voirNonAssignes(@RequestParam Date date, Model model) {
    List<Reservation> unassigned = reservationDAO.findUnassignedPassengers(date);
    model.addAttribute("unassigned", unassigned);
    return "non-assignes";
}
```

---

### 6. JSP / UI

#### non-assignes.jsp (NOUVEAU)
```jsp
<h2>Passagers non assignés en attente</h2>
<table>
    <thead>
        <tr>
            <th>Réservation</th>
            <th>Client</th>
            <th>Passagers restants</th>
            <th>Fenêtre d'origine</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${unassigned}" var="r">
            <tr>
                <td>${r.id}</td>
                <td>${r.client.nom}</td>
                <td>${r.remaining}</td>
                <td>${r.windowOrigin}</td>
            </tr>
        </c:forEach>
    </tbody>
</table>
```

#### tracabilite-resultat.jsp — Modifications
- Ajouter une section "Non assignés reportés"
- Afficher l'historique des fenêtres
- Indiquer les passagers prioritaires

#### assignation-detail.jsp — Modifications
- Afficher clairement les non assignés (priorité)
- Séparer visuellement : [NON ASSIGNÉS] | [NOUVELLES RÉSERVATIONS]
- Bouton "Simuler retour véhicule"

---

### 7. Tests d'intégration Dev2

#### Sprint8IntegrationTest
```java
@Test
void testFullPipelineWithUnassigned() {
    // 1. Créer réservations fenêtre 1
    // 2. Exécuter allocation → certains restent
    // 3. Simuler retour véhicule
    // 4. Vérifier que les non assignés sont traités en priorité
    // 5. Vérifier persistance correcte
}

@Test
void testImmediateDepartureWhenFull() {
    // Véhicule plein avec non assignés uniquement
    // Vérifier départ immédiat (pas d'attente fenêtre)
}

@Test
void testWaitForWindowEndWhenNotFull() {
    // Véhicule partiellement rempli
    // Vérifier attente jusqu'à fin de fenêtre
}

@Test
void testMultipleWindowChain() {
    // Fenêtre 1 → Fenêtre 2 → Fenêtre 3
    // Vérifier report correct des non assignés
}
```

---

## ✅ Critères d'acceptation

- [ ] Les non assignés ont une **priorité absolue** sur les nouvelles réservations
- [ ] `traiterRetourVehicule()` crée une nouvelle fenêtre à partir de `available_from`
- [ ] Véhicule plein → départ immédiat
- [ ] Véhicule non plein → attente fin de fenêtre
- [ ] Les non assignés sont reportés correctement entre fenêtres
- [ ] L'UI affiche clairement les non assignés en attente
- [ ] Les tests couvrent les 3 cas du Sprint 8

---

## 📦 Commits & PR

- commits atomiques
- PR séparées :
  1. **DAO + méthodes findUnassigned** (Dev1)
  2. **allocateForGroup modifié + traiterRetourVehicule** (Dev1)
  3. **Intégration flux + Controllers** (Dev2)
  4. **UI non-assignes.jsp + modifications JSP** (Dev2)
  5. **Tests unitaires + intégration** (Dev1 + Dev2)

---

## 📊 ÉTAT D'AVANCEMENT SPRINT 8

### ✅ Tâches COMPLÉTÉES (Dev1 - Alexandra)

- [x] Vérifier colonnes Sprint 7 fonctionnelles (`assigned_count`, `passengers_assigned`)
- [x] Ajouter colonnes `priority_order`, `window_origin_id`, `first_window_time`
- [x] Migration SQL Sprint 8 (`sprint8_migration.sql`)
- [x] Modèle `Reservation.java` mis à jour avec champs Sprint 8
- [x] `ReservationDAO.findUnassignedPassengers()`
- [x] `ReservationDAO.findUnassignedForWindow()`
- [x] `ReservationDAO.findInWindow()`
- [x] `ReservationDAO.updatePriorityOrder()`
- [x] `ReservationDAO.updateFirstWindowTime()`
- [x] `ReservationDAO.markAsPriority()`
- [x] `ReservationDAO.resetPriority()`
- [x] `VehiculeDAO.findAvailableFrom(Date, Time)`
- [x] `VehiculeDAO.findNextAvailable(Date, Time)`
- [x] `VehiculeDAO.findAvailableNow()`
- [x] `VehiculeDAO.findReturningInWindow()`
- [x] `VehiculeDAO.markAsInTransit()`
- [x] `VehiculeDAO.markAsAvailable()`
- [x] Modifier `allocateForGroup()` pour accepter les non assignés en priorité → `allocateForGroupSprint8()`
- [x] Helper `copyReservations()` pour éviter mutation des objets
- [x] Helper `selectBestCandidate()` pour scoring Sprint 7
- [x] **Implémenter `traiterRetourVehicule()`** — MÉTHODE CENTRALE SPRINT 8
- [x] Helper `addMinutes(Time, int)` pour calcul fin de fenêtre
- [x] `isVehicleFull(Vehicule, AllocationResult)` pour départ immédiat
- [x] `traiterRetourVehiculeEtPersister()` version avec persistance auto
- [x] **Tests unitaires Sprint 8** → `GroupingServiceSprint8Dev1Test.java`
  - [x] `testUnassignedHavePriority()` - Priorité absolue des non assignés
  - [x] `testVehicleFullWithUnassignedOnly()` - Véhicule plein → départ immédiat
  - [x] `testVehiclePartiallyFilled()` - Véhicule partiel → attendre fin fenêtre
  - [x] `testChainedWindows()` - Chaîne de fenêtres avec report
  - [x] `testSprint8CompleteExample()` - Exemple complet Cas 3 du sprint8.md
  - [x] `testUnassignedFIFOOrder()` - Ordre FIFO par ancienneté
  - [x] `testAllocateForGroupSprint8AutoSeparation()` - Séparation automatique

### ✅ Tâches COMPLÉTÉES (Dev1 - Alexandra) - TOUTES TERMINÉES

### ❌ Tâches À FAIRE (Dev2 - Tojo)

- [ ] Intégrer `traiterRetourVehicule()` dans le flux (après retour véhicule)
- [ ] Logique départ immédiat si véhicule plein → `isVehicleFull()` implémenté ✓
- [ ] Mise à jour `available_from` après départ
- [ ] Endpoint `/traiter-retour`
- [ ] Endpoint `/non-assignes`
- [ ] `non-assignes.jsp`
- [ ] Modifications `tracabilite-resultat.jsp`
- [ ] Modifications `assignation-detail.jsp`
- [ ] Tests d'intégration Sprint 8

---

## 🔗 Dépendances

```
Sprint 7 (COMPLÉTÉ)
    │
    ├── allocateForGroup() ✅
    ├── persistAllocationResult() ✅
    ├── passengers_assigned ✅
    └── assigned_count / remaining ✅
           │
           ▼
Sprint 8 (EN COURS)
    │
    ├── findUnassignedPassengers() ⬅️ NOUVEAU
    ├── traiterRetourVehicule() ⬅️ NOUVEAU (CŒUR)
    ├── allocateForGroup() modifié ⬅️ PRIORITÉ NON ASSIGNÉS
    └── Départ immédiat si plein ⬅️ NOUVEAU
```

---

## 🎯 Prochaines étapes recommandées

1. **Compléter Sprint 7** : Brancher `persistAllocationResult` sur le flux de confirmation
2. **Implémenter `findUnassignedPassengers()`** : Base pour Sprint 8
3. **Modifier `allocateForGroup()`** : Ajouter paramètre `unassignedPriority`
4. **Implémenter `traiterRetourVehicule()`** : Cœur du Sprint 8
5. **Tester la chaîne complète** : Fenêtre 1 → Fenêtre 2 → Fenêtre 3

---

## ⚠️ Points critiques à ne pas oublier

1. **Sans `traiterRetourVehicule()`** : Les non assignés dorment à l'aéroport pour toujours ! 😂
2. **Priorité absolue** : Les non assignés passent AVANT les nouvelles réservations, TOUJOURS
3. **Fenêtre dynamique** : `windowStart = vehicule.available_from`, pas une heure fixe
4. **Boucle métier** : Sprint 8 n'est pas un one-shot, c'est une boucle continue
