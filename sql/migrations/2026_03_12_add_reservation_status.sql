-- Migration: add reservation status column
-- Date: 2026-03-12

USE hotel_db;

-- Ajouter la colonne status pour indiquer l'état d'assignation des réservations
ALTER TABLE reservations
  ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE' COMMENT 'EN_ATTENTE | ASSIGNE | NON_ASSIGNE';

-- Index optionnel pour filtrer rapidement les réservations en attente
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations (status);
