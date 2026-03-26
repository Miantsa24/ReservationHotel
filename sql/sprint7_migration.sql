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
