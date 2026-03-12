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
        return reservation;
    }
}
