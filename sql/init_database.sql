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
    adresse VARCHAR(255),
    ville VARCHAR(100),
    etoiles INT CHECK (etoiles BETWEEN 1 AND 5),
    prix_par_nuit DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- Table des réservations
-- =============================================
CREATE TABLE IF NOT EXISTS reservations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    hotel_id INT NOT NULL,
    date_arrivee DATE NOT NULL,
    heure_arrivee TIME NOT NULL,
    date_depart DATE NOT NULL,
    nombre_personnes INT NOT NULL,
    nom_client VARCHAR(100) NOT NULL,
    email_client VARCHAR(100),
    telephone_client VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE
);


