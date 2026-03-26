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
(1, 'Colbert', 'COL'),
(2, 'Novotel', 'NOV'),
(3, 'Ibis', 'IBI'),
(4, 'Lokanga', 'LOK'),
(5, 'Hotel1', 'HT1'),
(6, 'Trano', 'TRN');

-- =============================================
-- Vérification des données insérées
-- =============================================
-- Insertion des distances (une seule entrée par paire, bidirectionnalité gérée côté code)
-- =============================================
INSERT INTO distance (`from`, `to`, km) VALUES
   ('Aeroport', 'Colbert', 10.00),
   ('Aeroport', 'Hotel1', 50.00),
   ('Aeroport', 'Novotel', 20.00),
   ('Aeroport', 'Ibis', 10.00),
   ('Aeroport', 'Lokanga', 25.00),
   ('Aeroport', 'Trano', 30.00),
   ('Colbert', 'Novotel', 8.00),
   ('Colbert', 'Ibis', 6.00),
   ('Colbert', 'Lokanga', 12.00),
   ('Colbert', 'Trano', 20.00),
   ('Novotel', 'Ibis', 14.00),
   ('Novotel', 'Lokanga', 10.00),
   ('Novotel', 'Trano', 16.00),
   ('Ibis', 'Lokanga', 18.00),
   ('Ibis', 'Trano', 18.00),
   ('Lokanga', 'Trano', 12.00);

   INSERT INTO vehicules (id, marque, capacite, typeCarburant, vitesseMoyenne, tempsAttente, available_from, trajets_effectues) VALUES
(1, 'Vehicule 1', 8, 'diesel', 60.00, 30, NULL, 0),
(2, 'Vehicule 2', 3, 'diesel', 60.00, 30, NULL, 0);

