package dao;

import models.ReservationVehicule;
import models.Reservation;
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

    /**
     * Récupère les réservations (objets Reservation) assignées à un véhicule pour une date donnée.
     */
    public List<Reservation> findReservationsByVehiculeAndDate(int idVehicule, Date date) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.*, h.nom as hotel_nom " +
                     "FROM reservation_vehicule rv " +
                     "JOIN reservations r ON rv.id_reservation = r.id " +
                     "LEFT JOIN hotels h ON r.hotel_id = h.id " +
                     "WHERE rv.id_vehicule = ? AND r.date_arrivee = ? " +
                     "ORDER BY r.heure_arrivee";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idVehicule);
            stmt.setDate(2, date);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapReservationResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Calcule la capacité déjà occupée sur un véhicule pour une date+heure précise.
     * Retourne la somme des `nombre_personnes` des réservations assignées.
     */
    public int getOccupiedCapacityForDateTime(int idVehicule, Date date, Time time) throws SQLException {
        String sql = "SELECT SUM(r.nombre_personnes) as total " +
                     "FROM reservation_vehicule rv " +
                     "JOIN reservations r ON rv.id_reservation = r.id " +
                     "WHERE rv.id_vehicule = ? AND r.date_arrivee = ? AND r.heure_arrivee = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idVehicule);
            stmt.setDate(2, date);
            stmt.setTime(3, time);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    /**
     * Calcule la capacité occupée pour un véhicule sur une date (toutes heures confondues).
     */
    public int getOccupiedCapacityForDate(int idVehicule, Date date) throws SQLException {
        String sql = "SELECT SUM(r.nombre_personnes) as total " +
                     "FROM reservation_vehicule rv " +
                     "JOIN reservations r ON rv.id_reservation = r.id " +
                     "WHERE rv.id_vehicule = ? AND r.date_arrivee = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idVehicule);
            stmt.setDate(2, date);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    private ReservationVehicule mapResultSet(ResultSet rs) throws SQLException {
        ReservationVehicule rv = new ReservationVehicule();
        rv.setId(rs.getInt("id"));
        rv.setIdReservation(rs.getInt("id_reservation"));
        rv.setIdVehicule(rs.getInt("id_vehicule"));
        try {
            int trajetId = rs.getInt("vehicule_trajet_id");
            if (!rs.wasNull()) rv.setVehiculeTrajetId(trajetId);
        } catch (SQLException e) {
            // colonne peut ne pas exister selon schéma; ignorer
        }
        return rv;
    }

    /**
     * Met à jour la colonne `vehicule_trajet_id` pour une ligne `reservation_vehicule` donnée.
     * trajetId peut être null pour dissocier la réservation d'un trajet.
     */
    public void setTrajetId(int reservationVehiculeId, Integer trajetId) throws SQLException {
        String sql = "UPDATE reservation_vehicule SET vehicule_trajet_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (trajetId != null) {
                stmt.setInt(1, trajetId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setInt(2, reservationVehiculeId);
            stmt.executeUpdate();
        }
    }

    /**
     * Retourne toutes les associations `reservation_vehicule` liées à un trajet donné.
     */
    public List<ReservationVehicule> findByTrajetId(int trajetId) throws SQLException {
        List<ReservationVehicule> list = new ArrayList<>();
        String sql = "SELECT * FROM reservation_vehicule WHERE vehicule_trajet_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trajetId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    private Reservation mapReservationResultSet(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setHotelId(rs.getInt("hotel_id"));
        r.setDateArrivee(rs.getDate("date_arrivee"));
        r.setHeureArrivee(rs.getTime("heure_arrivee"));
        r.setNombrePersonnes(rs.getInt("nombre_personnes"));
        r.setRefClient(rs.getString("ref_client"));
        try {
            r.setHotelNom(rs.getString("hotel_nom"));
        } catch (SQLException e) {
            // ignore if not present
        }
        return r;
    }
}
