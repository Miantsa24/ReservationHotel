# Sprint 8 – Priorisation des réservations non assignées

---

## Objectif

L'objectif du Sprint 8 est de **prioriser les passagers non assignés** (ceux qui n'ont pas pu être placés dans un véhicule lors d'une fenêtre précédente) lorsqu'un véhicule revient à l'aéroport et ouvre une nouvelle fenêtre d'attente.

---

## Règles générales d'une fenêtre

Dans chaque fenêtre, les priorités sont appliquées dans cet ordre :

1. **Passagers non assignés des fenêtres précédentes** (priorité absolue)
2. **Tri descendant** des réservations par nombre de passagers
3. **Règle d'optimisation du Sprint 7** (remplissage optimal du véhicule)
4. Toutes les autres règles restent en vigueur
5. **Objectif** : remplir le véhicule au maximum. S'il est plein → départ immédiat. Sinon → attendre la fin du délai de la fenêtre avant de partir.

> ⚠️ **Point critique** : L'heure de début de la nouvelle fenêtre = heure de retour du véhicule à l'aéroport.

---

## Cas 1 – Le véhicule revient et les non assignés remplissent exactement le véhicule

**Contexte :**
- Véhicule V1 : **10 places**, temps d'attente : **30 min**
- V1 vient de faire un trajet et revient à l'aéroport à **09:00**
- **10 passagers non assignés** attendent à l'aéroport

**Déroulement :**

Dès le retour de V1 à 09:00, les 10 non assignés sont prioritaires et montent immédiatement dans V1.

V1 est **plein** → départ immédiat, sans attendre la fin de la fenêtre (09:00–09:30).

```
09:00 → Retour V1 (10 places)
        10 non assignés montent immédiatement
        V1 plein → Départ immédiat
```

---

## Cas 2 – Le véhicule revient, les non assignés ne remplissent pas complètement le véhicule

**Contexte :**
- Véhicule V1 : **15 places**, temps d'attente : **30 min**
- V1 revient à l'aéroport à **09:00**
- **10 passagers non assignés** attendent

**Déroulement :**

Dès le retour de V1 à 09:00, les 10 non assignés montent immédiatement (priorité absolue).

Il reste **5 places libres**. V1 attend dans la fenêtre **09:00–09:30** pour tenter de remplir ces places avec de nouvelles réservations.

- Si de nouvelles réservations arrivent entre 09:00 et 09:30 → elles montent pour remplir les 5 places restantes
- Si aucune réservation n'arrive avant 09:30 → V1 part avec les 10 non assignés seulement

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

---

## Cas 3 – Report des non assignés d'une fenêtre vers la suivante (exemple complet)

### Rappel de la situation issue du Sprint 7

Fenêtre 1 : **08:00 – 08:20**

| Véhicule | Capacité | Passagers chargés |
|---|---|---|
| V1 | 8 places | 6 passagers Client 1 + 2 passagers Client 3 → **plein** |
| V2 | 3 places | 3 passagers Client 2 → **plein** |
| **Non assignés** | — | 1 passager Client 2 (r2) + 1 passager Client 3 (r3) |

À l'issue de la fenêtre 1, **2 passagers restent non assignés** : 1 de r2 et 1 de r3.

---

### Nouvelle fenêtre – Retour de V1 à 09:45

**Nouvelles réservations reçues :**

| Réservation | Heure | Passagers |
|---|---|---|
| r4 | 10:00 | 7 personnes |
| r5 | 10:15 | 5 personnes |
| r6 | 10:12 | 3 personnes |

**Véhicule disponible :** V1, **15 places**, temps d'attente **30 min**

**Fenêtre 2 : 09:45 – 10:15** *(début = heure de retour de V1)*

---

### Déroulement étape par étape

**Étape 1 – Non assignés en priorité**

Les 2 non assignés de la fenêtre 1 montent immédiatement dans V1.

```
V1 : 15 places → 2 non assignés montent
Places restantes : 13
```

**Étape 2 – Tri descendant des nouvelles réservations**

```
r4 : 7 passagers
r6 : 3 passagers  (arrivée 10:12 < 10:15, donc placée avant r5 par heure)
r5 : 5 passagers
```

Application de la règle Sprint 7 (optimisation) sur les 13 places restantes :

- **r4 (7 passagers)** → entre en premier. Places restantes : **13 − 7 = 6**
- **r5 (5 passagers)** → 6 − 5 = 1, r5 tient entièrement. Places restantes : **1**
- **r6 (3 passagers)** → 1 place restante, seulement **1 passager de r6** peut monter. Les **2 restants de r6** sont non assignés → reportés à la fenêtre suivante.

**Étape 3 – Départ**

À **10:15** (fin de la fenêtre d'attente de 30 min depuis 09:45), V1 part.

```
V1 au départ (fenêtre 2 : 09:45 – 10:15) :
  - 2 non assignés (fenêtre 1)
  - r4 : 7 passagers  → complet
  - r5 : 5 passagers  → complet
  - r6 : 1 passager   → partiel
  ─────────────────────────────
  Total : 15/15 → V1 plein

Non assignés reportés à la fenêtre 3 : 2 passagers de r6
```

**Fenêtre 3** commencera à l'heure de retour de V1 depuis ce trajet, et les 2 non assignés de r6 seront à nouveau prioritaires.

---

## Récapitulatif des règles Sprint 8

| Règle | Description |
|---|---|
| **Priorité non assignés** | Les passagers non assignés d'une fenêtre précédente montent toujours en premier dans le véhicule suivant |
| **Heure de début de fenêtre** | = Heure de retour du véhicule à l'aéroport |
| **Remplissage** | Tri descendant par nombre de passagers + optimisation Sprint 7 |
| **Véhicule plein** | Départ immédiat sans attendre la fin de la fenêtre |
| **Véhicule non plein** | Attendre la fin du délai, puis partir |
| **Nouveau report** | Les passagers non casés dans la fenêtre courante deviennent non assignés pour la fenêtre suivante |
