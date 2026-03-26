-- Script d'initialisation de la base de données Hôtel
-- Exécuter ce script dans MySQL pour créer les tables et insérer les données

-- Création de la base de données
CREATE DATABASE IF NOT EXISTS hotel_db;
USE hotel_db;

-- =============================================
-- Table des hôtels
-- =============================================
CREATE TABLE IF NOT EXISTS hotels (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL
);

-- =============================================
-- Table des réservations
-- =============================================
CREATE TABLE IF NOT EXISTS reservations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    hotel_id INT NOT NULL,
    date_arrivee DATE NOT NULL,
    heure_arrivee TIME NOT NULL,
    nombre_personnes INT NOT NULL,
    ref_client VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
    FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE
);

-- =============================================
-- Table des véhicules
-- =============================================
CREATE TABLE IF NOT EXISTS vehicules (
    id INT PRIMARY KEY AUTO_INCREMENT,
    marque VARCHAR(100) NOT NULL,
    capacite INT NOT NULL,
    typeCarburant VARCHAR(50) NOT NULL,
    vitesseMoyenne DECIMAL(5,2) NOT NULL,
    tempsAttente INT NOT NULL
);

-- =============================================
-- Table des trajets / traceabilité des véhicules
-- =============================================
CREATE TABLE IF NOT EXISTS vehicule_trajet (
    id INT PRIMARY KEY AUTO_INCREMENT,
    vehicule_id INT NOT NULL,
    date DATE NOT NULL,
    heure_depart DATETIME NULL,
    heure_arrivee DATETIME NULL,
    liste_reservation JSON NULL,
    kilometrage_parcouru DECIMAL(8,2) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_vehicule_trajet_vehicule (vehicule_id),
    CONSTRAINT fk_vehicule_trajet_vehicule FOREIGN KEY (vehicule_id) REFERENCES vehicules(id) ON DELETE CASCADE
);

-- =============================================
-- Table des tokens
-- =============================================
CREATE TABLE IF NOT EXISTS tokens (
    id INT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(255) NOT NULL,
    heure_expiration DATETIME NOT NULL
);

-- =============================================
-- Table des distances entre lieux
-- (une seule entrée par paire, la bidirectionnalité est gérée côté code)
-- =============================================
CREATE TABLE IF NOT EXISTS distance (
    id INT PRIMARY KEY AUTO_INCREMENT,
    `from` VARCHAR(100) NOT NULL,
    `to` VARCHAR(100) NOT NULL,
    km DECIMAL(10,2) NOT NULL
);

-- =============================================
-- Table associative réservation-véhicule
-- =============================================
CREATE TABLE IF NOT EXISTS reservation_vehicule (
    id INT PRIMARY KEY AUTO_INCREMENT,
    id_reservation INT NOT NULL,
    id_vehicule INT NOT NULL,
    vehicule_trajet_id INT NULL,
    FOREIGN KEY (id_reservation) REFERENCES reservations(id) ON DELETE CASCADE,
    FOREIGN KEY (id_vehicule) REFERENCES vehicules(id) ON DELETE CASCADE,
    FOREIGN KEY (vehicule_trajet_id) REFERENCES vehicule_trajet(id) ON DELETE SET NULL
);

ALTER TABLE vehicules
  ADD COLUMN available_from DATETIME NULL COMMENT 'Timestamp when vehicle becomes available (returned to airport)';

-- 2) Optionnel : index pour rechercher rapidement les véhicules disponibles
CREATE INDEX idx_vehicules_available_from ON vehicules (available_from);

-- 3) Index pour accélérer les recherches sur réservations par date+heure
CREATE INDEX idx_reservations_date_heure ON reservations (date_arrivee, heure_arrivee);

-- Index optionnel pour filtrer rapidement les réservations en attente
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations (status);

ALTER TABLE `vehicules` 
   ADD COLUMN `trajets_effectues` INT DEFAULT 0;


-- //Sprint7
-- =============================================
-- 1) Ajouter assigned_count à reservations
-- =============================================
ALTER TABLE reservations
  ADD COLUMN assigned_count INT NOT NULL DEFAULT 0 COMMENT 'Nombre total de passagers assignés à des véhicules';

-- Mettre à jour assigned_count pour les réservations existantes
-- Comme reservation_vehicule n’a pas passengers_assigned pour l’instant, on initialise assigned_count à 0
UPDATE reservations
SET assigned_count = 0;

-- =============================================
-- 2) Ajouter passengers_assigned à reservation_vehicule
-- =============================================
ALTER TABLE reservation_vehicule
  ADD COLUMN passengers_assigned INT NOT NULL DEFAULT 0 COMMENT 'Nombre de passagers de cette réservation assignés à ce véhicule';

-- =============================================
-- 3) Index pour accélérer les calculs et recherches
-- =============================================
CREATE INDEX idx_reservation_vehicule_id_reservation ON reservation_vehicule(id_reservation);
CREATE INDEX idx_reservation_vehicule_id_vehicule ON reservation_vehicule(id_vehicule);

-- =============================================
-- 4) Contrainte logique (application côté code)
--    SUM(passengers_assigned) <= reservations.nombre_personnes
--    sera géré transactionnellement côté AllocationService
-- =============================================
-- Pas de contrainte SQL stricte ici pour éviter blocage sur insertions partielles


-- Sprint 7 Migration Script
-- Exécuter ce script sur la base hotel_db

-- 1. Ajouter assigned_count à reservations
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS assigned_count INT NOT NULL DEFAULT 0;

-- 2. Ajouter passengers_assigned à reservation_vehicule
ALTER TABLE reservation_vehicule ADD COLUMN IF NOT EXISTS passengers_assigned INT NOT NULL DEFAULT 0;

-- 3. Index pour performance (optionnel)
CREATE INDEX IF NOT EXISTS idx_rv_passengers ON reservation_vehicule(passengers_assigned);

-- 4. Vérification
SELECT 'Migration Sprint 7 terminée!' AS status;
SHOW COLUMNS FROM reservations LIKE 'assigned_count';
SHOW COLUMNS FROM reservation_vehicule LIKE 'passengers_assigned';
