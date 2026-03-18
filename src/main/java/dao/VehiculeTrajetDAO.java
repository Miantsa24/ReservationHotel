package dao;

import models.VehiculeTrajet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculeTrajetDAO {

    public void save(VehiculeTrajet trajet) throws SQLException {
        if (trajet.getId() == 0) {
            String sql = "INSERT INTO vehicule_trajet (vehicule_id, date, heure_depart, heure_arrivee, liste_reservation, kilometrage_parcouru) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, trajet.getVehiculeId());
                stmt.setDate(2, trajet.getDate());
                stmt.setTimestamp(3, trajet.getHeureDepart());
                stmt.setTimestamp(4, trajet.getHeureArrivee());
                stmt.setString(5, trajet.getListeReservation());
                stmt.setDouble(6, trajet.getKilometrageParcouru());
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) trajet.setId(rs.getInt(1));
                }
            }
        } else {
            String sql = "UPDATE vehicule_trajet SET vehicule_id = ?, date = ?, heure_depart = ?, heure_arrivee = ?, liste_reservation = ?, kilometrage_parcouru = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, trajet.getVehiculeId());
                stmt.setDate(2, trajet.getDate());
                stmt.setTimestamp(3, trajet.getHeureDepart());
                stmt.setTimestamp(4, trajet.getHeureArrivee());
                stmt.setString(5, trajet.getListeReservation());
                stmt.setDouble(6, trajet.getKilometrageParcouru());
                stmt.setInt(7, trajet.getId());
                stmt.executeUpdate();
            }
        }
    }

    public VehiculeTrajet findById(int id) throws SQLException {
        String sql = "SELECT * FROM vehicule_trajet WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        }
        return null;
    }

    public List<VehiculeTrajet> findByVehiculeIdAndDate(int vehiculeId, Date date) throws SQLException {
        String sql = "SELECT * FROM vehicule_trajet WHERE vehicule_id = ? AND date = ? ORDER BY heure_depart";
        List<VehiculeTrajet> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vehiculeId);
            stmt.setDate(2, date);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.add(mapResultSet(rs));
            }
        }
        return result;
    }

    public List<VehiculeTrajet> findByDate(Date date) throws SQLException {
        String sql = "SELECT * FROM vehicule_trajet WHERE date = ? ORDER BY vehicule_id, heure_depart";
        List<VehiculeTrajet> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, date);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.add(mapResultSet(rs));
            }
        }
        return result;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM vehicule_trajet WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private VehiculeTrajet mapResultSet(ResultSet rs) throws SQLException {
        VehiculeTrajet t = new VehiculeTrajet();
        t.setId(rs.getInt("id"));
        t.setVehiculeId(rs.getInt("vehicule_id"));
        t.setDate(rs.getDate("date"));
        t.setHeureDepart(rs.getTimestamp("heure_depart"));
        t.setHeureArrivee(rs.getTimestamp("heure_arrivee"));
        try {
            t.setListeReservation(rs.getString("liste_reservation"));
        } catch (SQLException e) {
            // colonne peut ne pas exister selon version de la BDD; ignorer
        }
        t.setKilometrageParcouru(rs.getDouble("kilometrage_parcouru"));
        try { t.setCreatedAt(rs.getTimestamp("created_at")); } catch (SQLException ignored) {}
        try { t.setUpdatedAt(rs.getTimestamp("updated_at")); } catch (SQLException ignored) {}
        return t;
    }
}
