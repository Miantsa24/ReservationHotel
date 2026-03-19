// package service;

// import models.Vehicule;
// import org.junit.jupiter.api.Test;

// import java.lang.reflect.Field;
// import java.math.BigDecimal;
// import java.sql.SQLException;
// import java.util.ArrayList;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;

// public class VehiculeSelectionServiceTest {

//     // Helper test DAO to inject a controlled list of vehicles
//     static class TestVehiculeDAO extends dao.VehiculeDAO {
//         private List<Vehicule> list;
//         public TestVehiculeDAO(List<Vehicule> list) { this.list = list; }
//         @Override
//         public List<Vehicule> findAll() throws SQLException { return list; }
//     }

//     private VehiculeSelectionService prepareServiceWith(List<Vehicule> vehicules) throws Exception {
//         VehiculeSelectionService svc = new VehiculeSelectionService();
//         // inject test DAO
//         Field f = VehiculeSelectionService.class.getDeclaredField("vehiculeDAO");
//         f.setAccessible(true);
//         f.set(svc, new TestVehiculeDAO(vehicules));
//         return svc;
//     }

//     private Vehicule makeVehicule(String marque, int capacite, String carburant, int trajets) {
//         Vehicule v = new Vehicule();
//         v.setMarque(marque);
//         v.setCapacite(capacite);
//         v.setTypeCarburant(carburant);
//         v.setVitesseMoyenne(BigDecimal.valueOf(60));
//         v.setTempsAttente(30);
//         v.setTrajetsEffectues(trajets);
//         return v;
//     }

//     @Test
//     public void scenario1_capacityFiltering() throws Exception {
//         Vehicule v1 = makeVehicule("V1", 8, "Diesel", 1);
//         Vehicule v2 = makeVehicule("V2", 5, "Essence", 2);

//         List<Vehicule> list = new ArrayList<>(); list.add(v1); list.add(v2);
//         VehiculeSelectionService svc = prepareServiceWith(list);

//         Vehicule chosen = svc.selectionnerVehicule(6);
//         assertNotNull(chosen);
//         assertEquals("V1", chosen.getMarque());
//     }

//     @Test
//     public void scenario2_chooseLessTrajets() throws Exception {
//         Vehicule v1 = makeVehicule("V1", 8, "Diesel", 5);
//         Vehicule v2 = makeVehicule("V2", 8, "Essence", 2);

//         List<Vehicule> list = new ArrayList<>(); list.add(v1); list.add(v2);
//         VehiculeSelectionService svc = prepareServiceWith(list);

//         Vehicule chosen = svc.selectionnerVehicule(6);
//         assertNotNull(chosen);
//         assertEquals("V2", chosen.getMarque());
//     }

//     @Test
//     public void scenario3_randomOnFullTie() throws Exception {
//         Vehicule v1 = makeVehicule("V1", 8, "Diesel", 2);
//         Vehicule v2 = makeVehicule("V2", 8, "Diesel", 2);

//         List<Vehicule> list = new ArrayList<>(); list.add(v1); list.add(v2);
//         VehiculeSelectionService svc = prepareServiceWith(list);

//         Vehicule chosen = svc.selectionnerVehicule(6);
//         assertNotNull(chosen);
//         assertTrue(chosen.getMarque().equals("V1") || chosen.getMarque().equals("V2"));
//     }

//     @Test
//     public void scenario4_fuelPriority() throws Exception {
//         Vehicule v1 = makeVehicule("V1", 8, "Hybride", 2);
//         Vehicule v2 = makeVehicule("V2", 8, "Diesel", 2);

//         List<Vehicule> list = new ArrayList<>(); list.add(v1); list.add(v2);
//         VehiculeSelectionService svc = prepareServiceWith(list);

//         Vehicule chosen = svc.selectionnerVehicule(6);
//         assertNotNull(chosen);
//         assertEquals("V2", chosen.getMarque());
//     }
// }
