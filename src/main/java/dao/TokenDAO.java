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
}