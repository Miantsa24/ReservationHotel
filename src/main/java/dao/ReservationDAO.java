package dao;

import models.Reservation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public void save(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservations (hotel_id, date_arrivee, heure_arrivee, nombre_personnes, ref_client) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, reservation.getHotelId());
            stmt.setDate(2, reservation.getDateArrivee());
            stmt.setTime(3, reservation.getHeureArrivee());
            stmt.setInt(4, reservation.getNombrePersonnes());
            stmt.setString(5, reservation.getRefClient());
            
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

    private Reservation mapResultSet(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setId(rs.getInt("id"));
        reservation.setHotelId(rs.getInt("hotel_id"));
        reservation.setDateArrivee(rs.getDate("date_arrivee"));
        reservation.setHeureArrivee(rs.getTime("heure_arrivee"));
        reservation.setNombrePersonnes(rs.getInt("nombre_personnes"));
        reservation.setRefClient(rs.getString("ref_client"));
        reservation.setHotelNom(rs.getString("hotel_nom"));
        return reservation;
    }
}
