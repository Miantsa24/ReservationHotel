package dao;

import models.Token;
import java.sql.*;
import java.util.UUID;

public class TokenDAO {

    /**
     * Génère un token avec un UUID et l'insère en base avec expiration.
     * @param expirationHours nombre d'heures avant expiration
     * @return le Token créé ou null en cas d'erreur
     */
    public Token generateAndInsertToken(int expirationHours) throws SQLException {
        String token = UUID.randomUUID().toString();
        Timestamp expiration = new Timestamp(System.currentTimeMillis() + (long) expirationHours * 60 * 60 * 1000);

        return insertToken(token, expiration);
    }

    /**
     * Insère un token avec une date d'expiration spécifique.
     * @param tokenValue valeur du token
     * @param expiration timestamp d'expiration
     * @return le Token créé ou null en cas d'erreur
     */
    public Token insertToken(String tokenValue, Timestamp expiration) throws SQLException {
        String sql = "INSERT INTO tokens (token, heure_expiration) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, tokenValue);
            stmt.setTimestamp(2, expiration);

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                return new Token(id, tokenValue, expiration);
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

    /**
     * Récupère le token le plus récent (valide ou non).
     * @return le Token le plus récent ou null si aucun
     */
    public Token getLatestToken() throws SQLException {
        String sql = "SELECT id, token, heure_expiration FROM tokens ORDER BY id DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return new Token(
                    rs.getInt("id"),
                    rs.getString("token"),
                    rs.getTimestamp("heure_expiration")
                );
            }
        }
        return null;
    }
}