package service;

import dao.ReservationDAO;
import dao.ReservationVehiculeDAO;
import dao.VehiculeDAO;
import dao.VehiculeTrajetDAO;
import models.Reservation;
import models.ReservationVehicule;
import models.Vehicule;
import models.VehiculeTrajet;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service responsable du groupement des réservations selon le temps d'attente
 * et de l'affectation en respectant les règles métier existantes.
 *
 * Règles implémentées :
 * - Fenêtre de groupement ancrée sur le premier vol du groupe : [t0, t0 + maxTempsAttente]
 *   où maxTempsAttente est la valeur maximale de `tempsAttente` parmi tous les véhicules.
 * - Départ du véhicule = heure d'arrivée du dernier vol inclus dans la fenêtre.
 * - Pour l'affectation on réutilise les règles métier existantes de `VehiculeSelectionService`.
 * - Statuts de réservation : EN_ATTENTE -> ASSIGNE ou NON_ASSIGNE selon résultat.
 */
public class GroupingService {

    private ReservationDAO reservationDAO = new ReservationDAO();
    private VehiculeDAO vehiculeDAO = new VehiculeDAO();
    private VehiculeSelectionService selectionService = new VehiculeSelectionService();
    private ReservationVehiculeDAO reservationVehiculeDAO = new ReservationVehiculeDAO();
    private VehiculeTrajetDAO vehiculeTrajetDAO = new VehiculeTrajetDAO();
    private TracabiliteService tracabiliteService = new TracabiliteService();
    private static final java.util.Map<String, Integer> PRIORITE_CARBURANT = new java.util.HashMap<>();
    static {
        PRIORITE_CARBURANT.put("Diesel", 1);
        PRIORITE_CARBURANT.put("Essence", 2);
        PRIORITE_CARBURANT.put("Hybride", 3);
        PRIORITE_CARBURANT.put("Électrique", 4);
        PRIORITE_CARBURANT.put("Electrique", 4);
    }

    private int getPrioriteCarburant(String t) {
        return PRIORITE_CARBURANT.getOrDefault(t, 99);
    }

    private Vehicule selectVehicleForTmp(Reservation tmp, Map<Integer,Integer> inMemoryAssigned) throws SQLException {
        java.sql.Date date = tmp.getDateArrivee();
        Time time = tmp.getHeureArrivee();

        java.util.List<Vehicule> tous = vehiculeDAO.findAll();
        java.util.List<Vehicule> candidats = new ArrayList<>();
        java.sql.Timestamp reservationTs = new java.sql.Timestamp(date.getTime() + time.getTime());

        for (Vehicule v : tous) {
            java.sql.Timestamp av = v.getAvailableFrom();
            if (av != null) {
                if (!av.after(reservationTs)) {
                    candidats.add(v);
                }
                continue;
            }

            java.util.List<Reservation> assigned = reservationVehiculeDAO.findReservationsByVehiculeAndDate(v.getId(), date);
            if (assigned == null || assigned.isEmpty()) {
                candidats.add(v);
                continue;
            }

            java.sql.Time maxTime = null;
            for (Reservation ar : assigned) {
                if (maxTime == null || ar.getHeureArrivee().after(maxTime)) maxTime = ar.getHeureArrivee();
            }
            if (maxTime == null) { candidats.add(v); continue; }

            java.time.LocalDate dateLocal = date.toLocalDate();
            java.time.LocalTime maxLocal = maxTime.toLocalTime();
            java.time.LocalDateTime allowedLocal = java.time.LocalDateTime.of(dateLocal, maxLocal).plusMinutes(v.getTempsAttente());
            java.sql.Timestamp allowedTs = java.sql.Timestamp.valueOf(allowedLocal);

            if (!reservationTs.after(allowedTs)) candidats.add(v);
        }

        if (candidats.isEmpty()) return null;

        java.util.List<Vehicule> eligibles = new ArrayList<>();
        java.util.Map<Integer,Integer> freeCapacity = new java.util.HashMap<>();
        for (Vehicule v : candidats) {
            int occupied = reservationVehiculeDAO.getOccupiedCapacityForDateTime(v.getId(), date, time);
            int inMem = inMemoryAssigned.getOrDefault(v.getId(), 0);
            int free = v.getCapacite() - (occupied + inMem);
            freeCapacity.put(v.getId(), free);
            if (free >= tmp.getNombrePersonnes()) eligibles.add(v);
        }

        if (eligibles.isEmpty()) return null;

        int ecartMin = Integer.MAX_VALUE;
        for (Vehicule v : eligibles) {
            int ecart = freeCapacity.get(v.getId()) - tmp.getNombrePersonnes();
            if (ecart < ecartMin) ecartMin = ecart;
        }

        java.util.List<Vehicule> meilleurCapacite = new ArrayList<>();
        for (Vehicule v : eligibles) {
            if (freeCapacity.get(v.getId()) - tmp.getNombrePersonnes() == ecartMin) meilleurCapacite.add(v);
        }

        if (meilleurCapacite.size() == 1) return meilleurCapacite.get(0);

        int meilleurePriorite = Integer.MAX_VALUE;
        for (Vehicule v : meilleurCapacite) {
            int prio = getPrioriteCarburant(v.getTypeCarburant());
            if (prio < meilleurePriorite) meilleurePriorite = prio;
        }

        java.util.List<Vehicule> meilleurCarburant = new ArrayList<>();
        for (Vehicule v : meilleurCapacite) {
            if (getPrioriteCarburant(v.getTypeCarburant()) == meilleurePriorite) meilleurCarburant.add(v);
        }

        if (meilleurCarburant.size() == 1) return meilleurCarburant.get(0);

        java.util.Random random = new java.util.Random();
        return meilleurCarburant.get(random.nextInt(meilleurCarburant.size()));
    }

    public static class Group {
        public List<Reservation> reservations = new ArrayList<>();
        public Time departureTime; // heure du dernier vol du groupe
    }

    /**
     * Construire les groupes pour une date donnée.
     * Ne prend en compte que les réservations en statut `EN_ATTENTE` pour la date.
     */
    public List<Group> groupReservationsByDate(Date date) throws SQLException {
        List<Reservation> all = reservationDAO.findByDateArrivee(date);
        List<Reservation> pending = new ArrayList<>();
        for (Reservation r : all) {
            if (r.getStatus() == null || "EN_ATTENTE".equals(r.getStatus())) pending.add(r);
        }

        List<Vehicule> vehicules = vehiculeDAO.findAll();
        int maxTempsAttente = 0;
        for (Vehicule v : vehicules) {
            if (v.getTempsAttente() > maxTempsAttente) maxTempsAttente = v.getTempsAttente();
        }

        List<Group> groups = new ArrayList<>();

        int i = 0;
        while (i < pending.size()) {
            Reservation anchor = pending.get(i);
            // window end = anchor.heure_arrivee + maxTempsAttente minutes
            java.time.LocalTime anchorTime = anchor.getHeureArrivee().toLocalTime();
            java.time.LocalTime windowEndLocal = anchorTime.plusMinutes(maxTempsAttente);

            Group g = new Group();
            int j = i;
            java.time.LocalTime lastIncluded = anchorTime;
            while (j < pending.size()) {
                Reservation cand = pending.get(j);
                java.time.LocalTime candTime = cand.getHeureArrivee().toLocalTime();
                if (!candTime.isAfter(windowEndLocal)) {
                    g.reservations.add(cand);
                    lastIncluded = candTime.isAfter(lastIncluded) ? candTime : lastIncluded;
                    j++;
                } else {
                    break;
                }
            }
            // departure = heure_arrivee du dernier vol inclus
            g.departureTime = Time.valueOf(lastIncluded);
            groups.add(g);
            i = j; // move to next window
        }
        return groups;
    }

    /**
     * Pour une date donnée, construit les groupes puis tente d'affecter les réservations
     * en respectant la logique métier existante. Persiste les trajets créés et met à jour
     * les statuts des réservations.
     *
     * Cette méthode réalise les opérations en mémoire puis persiste les résultats via les DAO.
     */
    public void assignGroupsForDate(Date date) throws SQLException {
        List<Group> groups = groupReservationsByDate(date);

        for (Group g : groups) {
            // map vehiculeId -> list of Reservations assigned in this group
            Map<Integer, List<Reservation>> assignments = new HashMap<>();

            // Sort reservations descending by nombrePersonnes (they already are ordered by time)
            g.reservations.sort((a,b) -> Integer.compare(b.getNombrePersonnes(), a.getNombrePersonnes()));

            // keep in-memory assigned capacity per vehicle for this group
            Map<Integer, Integer> inMemoryAssignedCapacity = new HashMap<>();

            for (Reservation r : g.reservations) {
                // Create a temporary reservation view where pickup time = group departure
                Reservation tmp = new Reservation();
                tmp.setId(r.getId());
                tmp.setDateArrivee(r.getDateArrivee());
                tmp.setHeureArrivee(g.departureTime);
                tmp.setNombrePersonnes(r.getNombrePersonnes());

                Vehicule chosen = selectVehicleForTmp(tmp, inMemoryAssignedCapacity);
                if (chosen != null) {
                    // record in-memory assignment (persist later)
                    assignments.computeIfAbsent(chosen.getId(), k -> new ArrayList<>()).add(r);
                    inMemoryAssignedCapacity.put(chosen.getId(), inMemoryAssignedCapacity.getOrDefault(chosen.getId(), 0) + r.getNombrePersonnes());
                } else {
                    // no vehicle found for this reservation in group
                    assignments.computeIfAbsent(-1, k -> new ArrayList<>()).add(r);
                }
            }
            // Persist associations and update statuses: assignments with key -1 are NON_ASSIGNE
            List<Reservation> nonAssigned = assignments.getOrDefault(-1, new ArrayList<>());
            for (Reservation na : nonAssigned) {
                reservationDAO.updateStatus(na.getId(), "NON_ASSIGNE");
            }

            // Persist actual assignments for vehicles (key != -1)
            for (Map.Entry<Integer, List<Reservation>> e : assignments.entrySet()) {
                if (e.getKey() == -1) continue;
                int vehiculeId = e.getKey();
                List<Reservation> assigned = e.getValue();

                // create reservation_vehicule rows
                for (Reservation ar : assigned) {
                    ReservationVehicule rv = new ReservationVehicule(ar.getId(), vehiculeId);
                    reservationVehiculeDAO.save(rv);
                    reservationDAO.updateStatus(ar.getId(), "ASSIGNE");
                }

                // create trajet for this vehicle
                VehiculeTrajet trajet = new VehiculeTrajet();
                trajet.setVehiculeId(vehiculeId);
                trajet.setDate(date);
                // store departure as TIMESTAMP (date + time)
                java.time.LocalDate d = date.toLocalDate();
                java.time.LocalDateTime dtStart = java.time.LocalDateTime.of(d, g.departureTime.toLocalTime());
                trajet.setHeureDepart(Timestamp.valueOf(dtStart));

                try {
                    Time heureRetour = tracabiliteService.calculerHeureRetour(vehiculeDAO.findById(vehiculeId), assigned);
                    if (heureRetour != null) {
                        java.time.LocalDateTime dtEnd = java.time.LocalDateTime.of(d, heureRetour.toLocalTime());
                        trajet.setHeureArrivee(Timestamp.valueOf(dtEnd));
                    }
                    double km = tracabiliteService.calculerDistanceTotale(assigned);
                    trajet.setKilometrageParcouru(km);
                } catch (SQLException ex) {
                    // best-effort: leave arrival/kilometrage null/0 if fails
                    System.err.println("Warning: failed to compute trajet metrics for vehicule " + vehiculeId + ": " + ex.getMessage());
                }

                // build JSON array liste_reservation for auditing (e.g. [1,2])
                StringBuilder sb = new StringBuilder();
                sb.append('[');
                for (int idx = 0; idx < assigned.size(); idx++) {
                    if (idx > 0) sb.append(',');
                    sb.append(assigned.get(idx).getId());
                }
                sb.append(']');
                trajet.setListeReservation(sb.toString());

                // persist trajet
                vehiculeTrajetDAO.save(trajet);

                // update reservation_vehicule rows to reference this trajet
                for (Reservation ar : assigned) {
                    ReservationVehicule rve = reservationVehiculeDAO.findByReservationId(ar.getId());
                    if (rve != null) {
                        reservationVehiculeDAO.setTrajetId(rve.getId(), trajet.getId());
                    }
                }

                // Optionally update vehicule.available_from to trajet.heure_arrivee
                if (trajet.getHeureArrivee() != null) {
                    vehiculeDAO.updateAvailableFrom(vehiculeId, trajet.getHeureArrivee());
                }
            }
        }
    }
}
