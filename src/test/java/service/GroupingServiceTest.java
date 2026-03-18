// package service;

// import dao.DatabaseConnection;
// import dao.VehiculeDAO;
// import dao.ReservationDAO;
// import dao.ReservationVehiculeDAO;
// import dao.DistanceDAO;

// import models.Vehicule;

// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// import java.sql.Connection;
// import java.sql.Date;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.Statement;
// import java.sql.Time;
// import java.time.LocalDate;
// import java.util.Map;

// import static org.junit.jupiter.api.Assertions.*;

// public class GroupingServiceTest {

//     private GroupingService groupingService = new GroupingService();

//     @BeforeEach
//     public void setUp() throws Exception {
//         try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement()) {
//             // ensure schema has status column
//             try { stmt.executeUpdate("ALTER TABLE reservations ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'EN_ATTENTE'"); } catch (Exception ignored) {}

//             // cleanup
//             try { stmt.executeUpdate("DELETE FROM reservation_vehicule"); } catch (Exception ignored) {}
//             try { stmt.executeUpdate("DELETE FROM vehicule_trajet"); } catch (Exception ignored) {}
//             stmt.executeUpdate("DELETE FROM reservations");
//             stmt.executeUpdate("DELETE FROM vehicules");
//             stmt.executeUpdate("DELETE FROM hotels");
//             stmt.executeUpdate("DELETE FROM distance");

//             // insert hotels
//             stmt.executeUpdate("INSERT INTO hotels (nom, code) VALUES ('Colbert','C1')", Statement.RETURN_GENERATED_KEYS);
//             try (ResultSet rs = stmt.getGeneratedKeys()) { if (rs.next()) {} }
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
//             PreparedStatement ps = conn.prepareStatement("INSERT INTO reservations (hotel_id, date_arrivee, heure_arrivee, nombre_personnes, ref_client, status) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
//             // get hotel ids
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
//     public void testExampleGroupingAndAssignment() throws Exception {
//         Date date = Date.valueOf(LocalDate.now());
//         // run grouping/assignment
//         groupingService.assignGroupsForDate(date);

//         // verify reservation statuses and assignment counts
//         try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement()) {
//             ResultSet rs = stmt.executeQuery("SELECT status, COUNT(*) as c FROM reservations GROUP BY status");
//             boolean foundAssign = false;
//             while (rs.next()) {
//                 String status = rs.getString("status");
//                 int c = rs.getInt("c");
//                 if ("ASSIGNE".equals(status)) foundAssign = true;
//             }
//             assertTrue(foundAssign, "At least one reservation should be ASSIGNE");

//             // check reservation_vehicule associations
//             rs = stmt.executeQuery("SELECT COUNT(*) FROM reservation_vehicule");
//             int assoc = 0;
//             if (rs.next()) assoc = rs.getInt(1);
//             assertEquals(3, assoc, "Should have 3 reservation_vehicule associations (each reservation assigned)");

//             // get vehicle ids
//             int v1Id = -1, v2Id = -1;
//             try (PreparedStatement p = conn.prepareStatement("SELECT id FROM vehicules WHERE marque = ?")) {
//                 p.setString(1, "V1");
//                 try (ResultSet r = p.executeQuery()) { if (r.next()) v1Id = r.getInt(1); }
//                 p.setString(1, "V2");
//                 try (ResultSet r = p.executeQuery()) { if (r.next()) v2Id = r.getInt(1); }
//             }
//             assertTrue(v1Id > 0 && v2Id > 0, "Vehicle ids must exist");

//             // get reservation ids by time
//             int res08 = idFor(conn, date, Time.valueOf("08:00:00"));
//             int res0815 = idFor(conn, date, Time.valueOf("08:15:00"));
//             int res0820 = idFor(conn, date, Time.valueOf("08:20:00"));

//             // mapping reservation -> vehicule
//             try (PreparedStatement p = conn.prepareStatement("SELECT rv.id_reservation, rv.id_vehicule FROM reservation_vehicule rv JOIN reservations r ON rv.id_reservation = r.id WHERE r.date_arrivee = ?")) {
//                 p.setDate(1, date);
//                 try (ResultSet r = p.executeQuery()) {
//                     Map<Integer,Integer> map = new java.util.HashMap<>();
//                     while (r.next()) map.put(r.getInt("id_reservation"), r.getInt("id_vehicule"));

//                     assertEquals((Integer)v1Id, map.get(res08), "08:00 should be assigned to V1");
//                     assertEquals((Integer)v2Id, map.get(res0815), "08:15 should be assigned to V2");
//                     assertEquals((Integer)v1Id, map.get(res0820), "08:20 should be assigned to V1 (fitting remaining capacity)");
//                 }
//             }

//             // verify vehicule_trajet records
//             try (PreparedStatement p = conn.prepareStatement("SELECT id, vehicule_id, liste_reservation, heure_depart, heure_arrivee FROM vehicule_trajet WHERE date = ?")) {
//                 p.setDate(1, date);
//                 try (ResultSet r = p.executeQuery()) {
//                     int trajets = 0;
//                     boolean foundV1 = false, foundV2 = false;
//                     while (r.next()) {
//                         trajets++;
//                         int vehId = r.getInt("vehicule_id");
//                         String list = r.getString("liste_reservation");
//                         String hd = r.getString("heure_depart");
//                         String ha = r.getString("heure_arrivee");
//                         assertNotNull(list, "liste_reservation must be set");
//                         assertTrue(list.startsWith("["), "liste_reservation should be JSON array");
//                         assertNotNull(hd, "heure_depart must be set");
//                         // heure_arrivee may be null in edge cases but usually set
//                         if (vehId == v1Id) foundV1 = true;
//                         if (vehId == v2Id) foundV2 = true;
//                     }
//                     assertTrue(trajets >= 2, "Should have at least 2 vehicule_trajet records");
//                     assertTrue(foundV1 && foundV2, "Trajets for V1 and V2 must exist");
//                 }
//             }

//             // verify vehicule available_from updated (non-null)
//             try (PreparedStatement p = conn.prepareStatement("SELECT available_from FROM vehicules WHERE id = ?")) {
//                 p.setInt(1, v1Id);
//                 try (ResultSet r = p.executeQuery()) { if (r.next()) assertNotNull(r.getTimestamp(1), "V1.available_from should be set"); }
//                 p.setInt(1, v2Id);
//                 try (ResultSet r = p.executeQuery()) { if (r.next()) assertNotNull(r.getTimestamp(1), "V2.available_from should be set"); }
//             }
//         }
//     }

//     private int idFor(Connection conn, Date date, Time time) throws Exception {
//         try (PreparedStatement p = conn.prepareStatement("SELECT id FROM reservations WHERE date_arrivee = ? AND heure_arrivee = ?")) {
//             p.setDate(1, date);
//             p.setTime(2, time);
//             try (ResultSet r = p.executeQuery()) { if (r.next()) return r.getInt(1); }
//         }
//         throw new IllegalStateException("Reservation not found for " + date + " " + time);
//     }
// }
