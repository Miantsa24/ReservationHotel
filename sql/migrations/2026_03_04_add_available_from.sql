-- Migration Sprint 4
-- Ajoute la colonne available_from pour gérer la disponibilité des véhicules
-- Date: 2026-03-04

USE hotel_db;

-- 1) Ajouter colonne available_from (datetime nullable)
ALTER TABLE vehicules
  ADD COLUMN available_from DATETIME NULL COMMENT 'Timestamp when vehicle becomes available (returned to airport)';

-- 2) Optionnel : index pour rechercher rapidement les véhicules disponibles
CREATE INDEX idx_vehicules_available_from ON vehicules (available_from);

-- 3) Index pour accélérer les recherches sur réservations par date+heure
CREATE INDEX idx_reservations_date_heure ON reservations (date_arrivee, heure_arrivee);

-- Notes:
-- - La colonne est nullable: NULL signifie véhicule libre immédiatement.
-- - Les heures stockées doivent être en heure locale du système ou UTC selon la convention du projet.
-- - Après application, l'application doit mettre à jour `available_from` pour chaque véhicule lors des assignations.
