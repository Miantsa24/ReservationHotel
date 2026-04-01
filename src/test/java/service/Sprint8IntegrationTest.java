package service;

import dao.ReservationDAO;
import dao.VehiculeDAO;
import dao.VehiculeTrajetDAO;
import models.Reservation;
import models.ReservationVehicule;
import models.Vehicule;
import org.junit.jupiter.api.*;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sprint 8 : Tests d'intégration Dev2
 * 
 * Tests du flux complet de priorisation des non assignés :
 * - Pipeline complet avec non assignés
 * - Départ immédiat si véhicule plein
 * - Attente fin de fenêtre si non plein
 * - Chaînage de fenêtres multiples
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Sprint8IntegrationTest {

    private GroupingService groupingService;
    private Date testDate;
    
    // Mock des DAO pour éviter les appels DB
    private static class TestReservationDAO extends ReservationDAO {
        private List<Reservation> mockUnassigned = new ArrayList<>();
        private List<Reservation> mockInWindow = new ArrayList<>();
        
        public void setMockUnassigned(List<Reservation> reservations) {
            this.mockUnassigned = reservations;
        }
        
        public void setMockInWindow(List<Reservation> reservations) {
            this.mockInWindow = reservations;
        }
        
        @Override
        public List<Reservation> findUnassignedPassengers(Date date) {
            return new ArrayList<>(mockUnassigned);
        }
        
        @Override
        public List<Reservation> findInWindow(Date date, Time windowStart, Time windowEnd) {
            return new ArrayList<>(mockInWindow);
        }
        
        @Override
        public void markAsPriority(int reservationId, Date date, Time windowStart) {
            // Mock - ne fait rien
        }
    }
    
    private static class TestVehiculeDAO extends VehiculeDAO {
        private Vehicule mockVehicule;
        
        public void setMockVehicule(Vehicule v) {
            this.mockVehicule = v;
        }
        
        @Override
        public Vehicule findById(int id) {
            return mockVehicule;
        }
        
        @Override
        public void markAsInTransit(int vehiculeId, Time returnTime) {
            // Mock - ne fait rien
        }
    }

    @BeforeEach
    void setUp() {
        groupingService = new GroupingService();
        testDate = Date.valueOf("2026-04-01");
    }

    // ===============================
    // Test 1 : Pipeline complet avec non assignés
    // ===============================
    @Test
    @Order(1)
    void testFullPipelineWithUnassigned() {
        // GIVEN: Fenêtre 1 - 12 passagers, véhicule 10 places
        Vehicule v1 = createVehicule(1, "Minibus A", 10, 30);
        
        // Réservations fenêtre 1
        Reservation r1 = createReservation(1, "Client A", 6, 0);  // 6 passagers
        Reservation r2 = createReservation(2, "Client B", 4, 0);  // 4 passagers
        Reservation r3 = createReservation(3, "Client C", 2, 0);  // 2 passagers - ne rentrera pas
        
        List<Reservation> unassigned = new ArrayList<>(); // Pas de prioritaires au départ
        List<Reservation> newReservations = List.of(r1, r2, r3);
        
        // WHEN: Allocation fenêtre 1
        GroupingService.AllocationResult result1 = groupingService.allocateForGroupSprint8(
            testDate,
            Time.valueOf("08:00:00"),
            unassigned,
            newReservations,
            v1
        );
        
        // THEN: 10 passagers assignés (r1=6, r2=4), r3 reste
        assertEquals(2, result1.assignments.size(), "2 réservations assignées");
        assertEquals(1, result1.remainingReservations.size(), "1 réservation reste");
        assertEquals(3, result1.remainingReservations.get(0).getId(), "Client C reste");
        
        // WHEN: Fenêtre 2 - Retour véhicule avec non assignés prioritaires
        Reservation r3Priority = createReservation(3, "Client C", 2, 0);
        r3Priority.setPriorityOrder(1);
        r3Priority.setFirstWindowTime(Timestamp.valueOf("2026-04-01 08:00:00"));
        
        Reservation r4 = createReservation(4, "Client D", 5, 0); // Nouvelle réservation
        
        List<Reservation> unassignedWindow2 = List.of(r3Priority);
        List<Reservation> newWindow2 = List.of(r4);
        
        GroupingService.AllocationResult result2 = groupingService.allocateForGroupSprint8(
            testDate,
            Time.valueOf("09:00:00"),
            unassignedWindow2,
            newWindow2,
            v1
        );
        
        // THEN: r3Priority assigné EN PREMIER, puis r4
        assertEquals(2, result2.assignments.size(), "2 réservations assignées fenêtre 2");
        
        // Vérifier l'ordre : prioritaire d'abord
        ReservationVehicule firstAssignment = result2.assignments.get(0);
        assertEquals(3, firstAssignment.getIdReservation(), "Client C (prioritaire) assigné en premier");
    }

    // ===============================
    // Test 2 : Départ immédiat si véhicule plein
    // ===============================
    @Test
    @Order(2)
    void testImmediateDepartureWhenFull() {
        // GIVEN: Véhicule 10 places, 10 non assignés prioritaires
        Vehicule v1 = createVehicule(1, "Minibus B", 10, 30);
        
        // 10 passagers non assignés = exactement la capacité
        List<Reservation> unassigned = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Reservation r = createReservation(i, "Priority " + i, 2, 0);
            r.setPriorityOrder(i);
            r.setFirstWindowTime(Timestamp.valueOf("2026-04-01 07:00:00"));
            unassigned.add(r);
        }
        
        // Nouvelles réservations (ne devraient PAS être prises car véhicule plein)
        Reservation newRes = createReservation(100, "New Client", 3, 0);
        List<Reservation> newReservations = List.of(newRes);
        
        // WHEN: Allocation
        GroupingService.AllocationResult result = groupingService.allocateForGroupSprint8(
            testDate,
            Time.valueOf("09:00:00"),
            unassigned,
            newReservations,
            v1
        );
        
        // THEN: Véhicule plein avec prioritaires uniquement
        assertTrue(result.vehicleFull, "Véhicule doit être marqué comme plein");
        assertEquals(5, result.assignments.size(), "5 réservations prioritaires assignées");
        assertEquals(10, result.getTotalAssigned(), "10 passagers au total");
        
        // La nouvelle réservation doit rester
        assertEquals(1, result.remainingReservations.size(), "Nouvelle réservation reste");
        assertEquals(100, result.remainingReservations.get(0).getId());
        
        // Vérifier décision de départ
        GroupingService.DepartureDecision decision = groupingService.checkAndTriggerDeparture(v1, result);
        assertTrue(decision.immediateDepart, "Départ immédiat car véhicule plein");
        assertEquals("Véhicule plein - départ immédiat", decision.reason);
    }

    // ===============================
    // Test 3 : Attente fin de fenêtre si non plein
    // ===============================
    @Test
    @Order(3)
    void testWaitForWindowEndWhenNotFull() {
        // GIVEN: Véhicule 15 places, seulement 8 passagers
        Vehicule v1 = createVehicule(1, "Grand Bus", 15, 30);
        
        // 5 non assignés prioritaires (total 5 passagers)
        Reservation r1 = createReservation(1, "Priority A", 3, 0);
        r1.setPriorityOrder(1);
        
        Reservation r2 = createReservation(2, "Priority B", 2, 0);
        r2.setPriorityOrder(2);
        
        List<Reservation> unassigned = List.of(r1, r2);
        
        // 3 nouvelles réservations (total 3 passagers)
        Reservation r3 = createReservation(3, "New Client", 3, 0);
        List<Reservation> newReservations = List.of(r3);
        
        // WHEN: Allocation
        Time windowStart = Time.valueOf("10:00:00");
        GroupingService.AllocationResult result = groupingService.allocateForGroupSprint8(
            testDate,
            windowStart,
            unassigned,
            newReservations,
            v1
        );
        result.windowEnd = GroupingService.addMinutes(windowStart, v1.getTempsAttente());
        
        // THEN: 8 passagers sur 15 places
        assertFalse(result.vehicleFull, "Véhicule ne doit PAS être plein");
        assertEquals(8, result.getTotalAssigned(), "8 passagers assignés");
        assertEquals(3, result.assignments.size(), "3 réservations assignées");
        
        // Vérifier décision de départ
        GroupingService.DepartureDecision decision = groupingService.checkAndTriggerDeparture(v1, result);
        assertFalse(decision.immediateDepart, "Pas de départ immédiat");
        assertEquals(result.windowEnd, decision.departureTime, "Départ à la fin de la fenêtre");
        assertTrue(decision.reason.contains("attendre"), "Message indique attente");
    }

    // ===============================
    // Test 4 : Chaînage de fenêtres multiples
    // ===============================
    @Test
    @Order(4)
    void testMultipleWindowChain() {
        // GIVEN: Véhicule 5 places pour forcer des restes à chaque fenêtre
        Vehicule v1 = createVehicule(1, "Petit Van", 5, 20);
        
        // === FENÊTRE 1 ===
        // 8 passagers, véhicule 5 places → 3 restent
        Reservation r1 = createReservation(1, "F1-A", 3, 0);
        Reservation r2 = createReservation(2, "F1-B", 3, 0);
        Reservation r3 = createReservation(3, "F1-C", 2, 0);
        
        GroupingService.AllocationResult result1 = groupingService.allocateForGroupSprint8(
            testDate,
            Time.valueOf("08:00:00"),
            new ArrayList<>(), // Pas de prioritaires
            List.of(r1, r2, r3),
            v1
        );
        
        // Vérifier fenêtre 1 : 5 passagers pris, reste les autres
        assertEquals(5, result1.getTotalAssigned(), "Fenêtre 1: 5 passagers");
        assertTrue(result1.remainingReservations.size() > 0, "Fenêtre 1: des restants");
        
        int remainingF1 = 0;
        for (Reservation r : result1.remainingReservations) {
            remainingF1 += r.getRemaining();
        }
        assertEquals(3, remainingF1, "Fenêtre 1: 3 passagers restants");
        
        // === FENÊTRE 2 ===
        // Les restants de F1 deviennent prioritaires
        List<Reservation> priorityF2 = new ArrayList<>();
        for (Reservation r : result1.remainingReservations) {
            r.setPriorityOrder(1);
            r.setFirstWindowTime(Timestamp.valueOf("2026-04-01 08:00:00"));
            priorityF2.add(r);
        }
        
        // Nouvelles réservations fenêtre 2
        Reservation r4 = createReservation(4, "F2-A", 4, 0);
        
        GroupingService.AllocationResult result2 = groupingService.allocateForGroupSprint8(
            testDate,
            Time.valueOf("09:00:00"),
            priorityF2,
            List.of(r4),
            v1
        );
        
        // Vérifier fenêtre 2 : prioritaires d'abord
        assertEquals(5, result2.getTotalAssigned(), "Fenêtre 2: 5 passagers");
        
        // Le premier assigné doit être un prioritaire
        ReservationVehicule firstF2 = result2.assignments.get(0);
        Reservation firstRes = firstF2.getReservation();
        assertTrue(firstRes.isPriority(), "Fenêtre 2: prioritaire assigné en premier");
        
        // === FENÊTRE 3 ===
        // Les restants de F2 deviennent prioritaires
        List<Reservation> priorityF3 = new ArrayList<>();
        for (Reservation r : result2.remainingReservations) {
            r.setPriorityOrder(r.getPriorityOrder() + 1);
            if (r.getFirstWindowTime() == null) {
                r.setFirstWindowTime(Timestamp.valueOf("2026-04-01 09:00:00"));
            }
            priorityF3.add(r);
        }
        
        // Nouvelles réservations fenêtre 3
        Reservation r5 = createReservation(5, "F3-A", 2, 0);
        
        GroupingService.AllocationResult result3 = groupingService.allocateForGroupSprint8(
            testDate,
            Time.valueOf("10:00:00"),
            priorityF3,
            List.of(r5),
            v1
        );
        
        // Vérifier chaînage complet
        assertNotNull(result3, "Fenêtre 3 doit produire un résultat");
        
        // Au moins un des prioritaires F3 doit être assigné en premier
        if (!result3.assignments.isEmpty()) {
            ReservationVehicule firstF3 = result3.assignments.get(0);
            if (!priorityF3.isEmpty()) {
                // Vérifier que les prioritaires passent en premier
                boolean priorityFirst = false;
                for (Reservation prio : priorityF3) {
                    if (firstF3.getIdReservation() == prio.getId()) {
                        priorityFirst = true;
                        break;
                    }
                }
                assertTrue(priorityFirst || priorityF3.isEmpty(), "Fenêtre 3: prioritaires d'abord");
            }
        }
    }

    // ===============================
    // Test 5 : Ordre FIFO des prioritaires
    // ===============================
    @Test
    @Order(5)
    void testFIFOOrderForPriority() {
        // GIVEN: 3 prioritaires avec des first_window_time différents
        Vehicule v1 = createVehicule(1, "Bus", 10, 30);
        
        // Prioritaire 1 - le plus ancien
        Reservation r1 = createReservation(1, "Oldest", 3, 0);
        r1.setPriorityOrder(1);
        r1.setFirstWindowTime(Timestamp.valueOf("2026-04-01 07:00:00")); // Plus ancien
        
        // Prioritaire 2 - intermédiaire
        Reservation r2 = createReservation(2, "Middle", 3, 0);
        r2.setPriorityOrder(1);
        r2.setFirstWindowTime(Timestamp.valueOf("2026-04-01 08:00:00"));
        
        // Prioritaire 3 - le plus récent
        Reservation r3 = createReservation(3, "Newest", 3, 0);
        r3.setPriorityOrder(1);
        r3.setFirstWindowTime(Timestamp.valueOf("2026-04-01 09:00:00"));
        
        // Donner dans le désordre
        List<Reservation> unassigned = List.of(r3, r1, r2);
        
        // WHEN: Allocation
        GroupingService.AllocationResult result = groupingService.allocateForGroupSprint8(
            testDate,
            Time.valueOf("10:00:00"),
            unassigned,
            new ArrayList<>(),
            v1
        );
        
        // THEN: Ordre FIFO respecté (r1 → r2 → r3)
        assertEquals(3, result.assignments.size(), "3 assignations");
        assertEquals(1, result.assignments.get(0).getIdReservation(), "Oldest (r1) en premier");
        assertEquals(2, result.assignments.get(1).getIdReservation(), "Middle (r2) en second");
        assertEquals(3, result.assignments.get(2).getIdReservation(), "Newest (r3) en dernier");
    }

    // ===============================
    // Test 6 : DepartureResult complet
    // ===============================
    @Test
    @Order(6)
    void testDepartureResultHandling() {
        // GIVEN: Configuration standard
        Vehicule v1 = createVehicule(1, "Test Bus", 8, 25);
        
        Reservation r1 = createReservation(1, "Client X", 4, 0);
        Reservation r2 = createReservation(2, "Client Y", 3, 0);
        
        List<Reservation> unassigned = List.of(r1);
        List<Reservation> newRes = List.of(r2);
        
        // WHEN: Allocation et gestion du départ
        GroupingService.AllocationResult result = groupingService.allocateForGroupSprint8(
            testDate,
            Time.valueOf("11:00:00"),
            unassigned,
            newRes,
            v1
        );
        result.windowEnd = Time.valueOf("11:25:00");
        
        // Vérifier DepartureDecision
        GroupingService.DepartureDecision decision = groupingService.checkAndTriggerDeparture(v1, result);
        
        assertNotNull(decision, "Decision ne doit pas être null");
        assertEquals(1, decision.vehiculeId);
        assertEquals("Test Bus", decision.vehiculeName);
        assertEquals(7, decision.totalPassengers, "7 passagers au total");
        assertEquals(8, decision.capacity);
        assertEquals(1, decision.getRemainingCapacity(), "1 place restante");
        assertFalse(decision.isFull(), "Pas plein");
    }

    // ===============================
    // Test 7 : Fragmentation des passagers
    // ===============================
    @Test
    @Order(7)
    void testPassengerFragmentation() {
        // GIVEN: Réservation de 8 passagers, véhicule 5 places
        Vehicule v1 = createVehicule(1, "Petit Van", 5, 20);
        
        Reservation r1 = createReservation(1, "Big Group", 8, 0);
        
        // WHEN: Allocation - seulement 5 peuvent monter
        GroupingService.AllocationResult result = groupingService.allocateForGroupSprint8(
            testDate,
            Time.valueOf("12:00:00"),
            new ArrayList<>(),
            List.of(r1),
            v1
        );
        
        // THEN: 5 assignés, 3 restent
        assertEquals(1, result.assignments.size(), "1 assignation");
        assertEquals(5, result.assignments.get(0).getPassengersAssigned(), "5 passagers assignés");
        
        assertEquals(1, result.remainingReservations.size(), "Réservation reste avec passagers");
        assertEquals(3, result.remainingReservations.get(0).getRemaining(), "3 passagers restants");
    }

    // ===============================
    // Méthodes utilitaires
    // ===============================
    
    private Vehicule createVehicule(int id, String nom, int capacite, int tempsAttente) {
        Vehicule v = new Vehicule();
        v.setId(id);
        v.setNom(nom);
        v.setCapacite(capacite);
        v.setTempsAttente(tempsAttente);
        v.setVitesseMoyenne(new java.math.BigDecimal("50.0"));
        return v;
    }
    
    private Reservation createReservation(int id, String nomClient, int nombrePassagers, int assignedCount) {
        Reservation r = new Reservation();
        r.setId(id);
        r.setNomClient(nomClient);
        r.setNombrePassager(nombrePassagers);
        r.setAssignedCount(assignedCount);
        r.setDateArrivee(testDate);
        r.setHeureArrivee(Time.valueOf("08:00:00"));
        r.setStatus("EN_ATTENTE");
        return r;
    }
}
