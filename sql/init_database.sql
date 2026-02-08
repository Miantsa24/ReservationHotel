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

-- =============================================
-- Insertion des hôtels (données de test)
-- =============================================
INSERT INTO hotels (nom, adresse, ville, etoiles, prix_par_nuit) VALUES
('Hôtel Aéroport Premium', '1 Avenue de l\'Aéroport', 'Paris', 4, 150.00),
('Ibis Aéroport', '25 Rue du Terminal', 'Paris', 3, 85.00),
('Novotel Airport', '10 Boulevard des Voyageurs', 'Paris', 4, 120.00),
('Première Classe Aéroport', '5 Rue des Pilotes', 'Paris', 2, 55.00),
('Hilton Airport', '100 Avenue Charles de Gaulle', 'Paris', 5, 250.00),
('Mercure Escale', '15 Rue de l\'Escale', 'Paris', 3, 95.00),
('B&B Hôtel Aéroport', '8 Impasse du Décollage', 'Paris', 2, 60.00),
('Marriott Airport', '50 Avenue des Nations', 'Paris', 5, 280.00);

-- Vérification des insertions
SELECT * FROM hotels;
