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
-- Table des tokens
-- =============================================
CREATE TABLE IF NOT EXISTS tokens (
    id INT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(255) NOT NULL,
    heure_expiration DATETIME NOT NULL
);


