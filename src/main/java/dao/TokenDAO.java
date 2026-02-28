package dao;

import models.Token;
import java.sql.*;
import java.util.UUID;

public class TokenDAO {

    public Token generateAndInsertToken(int expirationHours) throws SQLException {
        String token = UUID.randomUUID().toString();
        Timestamp expiration = new Timestamp(System.currentTimeMillis() + (long) expirationHours * 60 * 60 * 1000);

        String sql = "INSERT INTO tokens (token, heure_expiration) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, token);
            stmt.setTimestamp(2, expiration);

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                return new Token(id, token, expiration);
            }
        }
        return null;
    }

    /**
     * Vérifie qu'un token existe et n'est pas expiré.
     * @param token valeur du token à vérifier
     * @return true si valide, false sinon
     * @throws SQLException en cas de problème de connexion
     */
    public boolean isValidToken(String token) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tokens WHERE token = ? AND heure_expiration > CURRENT_TIMESTAMP";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}