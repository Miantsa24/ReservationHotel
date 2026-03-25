package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class DatabaseConnection {
    // paramètres de connexion configurable via propriétés système ou variables d'environnement
    // ex: -Ddb.url=jdbc:mysql://localhost:3306/hotel_db?serverTimezone=UTC
    //      -Ddb.user=root -Ddb.password=secret
    private static final String URL = System.getProperty("db.url", "jdbc:mysql://localhost:3306/hotel_db?serverTimezone=UTC");
    private static final String USER = System.getProperty("db.user", "root");
    private static final String PASSWORD = System.getProperty("db.password", "");

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                // ensure database exists and schema is created on first connect
                ensureDatabase();
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver MySQL non trouvé", e);
            }
        }
        return connection;
    }

    // vérifie la présence de la base et crée les tables si nécessaire
    private static void ensureDatabase() throws SQLException {
        // create database if not exists using a separate connection
        // use same URL but without database name
        String baseUrl = URL.substring(0, URL.indexOf("/") + 1); // crude extraction
        try (Connection conn = DriverManager.getConnection(baseUrl, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS hotel_db");
        }

        // create tables if they are missing (simple DDL)
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            // hotels
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS hotels (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    nom VARCHAR(100) NOT NULL,
                    code VARCHAR(10) NOT NULL
                ) ENGINE=InnoDB;
            """);
            // reservations
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS reservations (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    hotel_id INT NOT NULL,
                    date_arrivee DATE NOT NULL,
                    heure_arrivee TIME NOT NULL,
                    nombre_personnes INT NOT NULL,
                    ref_client VARCHAR(50),
                    FOREIGN KEY (hotel_id) REFERENCES hotels(id)
                ) ENGINE=InnoDB;
            """);
            // adapt existing tables: add columns if missing
            // note: MySQL 8 supports ADD COLUMN IF NOT EXISTS directly
            stmt.executeUpdate("ALTER TABLE hotels ADD COLUMN IF NOT EXISTS code VARCHAR(10) NOT NULL");
            stmt.executeUpdate("ALTER TABLE reservations ADD COLUMN IF NOT EXISTS ref_client VARCHAR(50)");
            // vehicules
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS vehicules (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    marque VARCHAR(100),
                    capacite INT,
                    typeCarburant VARCHAR(10),
                    vitesseMoyenne DECIMAL(10,2),
                    tempsAttente INT
                ) ENGINE=InnoDB;
            """);
            // tokens
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS tokens (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    token VARCHAR(255) NOT NULL,
                    heure_expiration TIMESTAMP NOT NULL
                ) ENGINE=InnoDB;
            """);
            // si aucun token n'existe encore, insérer un token de test fixe
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM tokens")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String sample = "TEST-TOKEN-1234-ABCD";
                    Timestamp exp = new Timestamp(System.currentTimeMillis() + 24L * 60 * 60 * 1000); // +1 jour
                    stmt.executeUpdate("INSERT INTO tokens (token, heure_expiration) VALUES ('" + sample + "', '" + exp + "')");
                }
            }
            // distance
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS distance (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    `from` VARCHAR(100) NOT NULL,
                    `to` VARCHAR(100) NOT NULL,
                    km DECIMAL(10,2) NOT NULL
                ) ENGINE=InnoDB;
            """);
            // reservation_vehicule
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS reservation_vehicule (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    id_reservation INT NOT NULL,
                    id_vehicule INT NOT NULL,
                    FOREIGN KEY (id_reservation) REFERENCES reservations(id) ON DELETE CASCADE,
                    FOREIGN KEY (id_vehicule) REFERENCES vehicules(id) ON DELETE CASCADE
                ) ENGINE=InnoDB;
            """);
        }
    }
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
