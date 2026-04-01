package dao;

import models.Vehicule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculeDAO {

    public void save(Vehicule vehicule) throws SQLException {
        String sql;
        if (vehicule.getId() == 0) {
            // Insert
            sql = "INSERT INTO vehicules (marque, capacite, typeCarburant, vitesseMoyenne, tempsAttente, available_from, trajets_effectues) VALUES (?, ?, ?, ?, ?, ?, ?)";
        } else {
            // Update
            sql = "UPDATE vehicules SET marque = ?, capacite = ?, typeCarburant = ?, vitesseMoyenne = ?, tempsAttente = ?, available_from = ?, trajets_effectues = ? WHERE id = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, vehicule.getId() == 0 ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {

            stmt.setString(1, vehicule.getMarque());
            stmt.setInt(2, vehicule.getCapacite());
            stmt.setString(3, vehicule.getTypeCarburant());
            stmt.setBigDecimal(4, vehicule.getVitesseMoyenne());
            stmt.setInt(5, vehicule.getTempsAttente());
            // available_from (nullable)
            if (vehicule.getAvailableFrom() != null) {
                stmt.setTimestamp(6, vehicule.getAvailableFrom());
            } else {
                stmt.setTimestamp(6, null);
            }
            stmt.setInt(7, vehicule.getTrajetsEffectues());

            if (vehicule.getId() != 0) {
                stmt.setInt(8, vehicule.getId());
            }

            stmt.executeUpdate();

            if (vehicule.getId() == 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        vehicule.setId(generatedKeys.getInt(1));
                    }
                }
            }
        }
    }

    public List<Vehicule> findAll() throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicules ORDER BY marque";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Vehicule vehicule = mapResultSet(rs);
                vehicules.add(vehicule);
            }
        }
        return vehicules;
    }

    public Vehicule findById(int id) throws SQLException {
        String sql = "SELECT * FROM vehicules WHERE id = ?";

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

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM vehicules WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Vehicule mapResultSet(ResultSet rs) throws SQLException {
        Vehicule vehicule = new Vehicule();
        vehicule.setId(rs.getInt("id"));
        vehicule.setMarque(rs.getString("marque"));
        vehicule.setCapacite(rs.getInt("capacite"));
        vehicule.setTypeCarburant(rs.getString("typeCarburant"));
        vehicule.setVitesseMoyenne(rs.getBigDecimal("vitesseMoyenne"));
        vehicule.setTempsAttente(rs.getInt("tempsAttente"));
        vehicule.setTrajetsEffectues(rs.getInt("trajets_effectues"));
        try {
            vehicule.setAvailableFrom(rs.getTimestamp("available_from"));
        } catch (SQLException e) {
            // colonne peut ne pas exister si migration non appliquée; ignorer
        }
        return vehicule;
    }

    /**
     * Met à jour la colonne available_from pour un véhicule.
     */
    public void updateAvailableFrom(int id, java.sql.Timestamp ts) throws SQLException {
        String sql = "UPDATE vehicules SET available_from = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, ts);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Récupère la valeur available_from pour un véhicule (peut être null).
     */
    public java.sql.Timestamp findAvailableFrom(int id) throws SQLException {
        String sql = "SELECT available_from FROM vehicules WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("available_from");
                }
            }
        }
        return null;
    }

    /**
     * Incrémente le compteur de trajets effectués pour un véhicule.
     */
    public void incrementTrajetsEffectues(int id) throws SQLException {
        String sql = "UPDATE vehicules SET trajets_effectues = trajets_effectues + 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // ===============================
    // Sprint 8 : Méthodes pour gestion retour véhicule
    // ===============================

    /**
     * Récupère les véhicules disponibles à partir d'une date/heure donnée.
     * Un véhicule est disponible si available_from est NULL ou <= au timestamp donné.
     * 
     * @param date La date de recherche
     * @param time L'heure à partir de laquelle chercher
     * @return Liste des véhicules disponibles, triés par capacité décroissante
     */
    public List<Vehicule> findAvailableFrom(Date date, Time time) throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        // Construire le timestamp à partir de date + time
        String sql = "SELECT * FROM vehicules " +
                     "WHERE available_from IS NULL " +
                     "   OR available_from <= ? " +
                     "ORDER BY capacite DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Combiner date et time en timestamp
            Timestamp ts = Timestamp.valueOf(date.toLocalDate().atTime(time.toLocalTime()));
            stmt.setTimestamp(1, ts);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapResultSet(rs));
                }
            }
        }
        return vehicules;
    }

    /**
     * Récupère le prochain véhicule qui sera disponible après une heure donnée.
     * Utile pour planifier la prochaine fenêtre d'attente.
     * 
     * @param date La date de recherche
     * @param afterTime L'heure après laquelle chercher
     * @return Le prochain véhicule disponible, ou null si aucun
     */
    public Vehicule findNextAvailable(Date date, Time afterTime) throws SQLException {
        // Construire le timestamp à partir de date + time
        String sql = "SELECT * FROM vehicules " +
                     "WHERE available_from > ? " +
                     "ORDER BY available_from ASC " +
                     "LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            Timestamp ts = Timestamp.valueOf(date.toLocalDate().atTime(afterTime.toLocalTime()));
            stmt.setTimestamp(1, ts);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Récupère tous les véhicules disponibles immédiatement (available_from IS NULL ou dans le passé).
     * 
     * @return Liste des véhicules disponibles maintenant
     */
    public List<Vehicule> findAvailableNow() throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicules " +
                     "WHERE available_from IS NULL " +
                     "   OR available_from <= NOW() " +
                     "ORDER BY capacite DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                vehicules.add(mapResultSet(rs));
            }
        }
        return vehicules;
    }

    /**
     * Récupère les véhicules qui reviendront dans une fenêtre temporelle donnée.
     * Utile pour anticiper les retours de véhicules.
     * 
     * @param date La date de recherche
     * @param windowStart Début de la fenêtre
     * @param windowEnd Fin de la fenêtre
     * @return Liste des véhicules qui reviendront dans cette fenêtre
     */
    public List<Vehicule> findReturningInWindow(Date date, Time windowStart, Time windowEnd) throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicules " +
                     "WHERE available_from >= ? " +
                     "  AND available_from <= ? " +
                     "ORDER BY available_from ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            Timestamp tsStart = Timestamp.valueOf(date.toLocalDate().atTime(windowStart.toLocalTime()));
            Timestamp tsEnd = Timestamp.valueOf(date.toLocalDate().atTime(windowEnd.toLocalTime()));
            stmt.setTimestamp(1, tsStart);
            stmt.setTimestamp(2, tsEnd);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapResultSet(rs));
                }
            }
        }
        return vehicules;
    }

    /**
     * Marque un véhicule comme en cours de trajet (non disponible).
     * Le véhicule sera disponible à returnTime.
     * 
     * @param vehiculeId ID du véhicule
     * @param returnTime Heure de retour prévue
     */
    public void markAsInTransit(int vehiculeId, Timestamp returnTime) throws SQLException {
        String sql = "UPDATE vehicules SET available_from = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, returnTime);
            stmt.setInt(2, vehiculeId);
            stmt.executeUpdate();
        }
    }

    /**
     * Marque un véhicule comme disponible immédiatement (retour à l'aéroport).
     * 
     * @param vehiculeId ID du véhicule
     */
    public void markAsAvailable(int vehiculeId) throws SQLException {
        String sql = "UPDATE vehicules SET available_from = NULL WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vehiculeId);
            stmt.executeUpdate();
        }
    }
}