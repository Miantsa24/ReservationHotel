-- Test seed data for Sprint 4 scenarios + expected results
USE hotel_db;

-- Clean test tables (careful in production)
DELETE FROM reservation_vehicule;
DELETE FROM reservations;
DELETE FROM vehicules;

-- Ensure hotels exist (re-use ids from data.sql)
INSERT IGNORE INTO hotels (id, nom, code) VALUES
(1, 'Colbert', 'COL'),
(2, 'Novotel', 'NOV'),
(3, 'Ibis', 'IBI'),
(4, 'Lokanga', 'LOK');

-- Create vehicles: id, marque, capacite, typeCarburant, vitesseMoyenne, tempsAttente
INSERT INTO vehicules (id, marque, capacite, typeCarburant, vitesseMoyenne, tempsAttente) VALUES
(1, 'Renault Master', 5, 'Diesel', 60.00, 5),    -- Diesel, capacity 5
(2, 'Peugeot Expert', 4, 'Essence', 55.00, 5),   -- Essence, capacity 4
(3, 'Mercedes Sprinter', 9, 'Diesel', 70.00, 5), -- Diesel, capacity 9
(4, 'Toyota Hiace', 7, 'Hybride', 65.00, 5);     -- Hybride, capacity 7

-- Scenario 1: Multi-assignment allowed
-- Two reservations same date+heure: 2 pers and 3 pers on 2026-03-04 10:00
-- Expected: both assigned to vehicle id=1 (Renault Master, cap 5, Diesel)
INSERT INTO reservations (id, hotel_id, date_arrivee, heure_arrivee, nombre_personnes, ref_client) VALUES
(100, 1, '2026-03-04', '10:00:00', 2, 'A-TEST-100'),
(101, 2, '2026-03-04', '10:00:00', 3, 'B-TEST-101');

-- Scenario 2: Vehicle temporarily unavailable (available_from filter)
-- Mark vehicle id=3 (Mercedes Sprinter) as busy until 2026-03-05 12:00:00
UPDATE vehicules SET available_from = '2026-03-05 12:00:00' WHERE id = 3;

-- Reservation that would have fit only in vehicle 3. If vehicle 3 is excluded, assignment should fail or use other vehicle.
-- Example: reservation 102 needs 9 persons on 2026-03-04 11:00 (only vehicle 3 capacity 9 fits)
-- Expected: allocation fails (no vehicle available) because vehicle 3 is busy; reservation should be rejected by the app.
INSERT INTO reservations (id, hotel_id, date_arrivee, heure_arrivee, nombre_personnes, ref_client) VALUES
(102, 2, '2026-03-04', '11:00:00', 9, 'C-TEST-102');

-- Scenario 3: Rejection due to insufficient total capacity
-- Reservation 103 with 12 persons (no vehicle can carry 12)
-- Expected: allocation fails and reservation is rolled back / deleted by the controller logic
INSERT INTO reservations (id, hotel_id, date_arrivee, heure_arrivee, nombre_personnes, ref_client) VALUES
(103, 1, '2026-03-04', '12:00:00', 12, 'D-TEST-103');

-- Distances (needed for Tracabilite/routing)
INSERT IGNORE INTO distance (`from`, `to`, km) VALUES
('Aéroport', 'Colbert', 15.00),
('Aéroport', 'Novotel', 22.50),
('Aéroport', 'Ibis', 10.00),
('Colbert', 'Novotel', 8.00),
('Colbert', 'Ibis', 5.00),
('Novotel', 'Ibis', 12.00);

-- Quick verification queries (run after the app processed assignments):
-- 1) Check which vehicle was assigned to reservations 100 and 101
-- SELECT * FROM reservation_vehicule WHERE id_reservation IN (100,101);

-- 2) Check reservation 102 (expected: no assignment)
-- SELECT * FROM reservation_vehicule WHERE id_reservation = 102;

-- 3) Check reservation 103 (expected: rolled back / not present in reservations table)
-- SELECT * FROM reservations WHERE id = 103;

-- Notes / Expected Results Summary:
-- - Reservations 100 and 101 (2+3 persons at 2026-03-04 10:00): both should be assigned to vehicle id=1 (Renault Master, cap 5) by the selection logic because combined persons = 5 <= capacity 5 and vehicle 1 has highest fuel priority (Diesel) among suitable vehicles.
-- - Reservation 102 (9 persons at 2026-03-04 11:00): expected allocation FAIL because vehicle id=3 (cap 9) was marked busy until 2026-03-05 12:00 and other vehicles have insufficient free capacity.
-- - Reservation 103 (12 persons): expected allocation FAIL (no vehicle has capacity >=12). The controller should rollback/delete the reservation and show the clear error message.

-- How to run the scenarios:
-- 1) Load this SQL into the DB: mysql -u root -p -D hotel_db < sql/test_seed_and_expected_results.sql
-- 2) Start the app (mvn package + deploy to Tomcat).
-- 3) Use the web UI to list reservations (/reservations) and/or create new ones using the form; the automatic assignment runs on reservation creation.
-- 4) After running the app, inspect `reservation_vehicule` to confirm assignments.
