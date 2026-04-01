-- Utilisation de la base de données existante
USE hotel_db;

-- =============================================
-- Insertion des hôtels depuis l'image
-- =============================================
-- Note: On supprime d'abord les données de test si nécessaire
-- TRUNCATE TABLE reservations;
-- TRUNCATE TABLE hotels;

-- Insertion des hôtels avec leurs IDs spécifiques
INSERT INTO hotels (id, nom, code) VALUES
(1, 'hotel1', 'h1'),
(2, 'hotel2', 'h2');


-- =============================================
-- Vérification des données insérées
-- =============================================
-- Insertion des distances (une seule entrée par paire, bidirectionnalité gérée côté code)
-- =============================================
INSERT INTO distance (`from`, `to`, km) VALUES
   ('Aeroport', 'hotel1', 90.00),
   ('Aeroport', 'hotel2', 35.00),
   ('hotel1', 'hotel2', 60.00);

INSERT INTO vehicules (marque, capacite, typeCarburant, vitesseMoyenne, tempsAttente, available_from)
VALUES 
('vehicule1', 5, 'diesel', 50, 30, NULL),
('vehicule2', 5, 'essence', 50, 30, NULL),
('vehicule3', 12, 'diesel', 50, 30, NULL),
('vehicule4', 9, 'diesel', 50, 30, NULL),
('vehicule5', 12, 'essence', 50, 30, '2026-03-19 13:00:00');

INSERT INTO reservations (hotel_id, date_arrivee, heure_arrivee, nombre_personnes, ref_client, status) VALUES
(1, '2026-03-19', '09:00:00', 7,  'Client1', 'EN_ATTENTE'),
(2, '2026-03-19', '08:00:00', 20, 'Client2', 'EN_ATTENTE'),
(1, '2026-03-19', '09:10:00', 3,  'Client3', 'EN_ATTENTE'),
(1, '2026-03-19', '09:15:00', 10, 'Client4', 'EN_ATTENTE'),
(1, '2026-03-19', '09:20:00', 5,  'Client5', 'EN_ATTENTE'),
(1, '2026-03-19', '13:30:00', 12, 'Client6', 'EN_ATTENTE');
