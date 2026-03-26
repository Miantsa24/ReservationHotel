package service;

import models.Reservation;
import models.ReservationVehicule;
import models.Vehicule;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GroupingServiceSprint7Dev1Test {

    // Test DAO to avoid DB access: sumAssignedByVehicule returns 0
    static class TestReservationVehiculeDAO extends dao.ReservationVehiculeDAO {
        @Override
        public int sumAssignedByVehicule(int idVehicule) {
            return 0;
        }
    }

    private Reservation makeReservation(int id, int nombre) {
        Reservation r = new Reservation();
        r.setId(id);
        r.setNombrePersonnes(nombre);
        r.setAssignedCount(0);
        r.setDateArrivee(Date.valueOf(LocalDate.now()));
        r.setHeureArrivee(Time.valueOf("10:00:00"));
        r.setRefClient("R" + id);
        return r;
    }

    private Vehicule makeVehicule(int id, int capacite) {
        Vehicule v = new Vehicule();
        v.setId(id);
        v.setCapacite(capacite);
        v.setMarque("V" + id);
        v.setTypeCarburant("Diesel");
        return v;
    }

    @Test
    public void sprint7_example_allocation_dev1_only() throws Exception {
        // Arrange: example from Sprint 7
        Vehicule v1 = makeVehicule(101, 8); // V1: 8 places
        Vehicule v2 = makeVehicule(102, 3); // V2: 3 places
        List<Vehicule> vehicules = new ArrayList<>(); vehicules.add(v1); vehicules.add(v2);

        Reservation r1 = makeReservation(1, 6); // R1: 6
        Reservation r2 = makeReservation(2, 4); // R2: 4
        Reservation r3 = makeReservation(3, 3); // R3: 3
        List<Reservation> reservations = new ArrayList<>();
        reservations.add(r1); reservations.add(r2); reservations.add(r3);

        GroupingService svc = new GroupingService();
        // inject test DAO to avoid DB calls
        Field f = GroupingService.class.getDeclaredField("reservationVehiculeDAO");
        f.setAccessible(true);
        f.set(svc, new TestReservationVehiculeDAO());

        // Act
        Date date = Date.valueOf(LocalDate.now());
        Time windowStart = Time.valueOf("10:00:00");
        GroupingService.AllocationResult res = svc.allocateForGroup(date, windowStart, reservations, vehicules);

        // Assert: check assignments per vehicle
        // Expected: V1 -> R1:6, R3:2 ; V2 -> R2:3 ; remaining R2:1, R3:1
        // Sum up assigned per (reservation, vehicle)
        int assignedR1V1 = 0;
        int assignedR3V1 = 0;
        int assignedR2V2 = 0;

        for (ReservationVehicule rv : res.assignments) {
            if (rv.getIdVehicule() == 101 && rv.getIdReservation() == 1) assignedR1V1 += rv.getPassengersAssigned();
            if (rv.getIdVehicule() == 101 && rv.getIdReservation() == 3) assignedR3V1 += rv.getPassengersAssigned();
            if (rv.getIdVehicule() == 102 && rv.getIdReservation() == 2) assignedR2V2 += rv.getPassengersAssigned();
        }

        assertEquals(6, assignedR1V1, "R1 should have 6 assigned on V1");
        assertEquals(2, assignedR3V1, "R3 should have 2 assigned on V1");
        assertEquals(3, assignedR2V2, "R2 should have 3 assigned on V2");

        // Check remaining reservations counts
        Map<Integer, Integer> remainingMap = new java.util.HashMap<>();
        for (Reservation rr : res.remainingReservations) remainingMap.put(rr.getId(), rr.getRemaining());

        assertEquals(1, remainingMap.getOrDefault(2, 0).intValue(), "R2 remaining should be 1");
        assertEquals(1, remainingMap.getOrDefault(3, 0).intValue(), "R3 remaining should be 1");
    }
}
