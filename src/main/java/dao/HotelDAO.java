package dao;

import models.Hotel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HotelDAO {

    public List<Hotel> findAll() throws SQLException {
        List<Hotel> hotels = new ArrayList<>();
        String sql = "SELECT * FROM hotels ORDER BY nom";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Hotel hotel = new Hotel();
                hotel.setId(rs.getInt("id"));
                hotel.setNom(rs.getString("nom"));
                hotel.setAdresse(rs.getString("adresse"));
                hotel.setVille(rs.getString("ville"));
                hotel.setEtoiles(rs.getInt("etoiles"));
                hotel.setPrixParNuit(rs.getBigDecimal("prix_par_nuit"));
                hotel.setCreatedAt(rs.getTimestamp("created_at"));
                hotels.add(hotel);
            }
        }
        return hotels;
    }

    public Hotel findById(int id) throws SQLException {
        String sql = "SELECT * FROM hotels WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Hotel hotel = new Hotel();
                    hotel.setId(rs.getInt("id"));
                    hotel.setNom(rs.getString("nom"));
                    hotel.setAdresse(rs.getString("adresse"));
                    hotel.setVille(rs.getString("ville"));
                    hotel.setEtoiles(rs.getInt("etoiles"));
                    hotel.setPrixParNuit(rs.getBigDecimal("prix_par_nuit"));
                    hotel.setCreatedAt(rs.getTimestamp("created_at"));
                    return hotel;
                }
            }
        }
        return null;
    }
}
