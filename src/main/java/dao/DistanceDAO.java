package dao;

import models.Distance;
import java.sql.*;
import java.text.Normalizer;

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
        if (distance != null) return distance.getKm();

        // Fallback: try accent-insensitive / encoding-robust match by normalizing DB values
        String normFrom = normalizeName(from);
        String normTo = normalizeName(to);

        String sql = "SELECT `from`, `to`, km FROM distance";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String dbFrom = rs.getString("from");
                String dbTo = rs.getString("to");
                double km = rs.getDouble("km");
                String nDbFrom = normalizeName(dbFrom);
                String nDbTo = normalizeName(dbTo);
                if ((nDbFrom.equals(normFrom) && nDbTo.equals(normTo)) || (nDbFrom.equals(normTo) && nDbTo.equals(normFrom))) {
                    return km;
                }
            }
        }

        return Double.POSITIVE_INFINITY;
    }

    private String normalizeName(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        // remove diacritics
        n = n.replaceAll("\\p{M}", "");
        // keep only letters and digits
        n = n.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        return n;
    }
}
