package service;

import dao.DatabaseConnection;
import dao.VehiculeDAO;
import dao.ReservationDAO;
import dao.HotelDAO;
import models.Vehicule;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AssignationServiceTest {

    private AssignationService assignationService = new AssignationService();
    private VehiculeDAO vehiculeDAO = new VehiculeDAO();
    private int vehiculeId;
    private int hotelId;

    @BeforeEach
    public void setUp() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            // Cleanup
            stmt.executeUpdate("DELETE FROM reservation_vehicule");
            stmt.executeUpdate("DELETE FROM vehicule_trajet");
            stmt.executeUpdate("DELETE FROM reservations");
            stmt.executeUpdate("DELETE FROM vehicules");
            stmt.executeUpdate("DELETE FROM hotels");
            stmt.executeUpdate("DELETE FROM distance");

            // Insert hotel
            stmt.executeUpdate("INSERT INTO hotels (nom, code) VALUES ('TestHotel','TH1')", Statement.RETURN_GENERATED_KEYS);
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) hotelId = rs.getInt(1);
            }

            // Insert vehicule avec trajets_effectues = 0
            stmt.executeUpdate("INSERT INTO vehicules (marque, capacite, typeCarburant, vitesseMoyenne, tempsAttente, trajets_effectues) VALUES ('TestCar', 8, 'Diesel', 60.00, 30, 0)", Statement.RETURN_GENERATED_KEYS);
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) vehiculeId = rs.getInt(1);
            }

            // Insert distance
            stmt.executeUpdate("INSERT INTO distance (`from`,`to`,km) VALUES ('Aéroport','TestHotel',10)");
        }
    }

    @AfterEach
    public void tearDown() {
        DatabaseConnection.closeConnection();
    }

    @Test
    public void testAssignationIncrementsTrajetsEffectues() throws Exception {
        // Arrange - créer une réservation EN_ATTENTE
        Date date = Date.valueOf(LocalDate.now().plusDays(1));
        Time heure = Time.valueOf("10:00:00");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO reservations (hotel_id, date_arrivee, heure_arrivee, nombre_personnes, ref_client, status) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, hotelId);
            ps.setDate(2, date);
            ps.setTime(3, heure);
            ps.setInt(4, 4);
            ps.setString(5, "TEST001");
            ps.setString(6, "EN_ATTENTE");
            ps.executeUpdate();
        }

        // Vérifier trajetsEffectues avant
        Vehicule before = vehiculeDAO.findById(vehiculeId);
        assertEquals(0, before.getTrajetsEffectues(), "trajetsEffectues devrait être 0 avant assignation");

        // Act - exécuter l'assignation
        Map<String, Object> result = assignationService.assignerCreneau(date, heure);

        // Assert - vérifier que l'assignation a réussi
        assertEquals(1, result.get("assigned"), "Une réservation devrait être assignée");

        // Vérifier trajetsEffectues après
        Vehicule after = vehiculeDAO.findById(vehiculeId);
        assertEquals(1, after.getTrajetsEffectues(), "trajetsEffectues devrait être 1 après assignation");
    }

    @Test
    public void testMultipleAssignationsIncrementOnce() throws Exception {
        // Arrange - créer plusieurs réservations pour le même créneau
        Date date = Date.valueOf(LocalDate.now().plusDays(2));
        Time heure = Time.valueOf("11:00:00");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO reservations (hotel_id, date_arrivee, heure_arrivee, nombre_personnes, ref_client, status) VALUES (?, ?, ?, ?, ?, ?)")) {
            // Réservation 1
            ps.setInt(1, hotelId);
            ps.setDate(2, date);
            ps.setTime(3, heure);
            ps.setInt(4, 2);
            ps.setString(5, "TEST002");
            ps.setString(6, "EN_ATTENTE");
            ps.executeUpdate();

            // Réservation 2
            ps.setInt(1, hotelId);
            ps.setDate(2, date);
            ps.setTime(3, heure);
            ps.setInt(4, 3);
            ps.setString(5, "TEST003");
            ps.setString(6, "EN_ATTENTE");
            ps.executeUpdate();
        }

        // Act - exécuter l'assignation
        Map<String, Object> result = assignationService.assignerCreneau(date, heure);

        // Assert
        assertEquals(2, result.get("assigned"), "Deux réservations devraient être assignées");

        // trajetsEffectues ne devrait être incrémenté qu'une seule fois
        // car toutes les réservations sont sur le même véhicule et même trajet
        Vehicule after = vehiculeDAO.findById(vehiculeId);
        assertEquals(1, after.getTrajetsEffectues(), "trajetsEffectues devrait être 1 (un seul trajet)");
    }
}
