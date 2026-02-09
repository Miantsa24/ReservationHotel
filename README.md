# Hotel Reservation App

Application de réservation d'hôtel développée avec un framework MVC personnalisé.

## Prérequis

- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Tomcat 10.1+

## Installation

### 1. Cloner le projet avec les submodules

```bash
git clone --recurse-submodules https://github.com/Miantsa24/ReservationHotel.git
cd ReservationHotel
```

**Si vous avez déjà cloné le projet sans les submodules :**

```bash
git submodule update --init --recursive
```

### 2. Configurer la base de données

1. Démarrer MySQL
2. Exécuter le script SQL :

```bash
mysql -u root -p < sql/init_database.sql
```

Ou via phpMyAdmin, copiez-collez le contenu de `sql/init_database.sql`.

### 3. Compiler le projet

```bash
mvn clean package
```

### 4. Déployer sur Tomcat

Le WAR sera automatiquement copié dans le dossier `webapps` de Tomcat (configuré dans `pom.xml`).

### 5. Accéder à l'application

- **Formulaire de réservation** : http://localhost:8080/hotel-app-1.0-SNAPSHOT/reservation/form

## Structure du projet

- `src/main/java/controllers/` - Contrôleurs MVC
- `src/main/java/dao/` - Data Access Objects (connexion DB)
- `src/main/java/models/` - Modèles de données
- `src/main/webapp/WEB-INF/views/` - Pages JSP
- `framework/` - Framework MVC personnalisé (submodule Git)
- `sql/` - Scripts SQL

## Submodule Framework

Le dossier `framework/` est un submodule Git lié à : https://github.com/TojoTR/Framework_sprint

Pour mettre à jour le framework :

```bash
git submodule update --remote framework
```
