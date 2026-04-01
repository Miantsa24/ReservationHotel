-- =============================================
-- Sprint 8 Migration Script
-- Priorisation des réservations non assignées
-- =============================================

USE hotel_db;

-- =============================================
-- 1) Vérification des colonnes Sprint 7 (pré-requis)
-- =============================================
-- Ces colonnes doivent exister avant Sprint 8
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN 'OK: assigned_count existe'
        ELSE 'ERREUR: assigned_count manquant!'
    END AS check_assigned_count
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'hotel_db' 
  AND TABLE_NAME = 'reservations' 
  AND COLUMN_NAME = 'assigned_count';

SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN 'OK: passengers_assigned existe'
        ELSE 'ERREUR: passengers_assigned manquant!'
    END AS check_passengers_assigned
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'hotel_db' 
  AND TABLE_NAME = 'reservation_vehicule' 
  AND COLUMN_NAME = 'passengers_assigned';

-- =============================================
-- 2) Ajouter priority_order à reservations
--    Ordre de priorité pour les non assignés (FIFO)
--    Plus le nombre est bas, plus la priorité est haute
-- =============================================
ALTER TABLE reservations
  ADD COLUMN IF NOT EXISTS priority_order INT NOT NULL DEFAULT 0 
  COMMENT 'Ordre de priorité pour non assignés (0 = normal, >0 = prioritaire)';

-- =============================================
-- 3) Ajouter window_origin_id à reservations
--    Référence à la fenêtre d'origine où la réservation
--    a été créée ou première tentative d'assignation
-- =============================================
ALTER TABLE reservations
  ADD COLUMN IF NOT EXISTS window_origin_id INT NULL 
  COMMENT 'ID de la fenêtre temporelle d''origine';

-- =============================================
-- 4) Ajouter first_window_time à reservations
--    Timestamp de la première fenêtre où la réservation
--    a été considérée (pour calcul d'ancienneté)
-- =============================================
ALTER TABLE reservations
  ADD COLUMN IF NOT EXISTS first_window_time DATETIME NULL 
  COMMENT 'Timestamp de la première fenêtre d''attente';

-- =============================================
-- 5) Index pour accélérer la recherche des non assignés
-- =============================================
-- Index pour trouver rapidement les réservations avec passagers restants
-- remaining = nombre_personnes - assigned_count > 0
CREATE INDEX IF NOT EXISTS idx_reservations_priority 
  ON reservations(priority_order, first_window_time);

-- Index composite pour requête des non assignés par date
CREATE INDEX IF NOT EXISTS idx_reservations_unassigned 
  ON reservations(date_arrivee, assigned_count, nombre_personnes);

-- =============================================
-- 6) Initialisation des données existantes
-- =============================================
-- Les réservations existantes avec remaining > 0 reçoivent une priorité
-- basée sur leur heure d'arrivée (plus ancien = priorité plus haute)
UPDATE reservations 
SET priority_order = 0,
    first_window_time = CONCAT(date_arrivee, ' ', heure_arrivee)
WHERE first_window_time IS NULL;

-- =============================================
-- 7) Vérification finale
-- =============================================
SELECT 'Migration Sprint 8 terminée!' AS status;

-- Afficher les nouvelles colonnes
SHOW COLUMNS FROM reservations LIKE 'priority_order';
SHOW COLUMNS FROM reservations LIKE 'window_origin_id';
SHOW COLUMNS FROM reservations LIKE 'first_window_time';

-- Résumé des colonnes de reservations après Sprint 8
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'hotel_db' AND TABLE_NAME = 'reservations'
ORDER BY ORDINAL_POSITION;
