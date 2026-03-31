package dao;

import models.Reservation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public void save(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservations (hotel_id, date_arrivee, heure_arrivee, nombre_personnes, ref_client, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, reservation.getHotelId());
            stmt.setDate(2, reservation.getDateArrivee());
            stmt.setTime(3, reservation.getHeureArrivee());
            stmt.setInt(4, reservation.getNombrePersonnes());
            stmt.setString(5, reservation.getRefClient());
            // status (default EN_ATTENTE if null)
            stmt.setString(6, reservation.getStatus() != null ? reservation.getStatus() : "EN_ATTENTE");
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reservation.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<Reservation> findAll() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, h.nom as hotel_nom FROM reservations r " +
                     "JOIN hotels h ON r.hotel_id = h.id ORDER BY r.date_arrivee DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Reservation reservation = mapResultSet(rs);
                reservations.add(reservation);
            }
        }
        return reservations;
    }

    public List<Reservation> findByDateArrivee(Date dateArrivee) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, h.nom as hotel_nom FROM reservations r " +
                     "JOIN hotels h ON r.hotel_id = h.id WHERE r.date_arrivee = ? ORDER BY r.heure_arrivee";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, dateArrivee);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = mapResultSet(rs);
                    reservations.add(reservation);
                }
            }
        }
        return reservations;
    }

    public Reservation findById(int id) throws SQLException {
        String sql = "SELECT r.*, h.nom as hotel_nom FROM reservations r LEFT JOIN hotels h ON r.hotel_id = h.id WHERE r.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retourne la liste des dates qui ont des réservations avec le statut donné.
     */
    public List<java.sql.Date> findDistinctDatesByStatus(String status) throws SQLException {
        List<java.sql.Date> dates = new ArrayList<>();
        String sql = "SELECT DISTINCT date_arrivee FROM reservations WHERE status = ? ORDER BY date_arrivee";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dates.add(rs.getDate("date_arrivee"));
                }
            }
        }
        return dates;
    }

    /**
     * Retourne les heures distinctes (TIME) pour une date donnée et un statut donné.
     */
    public List<java.sql.Time> findDistinctHoursByDateAndStatus(java.sql.Date date, String status) throws SQLException {
        List<java.sql.Time> hours = new ArrayList<>();
        String sql = "SELECT DISTINCT heure_arrivee FROM reservations WHERE date_arrivee = ? AND status = ? ORDER BY heure_arrivee";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, date);
            stmt.setString(2, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    hours.add(rs.getTime("heure_arrivee"));
                }
            }
        }
        return hours;
    }

    /**
     * Récupère les réservations pour un couple date+heure et un statut donné.
     */
    public List<Reservation> findByDateAndTimeAndStatus(java.sql.Date date, java.sql.Time time, String status) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, h.nom as hotel_nom FROM reservations r JOIN hotels h ON r.hotel_id = h.id WHERE r.date_arrivee = ? AND r.heure_arrivee = ? AND r.status = ? ORDER BY r.heure_arrivee";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, date);
            stmt.setTime(2, time);
            stmt.setString(3, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSet(rs));
                }
            }
        }
        return reservations;
    }

    /**
     * Met à jour le statut d'une réservation.
     */
    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE reservations SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM reservations WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Compte le nombre de réservations pour une date et un statut donné.
     */
    public int countByDateAndStatus(java.sql.Date date, String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE date_arrivee = ? AND status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, date);
            stmt.setString(2, status);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Reservation mapResultSet(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setId(rs.getInt("id"));
        reservation.setHotelId(rs.getInt("hotel_id"));
        reservation.setDateArrivee(rs.getDate("date_arrivee"));
        reservation.setHeureArrivee(rs.getTime("heure_arrivee"));
        reservation.setNombrePersonnes(rs.getInt("nombre_personnes"));
        reservation.setRefClient(rs.getString("ref_client"));
        try {
            String status = rs.getString("status");
            reservation.setStatus(status != null ? status : "EN_ATTENTE");
        } catch (SQLException e) {
            // colonne `status` peut ne pas exister sur d'anciennes bases; fallback
            reservation.setStatus("EN_ATTENTE");
        }
        reservation.setHotelNom(rs.getString("hotel_nom"));
        
        // Sprint 7 : assigned_count
        try {
            int assigned = rs.getInt("assigned_count");
            if (!rs.wasNull()) reservation.setAssignedCount(assigned);
        } catch (SQLException e) {
            // colonne peut ne pas exister; ignore
        }
        
        // Sprint 8 : priority_order, window_origin_id, first_window_time
        try {
            int priority = rs.getInt("priority_order");
            if (!rs.wasNull()) reservation.setPriorityOrder(priority);
        } catch (SQLException e) {
            // colonne peut ne pas exister; ignore
        }
        try {
            int windowOrigin = rs.getInt("window_origin_id");
            if (!rs.wasNull()) reservation.setWindowOriginId(windowOrigin);
        } catch (SQLException e) {
            // colonne peut ne pas exister; ignore
        }
        try {
            Timestamp firstWindow = rs.getTimestamp("first_window_time");
            if (firstWindow != null) reservation.setFirstWindowTime(firstWindow);
        } catch (SQLException e) {
            // colonne peut ne pas exister; ignore
        }
        
        return reservation;
    }

    /**
  * Met à jour le nombre de passagers assignés pour une réservation.
  */
    public void updateAssignedCount(int reservationId, int newAssignedCount) throws SQLException {
        String sql = "UPDATE reservations SET assigned_count = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newAssignedCount);
            stmt.setInt(2, reservationId);
            stmt.executeUpdate();
        }
    }

    // ===============================
    // Sprint 8 : Méthodes pour priorisation des non assignés
    // ===============================

    /**
     * Récupère les réservations non assignées (remaining > 0) pour une date donnée.
     * Triées par priorité (plus haute d'abord), puis par ancienneté (first_window_time).
     */
    public List<Reservation> findUnassignedPassengers(Date date) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, h.nom as hotel_nom FROM reservations r " +
                     "JOIN hotels h ON r.hotel_id = h.id " +
                     "WHERE r.date_arrivee = ? " +
                     "AND (r.nombre_personnes - r.assigned_count) > 0 " +
                     "ORDER BY r.priority_order DESC, r.first_window_time ASC, r.heure_arrivee ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, date);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSet(rs));
                }
            }
        }
        return reservations;
    }

    /**
     * Récupère les réservations non assignées pour une fenêtre temporelle donnée.
     * windowEnd est la fin de la fenêtre d'attente.
     */
    public List<Reservation> findUnassignedForWindow(Date date, Time windowEnd) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, h.nom as hotel_nom FROM reservations r " +
                     "JOIN hotels h ON r.hotel_id = h.id " +
                     "WHERE r.date_arrivee = ? " +
                     "AND r.heure_arrivee <= ? " +
                     "AND (r.nombre_personnes - r.assigned_count) > 0 " +
                     "ORDER BY r.priority_order DESC, r.first_window_time ASC, r.heure_arrivee ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, date);
            stmt.setTime(2, windowEnd);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSet(rs));
                }
            }
        }
        return reservations;
    }

    /**
     * Met à jour l'ordre de priorité d'une réservation.
     * Plus le nombre est élevé, plus la priorité est haute.
     */
    public void updatePriorityOrder(int reservationId, int priority) throws SQLException {
        String sql = "UPDATE reservations SET priority_order = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, priority);
            stmt.setInt(2, reservationId);
            stmt.executeUpdate();
        }
    }

    /**
     * Met à jour le timestamp de la première fenêtre pour une réservation.
     */
    public void updateFirstWindowTime(int reservationId, Timestamp firstWindowTime) throws SQLException {
        String sql = "UPDATE reservations SET first_window_time = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, firstWindowTime);
            stmt.setInt(2, reservationId);
            stmt.executeUpdate();
        }
    }

    /**
     * Met à jour l'ID de la fenêtre d'origine pour une réservation.
     */
    public void updateWindowOriginId(int reservationId, Integer windowOriginId) throws SQLException {
        String sql = "UPDATE reservations SET window_origin_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (windowOriginId != null) {
                stmt.setInt(1, windowOriginId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setInt(2, reservationId);
            stmt.executeUpdate();
        }
    }

    /**
     * Marque une réservation comme prioritaire (non assigné d'une fenêtre précédente).
     * Incrémente le priority_order pour s'assurer qu'elle passe en premier.
     */
    public void markAsPriority(int reservationId, Timestamp windowTime) throws SQLException {
        String sql = "UPDATE reservations SET priority_order = priority_order + 1, " +
                     "first_window_time = COALESCE(first_window_time, ?) WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, windowTime);
            stmt.setInt(2, reservationId);
            stmt.executeUpdate();
        }
    }

    /**
     * Réinitialise la priorité d'une réservation (après assignation complète).
     */
    public void resetPriority(int reservationId) throws SQLException {
        String sql = "UPDATE reservations SET priority_order = 0 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            stmt.executeUpdate();
        }
    }

    /**
     * Récupère les réservations dans une fenêtre temporelle (entre windowStart et windowEnd).
     */
    public List<Reservation> findInWindow(Date date, Time windowStart, Time windowEnd) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, h.nom as hotel_nom FROM reservations r " +
                     "JOIN hotels h ON r.hotel_id = h.id " +
                     "WHERE r.date_arrivee = ? " +
                     "AND r.heure_arrivee >= ? AND r.heure_arrivee <= ? " +
                     "ORDER BY r.nombre_personnes DESC, r.heure_arrivee ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, date);
            stmt.setTime(2, windowStart);
            stmt.setTime(3, windowEnd);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSet(rs));
                }
            }
        }
        return reservations;
    }
}
