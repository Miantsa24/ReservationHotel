// package dao;

// import models.Vehicule;
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// import java.math.BigDecimal;
// import java.sql.Connection;
// import java.sql.Statement;

// import static org.junit.jupiter.api.Assertions.*;

// public class VehiculeDAOTest {

//     private VehiculeDAO vehiculeDAO = new VehiculeDAO();

//     @BeforeEach
//     public void setUp() throws Exception {
//         try (Connection conn = DatabaseConnection.getConnection();
//              Statement stmt = conn.createStatement()) {
//             // Cleanup vehicules
//             stmt.executeUpdate("DELETE FROM reservation_vehicule");
//             stmt.executeUpdate("DELETE FROM vehicule_trajet");
//             stmt.executeUpdate("DELETE FROM vehicules");
//         }
//     }

//     @AfterEach
//     public void tearDown() {
//         DatabaseConnection.closeConnection();
//     }

//     @Test
//     public void testSaveAndFindById_trajetsEffectues() throws Exception {
//         // Arrange
//         Vehicule v = new Vehicule();
//         v.setMarque("TestVehicule");
//         v.setCapacite(8);
//         v.setTypeCarburant("Diesel");
//         v.setVitesseMoyenne(new BigDecimal("60.00"));
//         v.setTempsAttente(30);
//         v.setTrajetsEffectues(5);

//         // Act
//         vehiculeDAO.save(v);
//         assertTrue(v.getId() > 0, "L'ID devrait être généré après save");

//         Vehicule found = vehiculeDAO.findById(v.getId());

//         // Assert
//         assertNotNull(found, "Le véhicule devrait être trouvé");
//         assertEquals(5, found.getTrajetsEffectues(), "trajetsEffectues devrait être persisté");
//         assertEquals("TestVehicule", found.getMarque());
//     }

//     @Test
//     public void testUpdate_trajetsEffectues() throws Exception {
//         // Arrange - créer un véhicule
//         Vehicule v = new Vehicule();
//         v.setMarque("TestUpdate");
//         v.setCapacite(5);
//         v.setTypeCarburant("Essence");
//         v.setVitesseMoyenne(new BigDecimal("50.00"));
//         v.setTempsAttente(20);
//         v.setTrajetsEffectues(0);
//         vehiculeDAO.save(v);

//         // Act - modifier trajetsEffectues et sauvegarder
//         v.setTrajetsEffectues(10);
//         vehiculeDAO.save(v);

//         Vehicule found = vehiculeDAO.findById(v.getId());

//         // Assert
//         assertEquals(10, found.getTrajetsEffectues(), "trajetsEffectues devrait être mis à jour");
//     }

//     @Test
//     public void testIncrementTrajetsEffectues() throws Exception {
//         // Arrange - créer un véhicule avec trajetsEffectues = 0
//         Vehicule v = new Vehicule();
//         v.setMarque("TestIncrement");
//         v.setCapacite(6);
//         v.setTypeCarburant("Hybride");
//         v.setVitesseMoyenne(new BigDecimal("55.00"));
//         v.setTempsAttente(25);
//         v.setTrajetsEffectues(0);
//         vehiculeDAO.save(v);

//         assertEquals(0, vehiculeDAO.findById(v.getId()).getTrajetsEffectues());

//         // Act - incrémenter
//         vehiculeDAO.incrementTrajetsEffectues(v.getId());

//         // Assert
//         Vehicule after1 = vehiculeDAO.findById(v.getId());
//         assertEquals(1, after1.getTrajetsEffectues(), "trajetsEffectues devrait être 1 après incrémentation");

//         // Act - incrémenter encore
//         vehiculeDAO.incrementTrajetsEffectues(v.getId());
//         vehiculeDAO.incrementTrajetsEffectues(v.getId());

//         // Assert
//         Vehicule after3 = vehiculeDAO.findById(v.getId());
//         assertEquals(3, after3.getTrajetsEffectues(), "trajetsEffectues devrait être 3 après 3 incrémentations");
//     }

//     @Test
//     public void testDefaultTrajetsEffectues() throws Exception {
//         // Arrange - créer un véhicule sans définir trajetsEffectues explicitement
//         Vehicule v = new Vehicule();
//         v.setMarque("TestDefault");
//         v.setCapacite(4);
//         v.setTypeCarburant("Électrique");
//         v.setVitesseMoyenne(new BigDecimal("45.00"));
//         v.setTempsAttente(15);
//         // trajetsEffectues non défini, devrait être 0 par défaut

//         vehiculeDAO.save(v);

//         // Act
//         Vehicule found = vehiculeDAO.findById(v.getId());

//         // Assert
//         assertEquals(0, found.getTrajetsEffectues(), "trajetsEffectues devrait être 0 par défaut");
//     }
// }
