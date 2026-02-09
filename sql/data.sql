-- Utilisation de la base de données existante
USE hotel_db;

-- =============================================
-- Insertion des hôtels depuis l'image
-- =============================================
-- Note: On supprime d'abord les données de test si nécessaire
-- TRUNCATE TABLE reservations;
-- TRUNCATE TABLE hotels;

-- Insertion des hôtels avec leurs IDs spécifiques
INSERT INTO hotels (id, nom) VALUES
(1, 'Colbert'),
(2, 'Novotel'),
(3, 'Ibis'),
(4, 'Lokanga');

-- =============================================
-- Insertion des réservations depuis l'image
-- =============================================
INSERT INTO reservations (id, hotel_id, date_arrivee, heure_arrivee, nombre_personnes, nom_client) VALUES
(1, 3, '2026-02-05', '00:01:00', 11, 'Client_4631'),
(2, 3, '2026-02-05', '23:55:00', 1, 'Client_4394'),
(3, 1, '2026-02-09', '10:17:00', 2, 'Client_8054'),
(4, 2, '2026-02-01', '15:25:00', 4, 'Client_1432'),
(5, 1, '2026-01-28', '07:11:00', 4, 'Client_7861'),
(6, 1, '2026-01-28', '07:45:00', 5, 'Client_3308'),
(7, 2, '2026-02-28', '08:25:00', 13, 'Client_4484'),
(8, 2, '2026-02-28', '13:00:00', 8, 'Client_9687'),
(9, 1, '2026-02-15', '13:00:00', 7, 'Client_6302'),
(10, 4, '2026-02-18', '22:55:00', 1, 'Client_8640');

-- =============================================
-- Vérification des données insérées
-- =============================================
SELECT * FROM hotels ORDER BY id;

SELECT 
    r.id,
    r.hotel_id,
    h.nom as hotel_nom,
    r.date_arrivee,
    r.heure_arrivee,
    r.nombre_personnes,
    r.nom_client
FROM reservations r
JOIN hotels h ON r.hotel_id = h.id
ORDER BY r.id;