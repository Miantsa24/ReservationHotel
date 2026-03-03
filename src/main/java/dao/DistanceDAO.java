package dao;

import models.Distance;
import java.sql.*;

public class DistanceDAO {

    /**
     * Récupère la distance entre deux lieux (bidirectionnel).
     * Ex: getDistance("Aéroport", "Colbert") retourne la même valeur que getDistance("Colbert", "Aéroport").
     */
    public Distance getDistance(String from, String to) throws SQLException {
        String sql = "SELECT id, `from`, `to`, km FROM distance WHERE (`from` = ? AND `to` = ?) OR (`from` = ? AND `to` = ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, from);
            stmt.setString(2, to);
            stmt.setString(3, to);
            stmt.setString(4, from);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Distance distance = new Distance();
                    distance.setId(rs.getInt("id"));
                    distance.setFrom(rs.getString("from"));
                    distance.setTo(rs.getString("to"));
                    distance.setKm(rs.getDouble("km"));
                    return distance;
                }
            }
        }
        return null;
    }

    /**
     * Récupère uniquement le nombre de km entre deux lieux (bidirectionnel).
     * Retourne 0 si aucune distance trouvée.
     */
    public double getKm(String from, String to) throws SQLException {
        Distance distance = getDistance(from, to);
        return distance != null ? distance.getKm() : 0;
    }
}
