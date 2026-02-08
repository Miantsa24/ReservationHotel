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