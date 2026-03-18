package service;

import dao.ReservationDAO;
import dao.VehiculeDAO;
import dao.ReservationVehiculeDAO;
import models.Reservation;
import models.Vehicule;
import models.ReservationVehicule;

import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

/**
 * Service responsible for batch assignment of reservations to vehicles for a given date+time.
 */
public class AssignationService {

    private ReservationDAO reservationDAO = new ReservationDAO();
    private VehiculeDAO vehiculeDAO = new VehiculeDAO();
    private ReservationVehiculeDAO reservationVehiculeDAO = new ReservationVehiculeDAO();
    private TracabiliteService tracabiliteService = new TracabiliteService();

    public Map<String, Object> assignerCreneau(Date date, Time heure) {
        Map<String, Object> result = new HashMap<>();
        int assigned = 0;
        int notAssigned = 0;

        try {
            // Note: Each DAO creates its own connection with auto-commit,
            // so no explicit transaction management needed.

            List<Reservation> reservations = reservationDAO.findByDateAndTimeAndStatus(date, heure, "EN_ATTENTE");
            if (reservations == null || reservations.isEmpty()) {
                result.put("assigned", assigned);
                result.put("notAssigned", notAssigned);
                return result;
            }

            // Sort largest-first
            reservations.sort((a,b) -> Integer.compare(b.getNombrePersonnes(), a.getNombrePersonnes()));

            // Load vehicles
            List<Vehicule> vehicules = vehiculeDAO.findAll();

            // Compute initial free capacities per vehicle for this date+time
            Map<Integer, Integer> freeCap = new HashMap<>();
            Map<Integer, Vehicule> vehiculeById = new HashMap<>();
            java.sql.Timestamp reservationTs = null;
            if (date != null && heure != null) {
                java.time.LocalDate ld = date.toLocalDate();
                java.time.LocalTime lt = heure.toLocalTime();
                reservationTs = java.sql.Timestamp.valueOf(java.time.LocalDateTime.of(ld, lt));
            }

            Map<Integer,Integer> occupiedMap = new HashMap<>();
            for (Vehicule v : vehicules) {
                vehiculeById.put(v.getId(), v);
                // check available_from
                java.sql.Timestamp av = v.getAvailableFrom();
                boolean candidateAvailable = true;
                if (av != null && av.after(reservationTs)) {
                    candidateAvailable = false;
                }
                if (!candidateAvailable) {
                    freeCap.put(v.getId(), -1); // mark unavailable
                    occupiedMap.put(v.getId(), 0);
                    continue;
                }
                int occupied = reservationVehiculeDAO.getOccupiedCapacityForDateTime(v.getId(), date, heure);
                occupiedMap.put(v.getId(), occupied);
                int free = v.getCapacite() - occupied;
                freeCap.put(v.getId(), free);
            }

            Random rand = new Random();

            for (Reservation r : reservations) {
                int personnes = r.getNombrePersonnes();

                System.out.println("=== Processing " + r.getRefClient() + " (" + personnes + " personnes) ===");
                System.out.println("Current occupiedMap: " + occupiedMap);
                System.out.println("Current freeCap: " + freeCap);

                // Build candidate lists: prefer vehicles that already have assignments (to pack)
                List<Vehicule> candidatsUsed = new ArrayList<>();
                List<Vehicule> candidatsEmpty = new ArrayList<>();
                for (Vehicule v : vehicules) {
                    int free = freeCap.getOrDefault(v.getId(), -1);
                    if (free >= personnes) {
                        int occ = occupiedMap.getOrDefault(v.getId(), 0);
                        if (occ > 0) candidatsUsed.add(v);
                        else candidatsEmpty.add(v);
                    }
                }

                System.out.println("CandidatsUsed: " + candidatsUsed.stream().map(v -> v.getId() + "(" + v.getMarque() + ")").toList());
                System.out.println("CandidatsEmpty: " + candidatsEmpty.stream().map(v -> v.getId() + "(" + v.getMarque() + ")").toList());

                List<Vehicule> candidats = !candidatsUsed.isEmpty() ? candidatsUsed : new ArrayList<>(candidatsEmpty);
                System.out.println("Final candidats: " + candidats.stream().map(v -> v.getId() + "(" + v.getMarque() + ")").toList());

                if (candidats.isEmpty()) {
                    // no vehicle fits
                    reservationDAO.updateStatus(r.getId(), "NON_ASSIGNE");
                    notAssigned++;
                    continue;
                }

                // Find minimal gap
                int ecartMin = Integer.MAX_VALUE;
                for (Vehicule v : candidats) {
                    int free = freeCap.get(v.getId());
                    int ecart = free - personnes;
                    if (ecart < ecartMin) ecartMin = ecart;
                }

                List<Vehicule> bestByCap = new ArrayList<>();
                for (Vehicule v : candidats) {
                    if (freeCap.get(v.getId()) - personnes == ecartMin) bestByCap.add(v);
                }

                // Carburant priority
                int bestPriority = Integer.MAX_VALUE;
                Map<String, Integer> PRIORITE_CARBURANT = new HashMap<>();
                PRIORITE_CARBURANT.put("Diesel", 1);
                PRIORITE_CARBURANT.put("Essence", 2);
                PRIORITE_CARBURANT.put("Hybride", 3);
                PRIORITE_CARBURANT.put("Électrique", 4);
                PRIORITE_CARBURANT.put("Electrique", 4);

                for (Vehicule v : bestByCap) {
                    int prio = PRIORITE_CARBURANT.getOrDefault(v.getTypeCarburant(), 99);
                    if (prio < bestPriority) bestPriority = prio;
                }

                List<Vehicule> bestByCarb = new ArrayList<>();
                for (Vehicule v : bestByCap) {
                    if (PRIORITE_CARBURANT.getOrDefault(v.getTypeCarburant(), 99) == bestPriority) bestByCarb.add(v);
                }

                Vehicule chosen;
                if (bestByCarb.size() == 1) chosen = bestByCarb.get(0);
                else chosen = bestByCarb.get(rand.nextInt(bestByCarb.size()));

                // persist assignment
                ReservationVehicule rv = new ReservationVehicule(r.getId(), chosen.getId());
                reservationVehiculeDAO.save(rv);
                reservationDAO.updateStatus(r.getId(), "ASSIGNE");
                assigned++;

                // update free capacity in memory
                int newFree = freeCap.get(chosen.getId()) - personnes;
                freeCap.put(chosen.getId(), newFree);
                // update occupied map so subsequent iterations consider this vehicle as used
                occupiedMap.put(chosen.getId(), occupiedMap.getOrDefault(chosen.getId(), 0) + personnes);
                
                System.out.println("Chosen vehicle: " + chosen.getId() + "(" + chosen.getMarque() + "), updated occupiedMap: " + occupiedMap.get(chosen.getId()));
                System.out.println();
            }

            // After assigning, update available_from for vehicles that may have departed
            for (Map.Entry<Integer, Integer> e : freeCap.entrySet()) {
                int vid = e.getKey();
                int free = e.getValue();
                Vehicule v = vehiculeById.get(vid);
                if (v == null) continue;
                // If vehicle is full (free <= 0) or if it had assignments and should depart, compute heureRetour
                try {
                    List<Reservation> resasVehicule = reservationVehiculeDAO.findReservationsByVehiculeAndDate(vid, date);
                    if (resasVehicule != null && !resasVehicule.isEmpty()) {
                        java.sql.Time heureRetour = tracabiliteService.calculerHeureRetour(v, resasVehicule);
                        java.sql.Timestamp ts = null;
                        if (heureRetour != null) {
                            java.time.LocalDate dateLocal = date.toLocalDate();
                            java.time.LocalTime timeLocal = heureRetour.toLocalTime();
                            java.time.LocalDateTime ldt = java.time.LocalDateTime.of(dateLocal, timeLocal);
                            ts = java.sql.Timestamp.valueOf(ldt);
                        } else {
                            // Fallback: use latest reservation heure_arrivee + 2 hours
                            java.time.LocalTime latest = resasVehicule.stream()
                                    .map(Reservation::getHeureArrivee)
                                    .filter(Objects::nonNull)
                                    .map(java.sql.Time::toLocalTime)
                                    .max(Comparator.naturalOrder())
                                    .orElse(java.time.LocalTime.MIDNIGHT);
                            java.time.LocalDate dateLocal = date.toLocalDate();
                            java.time.LocalDateTime ldt = java.time.LocalDateTime.of(dateLocal, latest).plusHours(2);
                            ts = java.sql.Timestamp.valueOf(ldt);
                        }
                        if (ts != null) {
                            vehiculeDAO.updateAvailableFrom(vid, ts);
                        }
                    }
                } catch (Exception ex) {
                    // non blocking
                    System.err.println("Warning updating available_from for vehicule " + vid + ": " + ex.getMessage());
                }
            }

            // Populate results
            result.put("assigned", assigned);
            result.put("notAssigned", notAssigned);

            return result;

        } catch (SQLException e) {
            // If something went wrong, return partial results with error
            result.put("assigned", assigned);
            result.put("notAssigned", notAssigned);
            result.put("error", e.getMessage());
            return result;
        }
    }
}
