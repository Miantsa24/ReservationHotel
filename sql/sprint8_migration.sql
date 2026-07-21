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
-- NOTE portabilité :
-- MySQL Server ne supporte pas "ADD COLUMN IF NOT EXISTS" / "CREATE INDEX IF NOT EXISTS"
-- (contrairement à MariaDB). Chaque ajout ci-dessous est donc gardé par une vérification
-- INFORMATION_SCHEMA + SQL préparé dynamique, ce qui rend ce script rejouable sans erreur
-- que la colonne/l'index existe déjà ou non, sur MySQL comme sur MariaDB.
-- =============================================

-- =============================================
-- 2) Ajouter priority_order à reservations
--    Ordre de priorité pour les non assignés (FIFO)
--    Plus le nombre est bas, plus la priorité est haute
-- =============================================
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reservations' AND COLUMN_NAME = 'priority_order');
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE reservations ADD COLUMN priority_order INT NOT NULL DEFAULT 0 COMMENT ''Ordre de priorite pour non assignes (0 = normal, >0 = prioritaire)''',
  'SELECT ''priority_order existe deja'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 3) Ajouter window_origin_id à reservations
--    Référence à la fenêtre d'origine où la réservation
--    a été créée ou première tentative d'assignation
-- =============================================
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reservations' AND COLUMN_NAME = 'window_origin_id');
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE reservations ADD COLUMN window_origin_id INT NULL COMMENT ''ID de la fenetre temporelle d''''origine''',
  'SELECT ''window_origin_id existe deja'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 4) Ajouter first_window_time à reservations
--    Timestamp de la première fenêtre où la réservation
--    a été considérée (pour calcul d'ancienneté)
-- =============================================
SET @col_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reservations' AND COLUMN_NAME = 'first_window_time');
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE reservations ADD COLUMN first_window_time DATETIME NULL COMMENT ''Timestamp de la premiere fenetre d''''attente''',
  'SELECT ''first_window_time existe deja'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 5) Index pour accélérer la recherche des non assignés
-- =============================================
-- Index pour trouver rapidement les réservations avec passagers restants
-- remaining = nombre_personnes - assigned_count > 0
SET @idx_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reservations' AND INDEX_NAME = 'idx_reservations_priority');
SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_reservations_priority ON reservations(priority_order, first_window_time)',
  'SELECT ''idx_reservations_priority existe deja'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Index composite pour requête des non assignés par date
SET @idx_exists := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reservations' AND INDEX_NAME = 'idx_reservations_unassigned');
SET @sql := IF(@idx_exists = 0,
  'CREATE INDEX idx_reservations_unassigned ON reservations(date_arrivee, assigned_count, nombre_personnes)',
  'SELECT ''idx_reservations_unassigned existe deja'' AS info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

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
