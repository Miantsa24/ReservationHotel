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
(4, 'Lokanga', 'LOK');

-- =============================================
-- Insertion des réservations depuis l'image
-- =============================================
INSERT INTO reservations (id, hotel_id, date_arrivee, heure_arrivee, nombre_personnes, ref_client) VALUES
(1, 3, '2026-02-05', '00:01:00', 11, '4631'),
(2, 3, '2026-02-05', '23:55:00', 1, '4394'),
(3, 1, '2026-02-09', '10:17:00', 2, '8054'),
(4, 2, '2026-02-01', '15:25:00', 4, '1432'),
(5, 1, '2026-01-28', '07:11:00', 4, '7861'),
(6, 1, '2026-01-28', '07:45:00', 5, '3308'),
(7, 2, '2026-02-28', '08:25:00', 13, '4484'),
(8, 2, '2026-02-28', '13:00:00', 8, '9687'),
(9, 1, '2026-02-15', '13:00:00', 7, '6302'),
(10, 4, '2026-02-18', '22:55:00', 1, '8640');

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
    r.ref_client
FROM reservations r
JOIN hotels h ON r.hotel_id = h.id
ORDER BY r.id;