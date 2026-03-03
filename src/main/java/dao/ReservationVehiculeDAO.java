package dao;

import models.ReservationVehicule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationVehiculeDAO {

    /**
     * Insère une association réservation-véhicule
     */
    public void save(ReservationVehicule rv) throws SQLException {
        String sql = "INSERT INTO reservation_vehicule (id_reservation, id_vehicule) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, rv.getIdReservation());
            stmt.setInt(2, rv.getIdVehicule());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    rv.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    /**
     * Récupère le véhicule assigné à une réservation
     */
    public ReservationVehicule findByReservationId(int idReservation) throws SQLException {
        String sql = "SELECT * FROM reservation_vehicule WHERE id_reservation = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idReservation);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Récupère toutes les associations pour un véhicule donné
     */
    public List<ReservationVehicule> findByVehiculeId(int idVehicule) throws SQLException {
        List<ReservationVehicule> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation_vehicule WHERE id_vehicule = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idVehicule);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    private ReservationVehicule mapResultSet(ResultSet rs) throws SQLException {
        ReservationVehicule rv = new ReservationVehicule();
        rv.setId(rs.getInt("id"));
        rv.setIdReservation(rs.getInt("id_reservation"));
        rv.setIdVehicule(rs.getInt("id_vehicule"));
        return rv;
    }
}
