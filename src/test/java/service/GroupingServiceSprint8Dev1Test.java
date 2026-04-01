package service;

import models.Reservation;
import models.ReservationVehicule;
import models.Vehicule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour Sprint 8 : Priorisation des réservations non assignées.
 * 
 * Tests couverts :
 * - Priorité absolue des non assignés
 * - Véhicule plein avec non assignés uniquement
 * - Véhicule partiellement rempli
 * - Chaîne de fenêtres (report des non assignés)
 * - Méthode traiterRetourVehicule
 */
public class GroupingServiceSprint8Dev1Test {

    private GroupingService groupingService;

    // Mock DAO pour éviter les appels DB
    static class TestReservationVehiculeDAO extends dao.ReservationVehiculeDAO {
        @Override
        public int sumAssignedByVehicule(int idVehicule) {
            return 0; // Véhicule vide au départ
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        groupingService = new GroupingService();
        // Injecter le mock DAO
        Field f = GroupingService.class.getDeclaredField("reservationVehiculeDAO");
        f.setAccessible(true);
        f.set(groupingService, new TestReservationVehiculeDAO());
    }

    // ===============================
    // Helpers pour créer des objets de test
    // ===============================

    private Reservation makeReservation(int id, int nombrePersonnes) {
        Reservation r = new Reservation();
        r.setId(id);
        r.setNombrePersonnes(nombrePersonnes);
        r.setAssignedCount(0);
        r.setDateArrivee(Date.valueOf(LocalDate.now()));
        r.setHeureArrivee(Time.valueOf("10:00:00"));
        r.setRefClient("Client" + id);
        r.setHotelId(1);
        r.setStatus("EN_ATTENTE");
        r.setPriorityOrder(0);
        return r;
    }

    private Reservation makeUnassignedReservation(int id, int nombrePersonnes, int alreadyAssigned, int priority) {
        Reservation r = makeReservation(id, nombrePersonnes);
        r.setAssignedCount(alreadyAssigned);
        r.setPriorityOrder(priority);
        r.setFirstWindowTime(Timestamp.valueOf(LocalDateTime.now().minusMinutes(30)));
        return r;
    }

    private Vehicule makeVehicule(int id, int capacite) {
        Vehicule v = new Vehicule();
        v.setId(id);
        v.setCapacite(capacite);
        v.setMarque("Vehicule" + id);
        v.setTypeCarburant("Diesel");
        v.setTempsAttente(30); // 30 minutes d'attente
        return v;
    }

    // ===============================
    // TEST 1 : Priorité absolue des non assignés
    // ===============================

    @Test
    @DisplayName("Sprint 8 - Les non assignés ont priorité absolue sur les nouvelles réservations")
    void testUnassignedHavePriority() throws Exception {
        // ARRANGE
        // 2 non assignés (prioritaires)
        Reservation unassigned1 = makeUnassignedReservation(1, 3, 0, 1); // 3 passagers, priorité 1
        Reservation unassigned2 = makeUnassignedReservation(2, 2, 0, 1); // 2 passagers, priorité 1
        List<Reservation> unassignedPriority = new ArrayList<>();
        unassignedPriority.add(unassigned1);
        unassignedPriority.add(unassigned2);

        // 5 nouvelles réservations (non prioritaires)
        Reservation new1 = makeReservation(10, 4); // 4 passagers
        Reservation new2 = makeReservation(11, 3); // 3 passagers
        Reservation new3 = makeReservation(12, 2); // 2 passagers
        Reservation new4 = makeReservation(13, 2); // 2 passagers
        Reservation new5 = makeReservation(14, 1); // 1 passager
        List<Reservation> newReservations = new ArrayList<>();
        newReservations.add(new1);
        newReservations.add(new2);
        newReservations.add(new3);
        newReservations.add(new4);
        newReservations.add(new5);

        // Véhicule avec 10 places
        Vehicule v = makeVehicule(100, 10);
        List<Vehicule> vehicules = List.of(v);

        // ACT
        Date date = Date.valueOf(LocalDate.now());
        Time windowStart = Time.valueOf("09:45:00");
        GroupingService.AllocationResult result = groupingService.allocateForGroupSprint8(
            date, windowStart, unassignedPriority, newReservations, vehicules
        );

        // ASSERT
        // Les non assignés doivent être placés EN PREMIER
        // Total non assignés = 3 + 2 = 5 passagers
        // Reste 5 places pour nouvelles réservations
        
        int assignedUnassigned1 = 0;
        int assignedUnassigned2 = 0;
        int assignedNew = 0;
        
        for (ReservationVehicule rv : result.assignments) {
            if (rv.getIdReservation() == 1) assignedUnassigned1 += rv.getPassengersAssigned();
            if (rv.getIdReservation() == 2) assignedUnassigned2 += rv.getPassengersAssigned();
            if (rv.getIdReservation() >= 10) assignedNew += rv.getPassengersAssigned();
        }

        // Vérifier que tous les non assignés sont placés
        assertEquals(3, assignedUnassigned1, "Non assigné 1 (3 passagers) doit être entièrement placé");
        assertEquals(2, assignedUnassigned2, "Non assigné 2 (2 passagers) doit être entièrement placé");
        
        // Vérifier que 5 places sont utilisées pour les nouvelles réservations
        assertEquals(5, assignedNew, "5 places restantes pour nouvelles réservations");
        
        // Vérifier que le véhicule est plein (10 passagers)
        assertEquals(0, result.finalVehicleRemaining.get(100).intValue(), "Véhicule doit être plein");
    }

    // ===============================
    // TEST 2 : Véhicule plein avec non assignés uniquement
    // ===============================

    @Test
    @DisplayName("Sprint 8 - Véhicule plein avec seulement des non assignés → départ immédiat")
    void testVehicleFullWithUnassignedOnly() throws Exception {
        // ARRANGE
        // 10 non assignés pour un véhicule de 10 places
        List<Reservation> unassignedPriority = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            unassignedPriority.add(makeUnassignedReservation(i, 2, 0, 1)); // 5 x 2 = 10 passagers
        }

        // Pas de nouvelles réservations
        List<Reservation> newReservations = new ArrayList<>();

        // Véhicule avec 10 places
        Vehicule v = makeVehicule(100, 10);
        List<Vehicule> vehicules = List.of(v);

        // ACT
        Date date = Date.valueOf(LocalDate.now());
        Time windowStart = Time.valueOf("09:00:00");
        GroupingService.AllocationResult result = groupingService.allocateForGroupSprint8(
            date, windowStart, unassignedPriority, newReservations, vehicules
        );

        // ASSERT
        // Tous les non assignés doivent être placés
        int totalAssigned = 0;
        for (ReservationVehicule rv : result.assignments) {
            totalAssigned += rv.getPassengersAssigned();
        }
        assertEquals(10, totalAssigned, "10 passagers non assignés doivent être placés");
        
        // Véhicule plein
        assertEquals(0, result.finalVehicleRemaining.get(100).intValue(), "Véhicule doit être plein");
        
        // Pas de restants
        assertTrue(result.remainingReservations.isEmpty(), "Aucun passager restant");
        
        // Vérifier que isVehicleFull retourne true
        assertTrue(groupingService.isVehicleFull(v, result), "Véhicule doit être considéré comme plein");
    }

    // ===============================
    // TEST 3 : Véhicule partiellement rempli
    // ===============================

    @Test
    @DisplayName("Sprint 8 - Véhicule partiellement rempli → attendre fin de fenêtre")
    void testVehiclePartiallyFilled() throws Exception {
        // ARRANGE
        // 5 non assignés pour un véhicule de 15 places
        Reservation unassigned1 = makeUnassignedReservation(1, 3, 0, 1);
        Reservation unassigned2 = makeUnassignedReservation(2, 2, 0, 1);
        List<Reservation> unassignedPriority = new ArrayList<>();
        unassignedPriority.add(unassigned1);
        unassignedPriority.add(unassigned2);

        // Pas de nouvelles réservations dans la fenêtre
        List<Reservation> newReservations = new ArrayList<>();

        // Véhicule avec 15 places
        Vehicule v = makeVehicule(100, 15);
        List<Vehicule> vehicules = List.of(v);

        // ACT
        Date date = Date.valueOf(LocalDate.now());
        Time windowStart = Time.valueOf("09:00:00");
        GroupingService.AllocationResult result = groupingService.allocateForGroupSprint8(
            date, windowStart, unassignedPriority, newReservations, vehicules
        );

        // ASSERT
        // 5 passagers placés, reste 10 places
        int totalAssigned = 0;
        for (ReservationVehicule rv : result.assignments) {
            totalAssigned += rv.getPassengersAssigned();
        }
        assertEquals(5, totalAssigned, "5 passagers non assignés doivent être placés");
        
        // 10 places restantes
        assertEquals(10, result.finalVehicleRemaining.get(100).intValue(), "10 places restantes");
        
        // Vérifier que isVehicleFull retourne false
        assertFalse(groupingService.isVehicleFull(v, result), "Véhicule ne doit pas être considéré comme plein");
    }

    // ===============================
    // TEST 4 : Chaîne de fenêtres (report des non assignés)
    // ===============================

    @Test
    @DisplayName("Sprint 8 - Chaîne de fenêtres avec report des non assignés")
    void testChainedWindows() throws Exception {
        // ARRANGE - Fenêtre 1
        // Réservations initiales : R1=6, R2=4, R3=3 = 13 passagers
        // Véhicules : V1=8, V2=3 = 11 places
        // Résultat attendu : 2 passagers restants
        
        Reservation r1 = makeReservation(1, 6);
        Reservation r2 = makeReservation(2, 4);
        Reservation r3 = makeReservation(3, 3);
        List<Reservation> reservationsFenetre1 = new ArrayList<>();
        reservationsFenetre1.add(r1);
        reservationsFenetre1.add(r2);
        reservationsFenetre1.add(r3);

        Vehicule v1 = makeVehicule(101, 8);
        Vehicule v2 = makeVehicule(102, 3);
        List<Vehicule> vehiculesFenetre1 = new ArrayList<>();
        vehiculesFenetre1.add(v1);
        vehiculesFenetre1.add(v2);

        // ACT - Fenêtre 1 (utiliser allocateForGroup standard)
        Date date = Date.valueOf(LocalDate.now());
        Time windowStart1 = Time.valueOf("08:00:00");
        GroupingService.AllocationResult resultFenetre1 = groupingService.allocateForGroup(
            date, windowStart1, reservationsFenetre1, vehiculesFenetre1
        );

        // ASSERT Fenêtre 1 - Doit avoir des restants
        assertFalse(resultFenetre1.remainingReservations.isEmpty(), "Fenêtre 1 doit avoir des passagers restants");
        
        int totalRestantsFenetre1 = 0;
        for (Reservation r : resultFenetre1.remainingReservations) {
            totalRestantsFenetre1 += r.getRemaining();
        }
        assertEquals(2, totalRestantsFenetre1, "2 passagers doivent rester après fenêtre 1");

        // ARRANGE - Fenêtre 2
        // Les restants de fenêtre 1 deviennent prioritaires
        List<Reservation> unassignedFenetre2 = new ArrayList<>();
        for (Reservation r : resultFenetre1.remainingReservations) {
            r.setPriorityOrder(1); // Marquer comme prioritaire
            r.setFirstWindowTime(Timestamp.valueOf(LocalDateTime.now().minusMinutes(30)));
            unassignedFenetre2.add(r);
        }

        // Nouvelles réservations fenêtre 2 : R4=7, R5=5, R6=3 = 15 passagers
        Reservation r4 = makeReservation(4, 7);
        Reservation r5 = makeReservation(5, 5);
        Reservation r6 = makeReservation(6, 3);
        List<Reservation> newReservationsFenetre2 = new ArrayList<>();
        newReservationsFenetre2.add(r4);
        newReservationsFenetre2.add(r5);
        newReservationsFenetre2.add(r6);

        // V1 revient avec 15 places
        Vehicule v1Fenetre2 = makeVehicule(101, 15);
        List<Vehicule> vehiculesFenetre2 = List.of(v1Fenetre2);

        // ACT - Fenêtre 2 (utiliser allocateForGroupSprint8)
        Time windowStart2 = Time.valueOf("09:45:00");
        GroupingService.AllocationResult resultFenetre2 = groupingService.allocateForGroupSprint8(
            date, windowStart2, unassignedFenetre2, newReservationsFenetre2, vehiculesFenetre2
        );

        // ASSERT Fenêtre 2
        // Total disponible : 15 places
        // Prioritaires : 2 passagers (restants fenêtre 1)
        // Nouvelles : 7+5+3 = 15 passagers
        // Total demandé : 17 passagers
        // Placés : 15 (véhicule plein)
        // Restants : 2 passagers

        int totalAssignedFenetre2 = 0;
        for (ReservationVehicule rv : resultFenetre2.assignments) {
            totalAssignedFenetre2 += rv.getPassengersAssigned();
        }
        assertEquals(15, totalAssignedFenetre2, "15 passagers doivent être placés en fenêtre 2");
        
        // Véhicule plein
        assertEquals(0, resultFenetre2.finalVehicleRemaining.get(101).intValue(), "V1 doit être plein");

        // Vérifier que les prioritaires ont été placés en premier
        // (ils doivent avoir été entièrement assignés car seulement 2 passagers)
        int prioritairesPlaces = 0;
        for (ReservationVehicule rv : resultFenetre2.assignments) {
            // Les IDs 1, 2, 3 sont les réservations de fenêtre 1
            if (rv.getIdReservation() <= 3) {
                prioritairesPlaces += rv.getPassengersAssigned();
            }
        }
        assertEquals(2, prioritairesPlaces, "Les 2 passagers prioritaires doivent être placés");
    }

    // ===============================
    // TEST 5 : Exemple complet du Sprint 8 (Cas 3 du sprint8.md)
    // ===============================

    @Test
    @DisplayName("Sprint 8 - Exemple complet : Fenêtre 2 avec non assignés de Fenêtre 1")
    void testSprint8CompleteExample() throws Exception {
        // Contexte du sprint8.md - Cas 3
        // Fenêtre 1 : 2 non assignés (1 de R2, 1 de R3)
        // Fenêtre 2 : V1 revient à 09:45 avec 15 places
        // Nouvelles : R4=7, R5=5, R6=3

        // ARRANGE
        // 2 non assignés de fenêtre 1
        Reservation unassignedR2 = makeUnassignedReservation(2, 4, 3, 1); // R2: 4 passagers, 3 déjà assignés, 1 restant
        Reservation unassignedR3 = makeUnassignedReservation(3, 3, 2, 1); // R3: 3 passagers, 2 déjà assignés, 1 restant
        List<Reservation> unassigned = new ArrayList<>();
        unassigned.add(unassignedR2);
        unassigned.add(unassignedR3);

        // Nouvelles réservations
        Reservation r4 = makeReservation(4, 7);
        r4.setHeureArrivee(Time.valueOf("10:00:00"));
        Reservation r5 = makeReservation(5, 5);
        r5.setHeureArrivee(Time.valueOf("10:15:00"));
        Reservation r6 = makeReservation(6, 3);
        r6.setHeureArrivee(Time.valueOf("10:12:00"));
        List<Reservation> newReservations = new ArrayList<>();
        newReservations.add(r4);
        newReservations.add(r5);
        newReservations.add(r6);

        // V1 : 15 places
        Vehicule v1 = makeVehicule(101, 15);
        List<Vehicule> vehicules = List.of(v1);

        // ACT
        Date date = Date.valueOf(LocalDate.now());
        Time windowStart = Time.valueOf("09:45:00");
        GroupingService.AllocationResult result = groupingService.allocateForGroupSprint8(
            date, windowStart, unassigned, newReservations, vehicules
        );

        // ASSERT
        // Étape 1 : 2 non assignés montent → reste 13 places
        // Étape 2 : R4(7) → reste 6 places
        // Étape 3 : R5(5) → reste 1 place
        // Étape 4 : R6(3) → seulement 1 monte, 2 restent
        // Total : 15 passagers, V1 plein

        // Vérifier le total
        int totalAssigned = 0;
        Map<Integer, Integer> assignedPerReservation = new HashMap<>();
        for (ReservationVehicule rv : result.assignments) {
            totalAssigned += rv.getPassengersAssigned();
            assignedPerReservation.merge(rv.getIdReservation(), rv.getPassengersAssigned(), Integer::sum);
        }

        assertEquals(15, totalAssigned, "15 passagers doivent être placés (V1 plein)");
        
        // Vérifier les assignations individuelles
        assertEquals(1, assignedPerReservation.getOrDefault(2, 0).intValue(), "R2: 1 passager (non assigné fenêtre 1)");
        assertEquals(1, assignedPerReservation.getOrDefault(3, 0).intValue(), "R3: 1 passager (non assigné fenêtre 1)");
        assertEquals(7, assignedPerReservation.getOrDefault(4, 0).intValue(), "R4: 7 passagers complets");
        assertEquals(5, assignedPerReservation.getOrDefault(5, 0).intValue(), "R5: 5 passagers complets");
        assertEquals(1, assignedPerReservation.getOrDefault(6, 0).intValue(), "R6: 1 passager (partiel)");

        // Vérifier les restants (2 passagers de R6)
        int totalRemaining = 0;
        for (Reservation r : result.remainingReservations) {
            totalRemaining += r.getRemaining();
        }
        assertEquals(2, totalRemaining, "2 passagers de R6 doivent rester pour fenêtre 3");

        // Vérifier que V1 est plein
        assertEquals(0, result.finalVehicleRemaining.get(101).intValue(), "V1 doit être plein");
    }

    // ===============================
    // TEST 6 : Ordre FIFO des non assignés
    // ===============================

    @Test
    @DisplayName("Sprint 8 - Les non assignés sont traités en ordre FIFO (ancienneté)")
    void testUnassignedFIFOOrder() throws Exception {
        // ARRANGE
        // 3 non assignés avec des timestamps différents
        Reservation old1 = makeUnassignedReservation(1, 3, 0, 1);
        old1.setFirstWindowTime(Timestamp.valueOf(LocalDateTime.now().minusHours(2))); // Le plus ancien
        
        Reservation old2 = makeUnassignedReservation(2, 2, 0, 1);
        old2.setFirstWindowTime(Timestamp.valueOf(LocalDateTime.now().minusHours(1)));
        
        Reservation old3 = makeUnassignedReservation(3, 4, 0, 1);
        old3.setFirstWindowTime(Timestamp.valueOf(LocalDateTime.now().minusMinutes(30))); // Le plus récent

        List<Reservation> unassigned = new ArrayList<>();
        // Ajouter dans le désordre
        unassigned.add(old3);
        unassigned.add(old1);
        unassigned.add(old2);

        // Véhicule avec seulement 5 places (ne peut pas prendre tout le monde)
        Vehicule v = makeVehicule(100, 5);
        List<Vehicule> vehicules = List.of(v);

        // ACT
        Date date = Date.valueOf(LocalDate.now());
        Time windowStart = Time.valueOf("10:00:00");
        GroupingService.AllocationResult result = groupingService.allocateForGroupSprint8(
            date, windowStart, unassigned, new ArrayList<>(), vehicules
        );

        // ASSERT
        // Les plus anciens doivent être placés en premier
        // old1 (3 passagers) + old2 (2 passagers) = 5 places = véhicule plein
        // old3 (4 passagers) doit rester
        
        Map<Integer, Integer> assigned = new HashMap<>();
        for (ReservationVehicule rv : result.assignments) {
            assigned.merge(rv.getIdReservation(), rv.getPassengersAssigned(), Integer::sum);
        }

        assertEquals(3, assigned.getOrDefault(1, 0).intValue(), "old1 (le plus ancien) doit être entièrement placé");
        assertEquals(2, assigned.getOrDefault(2, 0).intValue(), "old2 doit être entièrement placé");
        
        // old3 ne doit pas être placé (plus récent)
        assertEquals(0, assigned.getOrDefault(3, 0).intValue(), "old3 (le plus récent) ne doit pas être placé");
        
        // Vérifier les restants
        assertEquals(1, result.remainingReservations.size(), "1 réservation doit rester");
        assertEquals(3, result.remainingReservations.get(0).getId(), "old3 doit être dans les restants");
    }

    // ===============================
    // TEST 7 : Surcharge avec séparation automatique
    // ===============================

    @Test
    @DisplayName("Sprint 8 - Surcharge allocateForGroupSprint8 sépare automatiquement prioritaires et nouvelles")
    void testAllocateForGroupSprint8AutoSeparation() throws Exception {
        // ARRANGE
        // Mix de réservations prioritaires et non prioritaires
        Reservation priority1 = makeUnassignedReservation(1, 2, 0, 1); // Prioritaire
        Reservation priority2 = makeUnassignedReservation(2, 3, 0, 2); // Prioritaire
        Reservation normal1 = makeReservation(10, 4); // Normal
        Reservation normal2 = makeReservation(11, 3); // Normal

        List<Reservation> allReservations = new ArrayList<>();
        allReservations.add(normal1);
        allReservations.add(priority1);
        allReservations.add(normal2);
        allReservations.add(priority2);

        Vehicule v = makeVehicule(100, 10);
        List<Vehicule> vehicules = List.of(v);

        // ACT - Utiliser la surcharge qui sépare automatiquement
        Date date = Date.valueOf(LocalDate.now());
        Time windowStart = Time.valueOf("10:00:00");
        GroupingService.AllocationResult result = groupingService.allocateForGroupSprint8(
            date, windowStart, allReservations, vehicules
        );

        // ASSERT
        // Les prioritaires (5 passagers) doivent être placés en premier
        // Puis les normaux (7 passagers) mais seulement 5 places restantes
        
        Map<Integer, Integer> assigned = new HashMap<>();
        for (ReservationVehicule rv : result.assignments) {
            assigned.merge(rv.getIdReservation(), rv.getPassengersAssigned(), Integer::sum);
        }

        // Prioritaires entièrement placés
        assertEquals(2, assigned.getOrDefault(1, 0).intValue(), "Prioritaire 1 entièrement placé");
        assertEquals(3, assigned.getOrDefault(2, 0).intValue(), "Prioritaire 2 entièrement placé");

        // Normaux : 5 places restantes pour 7 passagers
        int normalAssigned = assigned.getOrDefault(10, 0) + assigned.getOrDefault(11, 0);
        assertEquals(5, normalAssigned, "5 passagers normaux placés");
    }
}
