// package integration;

// import dao.DatabaseConnection;
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import service.GroupingService;
// import models.AssignmentProposal;

// import java.sql.*;
// import java.time.LocalDate;

// import static org.junit.jupiter.api.Assertions.*;

// public class ComputeConfirmIntegrationTest {

//     private GroupingService groupingService = new GroupingService();

//     @BeforeEach
//     public void setUp() throws Exception {
//         try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement()) {
//             // ensure schema
//             try { stmt.executeUpdate("ALTER TABLE reservations ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'EN_ATTENTE'"); } catch (Exception ignored) {}

//             // cleanup
//             try { stmt.executeUpdate("DELETE FROM reservation_vehicule"); } catch (Exception ignored) {}
//             try { stmt.executeUpdate("DELETE FROM vehicule_trajet"); } catch (Exception ignored) {}
//             stmt.executeUpdate("DELETE FROM reservations");
//             stmt.executeUpdate("DELETE FROM vehicules");
//             stmt.executeUpdate("DELETE FROM hotels");
//             stmt.executeUpdate("DELETE FROM distance");

//             // insert hotels
//             stmt.executeUpdate("INSERT INTO hotels (nom, code) VALUES ('Colbert','C1')");
//             stmt.executeUpdate("INSERT INTO hotels (nom, code) VALUES ('Ibis','I1')");
//             stmt.executeUpdate("INSERT INTO hotels (nom, code) VALUES ('Panorama','P1')");

//             // insert vehicles
//             stmt.executeUpdate("INSERT INTO vehicules (marque, capacite, typeCarburant, vitesseMoyenne, tempsAttente) VALUES ('V1',8,'Diesel',60,30)");
//             stmt.executeUpdate("INSERT INTO vehicules (marque, capacite, typeCarburant, vitesseMoyenne, tempsAttente) VALUES ('V2',5,'Essence',60,30)");

//             // insert distances
//             stmt.executeUpdate("INSERT INTO distance (`from`,`to`,km) VALUES ('Aéroport','Colbert',15)");
//             stmt.executeUpdate("INSERT INTO distance (`from`,`to`,km) VALUES ('Aéroport','Ibis',10)");
//             stmt.executeUpdate("INSERT INTO distance (`from`,`to`,km) VALUES ('Aéroport','Panorama',20)");
//             stmt.executeUpdate("INSERT INTO distance (`from`,`to`,km) VALUES ('Colbert','Ibis',5)");
//             stmt.executeUpdate("INSERT INTO distance (`from`,`to`,km) VALUES ('Colbert','Panorama',12)");
//             stmt.executeUpdate("INSERT INTO distance (`from`,`to`,km) VALUES ('Ibis','Panorama',8)");

//             // insert reservations for date
//             Date date = Date.valueOf(LocalDate.now());
//             PreparedStatement ps = conn.prepareStatement("INSERT INTO reservations (hotel_id, date_arrivee, heure_arrivee, nombre_personnes, ref_client, status) VALUES (?, ?, ?, ?, ?, ?)");
//             int colbertId = getHotelId(conn, "Colbert");
//             int ibisId = getHotelId(conn, "Ibis");
//             int panoramaId = getHotelId(conn, "Panorama");

//             ps.setInt(1, colbertId);
//             ps.setDate(2, date);
//             ps.setTime(3, Time.valueOf("08:00:00"));
//             ps.setInt(4, 6);
//             ps.setString(5, "C1");
//             ps.setString(6, "EN_ATTENTE");
//             ps.executeUpdate();

//             ps.setInt(1, ibisId);
//             ps.setDate(2, date);
//             ps.setTime(3, Time.valueOf("08:15:00"));
//             ps.setInt(4, 4);
//             ps.setString(5, "I1");
//             ps.setString(6, "EN_ATTENTE");
//             ps.executeUpdate();

//             ps.setInt(1, panoramaId);
//             ps.setDate(2, date);
//             ps.setTime(3, Time.valueOf("08:20:00"));
//             ps.setInt(4, 2);
//             ps.setString(5, "P1");
//             ps.setString(6, "EN_ATTENTE");
//             ps.executeUpdate();
//             ps.close();
//         }
//     }

//     private int getHotelId(Connection conn, String name) throws Exception {
//         try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM hotels WHERE nom = ?")) {
//             ps.setString(1, name);
//             try (ResultSet rs = ps.executeQuery()) {
//                 if (rs.next()) return rs.getInt(1);
//             }
//         }
//         throw new IllegalStateException("Hotel not found: " + name);
//     }

//     @AfterEach
//     public void tearDown() {
//         DatabaseConnection.closeConnection();
//     }

//     @Test
//     public void testComputeThenConfirmPersistsTrajets() throws Exception {
//         Date date = Date.valueOf(LocalDate.now());
//         AssignmentProposal proposal = groupingService.computeAssignmentsForDate(date);
//         assertNotNull(proposal);
//         assertFalse(proposal.getGroups().isEmpty(), "Groups should be computed");

//         // ensure vehicle summaries present and reservations proposed
//         assertFalse(proposal.getVehicleSummaries().isEmpty(), "Vehicle summaries should not be empty");

//         // persist
//         groupingService.persistAssignments(proposal);

//         // verify DB state: vehicule_trajet and reservation_vehicule present
//         try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement()) {
//             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM reservation_vehicule");
//             assertTrue(rs.next());
//             assertTrue(rs.getInt(1) > 0, "reservation_vehicule rows should exist after persist");

//             rs = stmt.executeQuery("SELECT COUNT(*) FROM vehicule_trajet");
//             assertTrue(rs.next());
//             assertTrue(rs.getInt(1) > 0, "vehicule_trajet rows should exist after persist");
//         }
//     }

//     @Test
//     public void testPersistAssignmentsRollbackOnError() throws Exception {
//         Date date = Date.valueOf(LocalDate.now());
//         AssignmentProposal proposal = groupingService.computeAssignmentsForDate(date);
//         assertNotNull(proposal);

//         // Introduce an invalid vehicle summary to force SQL error (negative vehicle id)
//         AssignmentProposal.VehicleSummary bad = new AssignmentProposal.VehicleSummary();
//         bad.vehiculeId = -9999; // non existing
//         bad.reservationIds.add(-12345); // invalid reservation id
//         proposal.getVehicleSummaries().put(bad.vehiculeId, bad);

//         // Attempt persist -> should throw SQLException and not leave partial inserts
//         try {
//             groupingService.persistAssignments(proposal);
//             fail("persistAssignments should have thrown an exception for invalid data");
//         } catch (SQLException e) {
//             // expected
//         }

//         // Verify no reservation_vehicule rows inserted
//         try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement()) {
//             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM reservation_vehicule");
//             assertTrue(rs.next());
//             assertEquals(0, rs.getInt(1), "No reservation_vehicule rows should exist after rollback");
//         }
//     }
// }
