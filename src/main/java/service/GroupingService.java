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
import java.math.BigDecimal;
import java.sql.DriverManager;
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

    /**
     * Sélectionne un véhicule pour une réservation temporaire, en tenant compte de la disponibilité virtuelle.
     * @param tmp la réservation temporaire avec heure d'arrivée = heure de départ du groupe
     * @param inMemoryAssigned capacité déjà assignée en mémoire pour le groupe courant
     * @param virtualAvailableFrom disponibilité virtuelle des véhicules (calculée pour les groupes précédents non confirmés)
     */
    private Vehicule selectVehicleForTmp(Reservation tmp, Map<Integer,Integer> inMemoryAssigned,
                                         Map<Integer, Timestamp> virtualAvailableFrom) throws SQLException {
        java.sql.Date date = tmp.getDateArrivee();
        Time time = tmp.getHeureArrivee();

        java.util.List<Vehicule> tous = vehiculeDAO.findAll();
        java.util.List<Vehicule> candidats = new ArrayList<>();
        // Créer le timestamp correctement en combinant date et heure via LocalDateTime
        java.time.LocalDateTime reservationLdt = java.time.LocalDateTime.of(date.toLocalDate(), time.toLocalTime());
        java.sql.Timestamp reservationTs = java.sql.Timestamp.valueOf(reservationLdt);

        for (Vehicule v : tous) {
            // Déterminer la disponibilité effective du véhicule
            // On prend la plus tardive entre la disponibilité virtuelle et la disponibilité réelle
            java.sql.Timestamp virtualAv = virtualAvailableFrom.get(v.getId());
            java.sql.Timestamp realAv = v.getAvailableFrom();
            java.sql.Timestamp effectiveAv = null;

            if (virtualAv != null && realAv != null) {
                // Prendre la plus tardive des deux
                effectiveAv = virtualAv.after(realAv) ? virtualAv : realAv;
            } else if (virtualAv != null) {
                effectiveAv = virtualAv;
            } else if (realAv != null) {
                effectiveAv = realAv;
            }

            if (effectiveAv != null) {
                // Le véhicule est disponible si effectiveAv <= reservationTs
                if (!effectiveAv.after(reservationTs)) {
                    candidats.add(v);
                }
                continue; // Pas besoin de vérifier le fallback si on a une disponibilité définie
            }

            // Fallback: vérifier les réservations assignées pour cette date (quand pas de disponibilité définie)
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

    // Version backwards-compatible sans disponibilité virtuelle (pour assignGroupsForDate)
    private Vehicule selectVehicleForTmp(Reservation tmp, Map<Integer,Integer> inMemoryAssigned) throws SQLException {
        return selectVehicleForTmp(tmp, inMemoryAssigned, new HashMap<>());
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
                    rv.setPassengersAssigned(ar.getNombrePersonnes()); // Sprint 7: définir le nombre de passagers assignés
                    reservationVehiculeDAO.insertReservationVehicule(rv);
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

    /**
     * Compute-only: construit une proposition d'affectation pour une date donnée
     * sans effectuer de persistance. Retourne un AssignmentProposal détaillant
     * les groupes, la proposition par réservation et le résumé par véhicule.
     *
     * Sprint 7: Utilise le nouvel algorithme avec fragmentation et scoring.
     */
    public models.AssignmentProposal computeAssignmentsForDate(Date date) throws SQLException {
        // Sprint 7: Déléguer au nouvel algorithme avec fragmentation
        return computeAssignmentsSprint7ForDate(date);
    }

    /**
     * ANCIEN ALGORITHME (conservé pour référence/rollback si nécessaire)
     * Utilise selectVehicleForTmp (1 réservation → 1 véhicule, pas de fragmentation)
     */
    @Deprecated
    public models.AssignmentProposal computeAssignmentsForDateLegacy(Date date) throws SQLException {
        models.AssignmentProposal proposal = new models.AssignmentProposal();
        proposal.setDate(date);

        List<Group> groups = groupReservationsByDate(date);

        // Disponibilité virtuelle des véhicules (mise à jour après chaque groupe)
        // Ceci permet de savoir quand un véhicule sera disponible après avoir traité un groupe non confirmé
        Map<Integer, Timestamp> virtualAvailableFrom = new HashMap<>();
        int __debug_group_index = 0;

        for (Group g : groups) {
            models.AssignmentProposal.GroupProposal gp = new models.AssignmentProposal.GroupProposal();
            gp.departureTime = g.departureTime;

            // in-memory assigned capacity per vehicle
            Map<Integer, Integer> inMemoryAssignedCapacity = new HashMap<>();
            // map vehicle -> reservations assigned in this group
            Map<Integer, List<Integer>> assignments = new HashMap<>();

            // sort descending by size
            g.reservations.sort((a,b) -> Integer.compare(b.getNombrePersonnes(), a.getNombrePersonnes()));

            for (Reservation r : g.reservations) {
                Reservation tmp = new Reservation();
                tmp.setId(r.getId());
                tmp.setDateArrivee(r.getDateArrivee());
                tmp.setHeureArrivee(g.departureTime);
                tmp.setNombrePersonnes(r.getNombrePersonnes());

                Vehicule chosen = selectVehicleForTmp(tmp, inMemoryAssignedCapacity, virtualAvailableFrom);
                models.AssignmentProposal.ReservationProposal rp = new models.AssignmentProposal.ReservationProposal();
                rp.reservationId = r.getId();
                if (chosen != null) {
                    rp.proposedVehiculeId = chosen.getId();
                    assignments.computeIfAbsent(chosen.getId(), k -> new ArrayList<>()).add(r.getId());
                    inMemoryAssignedCapacity.put(chosen.getId(), inMemoryAssignedCapacity.getOrDefault(chosen.getId(), 0) + r.getNombrePersonnes());
                } else {
                    rp.proposedVehiculeId = null;
                    rp.reason = "NO_VEHICLE";
                }
                gp.reservations.add(rp);
            }

            // DEBUG: afficher contenu du groupe et des assignments calculés
            try {
                StringBuilder ids = new StringBuilder();
                for (models.AssignmentProposal.ReservationProposal rp : gp.reservations) {
                    if (ids.length() > 0) ids.append(','); ids.append(rp.reservationId);
                }
                System.out.println("DEBUG-GROUPING: processing groupIndex=" + __debug_group_index + " departure=" + gp.departureTime + " reservations=[" + ids.toString() + "]");

                StringBuilder asm = new StringBuilder();
                for (Map.Entry<Integer, List<Integer>> e : assignments.entrySet()) {
                    if (asm.length() > 0) asm.append(' ');
                    asm.append(e.getKey()).append("->").append(e.getValue());
                }
                System.out.println("DEBUG-GROUPING: assignments map for groupIndex=" + __debug_group_index + " : " + asm.toString());
            } catch (Exception __dbgEx) { System.out.println("DEBUG-GROUPING: failed to print debug info: " + __dbgEx.getMessage()); }

            // Compute actual departure time: last assigned reservation's arrival time
            // If the last reservation in the group is non-assigned, use the last assigned one
            Time actualDepartureTime = null;
            for (Reservation r : g.reservations) {
                // Check if this reservation is assigned (has a vehicle)
                boolean isAssigned = false;
                for (models.AssignmentProposal.ReservationProposal rp : gp.reservations) {
                    if (rp.reservationId == r.getId() && rp.proposedVehiculeId != null) {
                        isAssigned = true;
                        break;
                    }
                }
                if (isAssigned) {
                    // Update actualDepartureTime to the latest assigned reservation's arrival time
                    if (actualDepartureTime == null || r.getHeureArrivee().after(actualDepartureTime)) {
                        actualDepartureTime = r.getHeureArrivee();
                    }
                }
            }
            // Update group departure time for display
            if (actualDepartureTime != null) {
                gp.departureTime = actualDepartureTime;
            }

            // fill vehicle summaries for this group's assignments
            for (Map.Entry<Integer, List<Integer>> e : assignments.entrySet()) {
                Integer vid = e.getKey();
                List<Integer> rids = e.getValue();
                models.AssignmentProposal.VehicleSummary vs = proposal.getVehicleSummaries().getOrDefault(vid, new models.AssignmentProposal.VehicleSummary());
                vs.vehiculeId = vid;
                vs.reservationIds.addAll(rids);
                // estimate kilometrage and times using TracabiliteService by loading Reservation objects
                List<Reservation> assignedResas = new ArrayList<>();
                for (Integer rid : rids) {
                    Reservation res = reservationDAO.findById(rid);
                    if (res != null) assignedResas.add(res);
                }
                try {
                    vs.estimatedKilometrage = tracabiliteService.calculerDistanceTotale(assignedResas);
                    // Use actual departure time (last assigned reservation's arrival time)
                    if (actualDepartureTime != null) {
                        java.time.LocalDate d = date.toLocalDate();
                        java.time.LocalDateTime departLdt = java.time.LocalDateTime.of(d, actualDepartureTime.toLocalTime());
                        vs.heureDepart = java.sql.Timestamp.valueOf(departLdt);
                    }

                    // Estimate arrival based on total km and vehicle average speed
                    models.Vehicule veh = vehiculeDAO.findById(vid);
                    if (veh != null && vs.estimatedKilometrage > 0 && veh.getVitesseMoyenne() != null
                            && veh.getVitesseMoyenne().compareTo(BigDecimal.ZERO) != 0) {
                        double vitesse = veh.getVitesseMoyenne().doubleValue();
                        double heures = vs.estimatedKilometrage / vitesse;
                        long ms = (long) (heures * 3600 * 1000);
                        if (vs.heureDepart != null) {
                            long arriveeMs = vs.heureDepart.getTime() + ms;
                            vs.heureArrivee = new java.sql.Timestamp(arriveeMs);
                        }
                    } else {
                        // leave heureArrivee null when we cannot estimate
                    }
                } catch (SQLException ex) {
                    // ignore estimation failures; leave default values
                }
                proposal.getVehicleSummaries().put(vid, vs);

                // Mettre à jour la disponibilité virtuelle pour ce véhicule
                // Ceci permet aux groupes suivants de savoir que ce véhicule ne sera disponible qu'après son retour
                if (vs.heureArrivee != null) {
                    Timestamp currentVirtual = virtualAvailableFrom.get(vid);
                    // Prendre la date de retour la plus tardive si le véhicule était déjà assigné
                    if (currentVirtual == null || vs.heureArrivee.after(currentVirtual)) {
                        virtualAvailableFrom.put(vid, vs.heureArrivee);
                    }
                }
            }

            // DEBUG: afficher résumé véhicules courants (après mise à jour pour ce groupe)
            try {
                StringBuilder vsSummary = new StringBuilder();
                for (Map.Entry<Integer, models.AssignmentProposal.VehicleSummary> e : proposal.getVehicleSummaries().entrySet()) {
                    if (vsSummary.length() > 0) vsSummary.append(' ');
                    vsSummary.append(e.getKey()).append("->").append(e.getValue().reservationIds);
                }
                System.out.println("DEBUG-GROUPING: vehicleSummaries after groupIndex=" + __debug_group_index + " : " + vsSummary.toString());
            } catch (Exception __dbgEx) { System.out.println("DEBUG-GROUPING: failed to print vehicle summaries: " + __dbgEx.getMessage()); }

            proposal.getGroups().add(gp);
            __debug_group_index++;
        }

        // DEBUG: final proposal overview
        try { System.out.println("DEBUG-GROUPING: final proposal groups=" + proposal.getGroups().size() + " vehicleSummaries=" + proposal.getVehicleSummaries().keySet()); } catch (Exception __dbgEx) {}

        return proposal;
    }

    /**
     * Résultat d'allocation en mémoire pour un groupe/date.
     */
    public static class AllocationResult {
        public List<ReservationVehicule> assignments = new ArrayList<>();
        public List<Reservation> remainingReservations = new ArrayList<>();
        public Map<Integer, Integer> finalVehicleRemaining = new HashMap<>();
    }

    /**
     * Core allocation algorithm (memory-only). Does NOT persist.
     * Follows Sprint 7 rules: fragmentation allowed, score = remainingCap - reservation.remaining
     */
    public AllocationResult allocateForGroup(Date date, Time windowStart, List<Reservation> reservations, List<Vehicule> vehicules) throws SQLException {
        AllocationResult result = new AllocationResult();

        // Copy reservations into working list and ensure assignedCount is set
        List<Reservation> workRes = new ArrayList<>();
        for (Reservation r : reservations) {
            // create shallow copy to avoid mutating caller objects
            Reservation copy = new Reservation();
            copy.setId(r.getId());
            copy.setHotelId(r.getHotelId());
            copy.setDateArrivee(r.getDateArrivee());
            copy.setHeureArrivee(r.getHeureArrivee());
            copy.setNombrePersonnes(r.getNombrePersonnes());
            copy.setRefClient(r.getRefClient());
            copy.setAssignedCount(r.getAssignedCount());
            workRes.add(copy);
        }

        // Initialize remaining capacities using DB (sum of passengers_assigned)
        Map<Integer, Integer> remainingCap = new HashMap<>();
        for (Vehicule v : vehicules) {
            int occupied = reservationVehiculeDAO.sumAssignedByVehicule(v.getId());
            int free = v.getCapacite() - occupied;
            remainingCap.put(v.getId(), Math.max(0, free));
        }

        // Sort reservations descending by nombrePersonnes as preparation step
        workRes.sort((a,b) -> Integer.compare(b.getNombrePersonnes(), a.getNombrePersonnes()));

        // For each vehicle, try to fill it progressively
        for (Vehicule v : vehicules) {
            int vid = v.getId();
            int free = remainingCap.getOrDefault(vid, 0);
            if (free <= 0) continue;

            // While there is free capacity and there exist reservations with remaining>0
            while (free > 0) {
                // Build candidate list of reservations with remaining > 0
                List<Reservation> candidates = new ArrayList<>();
                for (Reservation r : workRes) {
                    if (r.getRemaining() > 0) candidates.add(r);
                }
                if (candidates.isEmpty()) break;

                // Choose best candidate per Sprint7: prefer negative score, then smallest abs(score), tie by date/time
                Reservation best = null;
                int bestScore = Integer.MAX_VALUE;
                for (Reservation cand : candidates) {
                    int score = free - cand.getRemaining();
                    if (best == null) { best = cand; bestScore = score; continue; }
                    boolean bestNeg = bestScore < 0;
                    boolean curNeg = score < 0;
                    if (curNeg && !bestNeg) {
                        best = cand; bestScore = score; continue;
                    }
                    if (curNeg == bestNeg) {
                        int absCur = Math.abs(score);
                        int absBest = Math.abs(bestScore);
                        if (absCur < absBest) { best = cand; bestScore = score; continue; }
                        if (absCur == absBest) {
                            // tie breaker by date then time then id
                            int cmpDate = cand.getDateArrivee().compareTo(best.getDateArrivee());
                            if (cmpDate < 0) { best = cand; bestScore = score; continue; }
                            if (cmpDate == 0) {
                                int cmpTime = cand.getHeureArrivee().compareTo(best.getHeureArrivee());
                                if (cmpTime < 0) { best = cand; bestScore = score; continue; }
                                if (cmpTime == 0 && cand.getId() < best.getId()) { best = cand; bestScore = score; continue; }
                            }
                        }
                    }
                }

                if (best == null) break;

                int need = best.getRemaining();
                int assign = Math.min(free, need);

                // Sprint 7: ne créer ReservationVehicule que si passagers > 0
                if (assign <= 0) break;

                // create reservation_vehicule assignment in memory
                ReservationVehicule rv = new ReservationVehicule(best.getId(), vid);
                rv.setPassengersAssigned(assign);
                result.assignments.add(rv);

                // update in-memory counters
                best.setAssignedCount(best.getAssignedCount() + assign);
                free -= assign;
            }

            remainingCap.put(vid, free);
        }

        // After filling vehicles, collect remaining reservations
        for (Reservation r : workRes) {
            if (r.getRemaining() > 0) result.remainingReservations.add(r);
        }

        result.finalVehicleRemaining.putAll(remainingCap);
        return result;
    }

    

    /**
     * Persist a previously computed AssignmentProposal in a single DB transaction.
     * Writes `reservation_vehicule`, `vehicule_trajet` and updates `reservation_vehicule.vehicule_trajet_id`
     * and `vehicules.available_from`.
     */
    public void persistAssignments(models.AssignmentProposal proposal) throws SQLException {
        // open a dedicated connection for transactional persistence to avoid
        // interference with DAOs that open/close the shared connection.
        String url = System.getProperty("db.url", "jdbc:mysql://localhost:3306/hotel_db?serverTimezone=UTC");
        String user = System.getProperty("db.user", "root");
        String pass = System.getProperty("db.password", "");
        try (java.sql.Connection conn = DriverManager.getConnection(url, user, pass)) {
            boolean previousAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // PreparedStatements for insertion
            String insertRvSql = "INSERT INTO reservation_vehicule (id_reservation, id_vehicule) VALUES (?, ?)";
            String insertTrajetSql = "INSERT INTO vehicule_trajet (vehicule_id, date, heure_depart, heure_arrivee, liste_reservation, kilometrage_parcouru) VALUES (?, ?, ?, ?, ?, ?)";
            String updateRvTrajetSql = "UPDATE reservation_vehicule SET vehicule_trajet_id = ? WHERE id = ?";
            String updateVehiculeAvailable = "UPDATE vehicules SET available_from = ? WHERE id = ?";

                                try (java.sql.PreparedStatement insertRvStmt = conn.prepareStatement(insertRvSql, java.sql.Statement.RETURN_GENERATED_KEYS);
                                    java.sql.PreparedStatement insertTrajetStmt = conn.prepareStatement(insertTrajetSql, java.sql.Statement.RETURN_GENERATED_KEYS);
                                    java.sql.PreparedStatement updateRvTrajetStmt = conn.prepareStatement(updateRvTrajetSql);
                                    java.sql.PreparedStatement updateVehiculeStmt = conn.prepareStatement(updateVehiculeAvailable);
                                    java.sql.PreparedStatement updateReservationStatusStmt = conn.prepareStatement("UPDATE reservations SET status = ? WHERE id = ?");
                                    java.sql.PreparedStatement updateTrajetsEffectuesStmt = conn.prepareStatement("UPDATE vehicules SET trajets_effectues = trajets_effectues + 1 WHERE id = ?")) {

                // First, update status for non-assigned reservations (proposedVehiculeId == null)
                for (models.AssignmentProposal.GroupProposal gp : proposal.getGroups()) {
                    for (models.AssignmentProposal.ReservationProposal rp : gp.reservations) {
                        if (rp.proposedVehiculeId == null) {
                            updateReservationStatusStmt.setString(1, "NON_ASSIGNE");
                            updateReservationStatusStmt.setInt(2, rp.reservationId);
                            updateReservationStatusStmt.executeUpdate();
                        }
                    }
                }

                // For each vehicle summary, create reservation_vehicule rows, then a vehicule_trajet
                for (models.AssignmentProposal.VehicleSummary vs : proposal.getVehicleSummaries().values()) {
                    if (vs.reservationIds.isEmpty()) continue;

                    List<Integer> createdRvIds = new ArrayList<>();
                    for (Integer rid : vs.reservationIds) {
                        insertRvStmt.setInt(1, rid);
                        insertRvStmt.setInt(2, vs.vehiculeId);
                        insertRvStmt.executeUpdate();
                        try (java.sql.ResultSet gk = insertRvStmt.getGeneratedKeys()) {
                            if (gk.next()) createdRvIds.add(gk.getInt(1));
                        }
                        // mark reservation ASSIGNE using the same transactional connection
                        updateReservationStatusStmt.setString(1, "ASSIGNE");
                        updateReservationStatusStmt.setInt(2, rid);
                        updateReservationStatusStmt.executeUpdate();
                    }

                    // build JSON list of reservations
                    StringBuilder sb = new StringBuilder();
                    sb.append('[');
                    for (int i = 0; i < vs.reservationIds.size(); i++) {
                        if (i > 0) sb.append(','); sb.append(vs.reservationIds.get(i));
                    }
                    sb.append(']');

                    // insert trajet
                    insertTrajetStmt.setInt(1, vs.vehiculeId);
                    insertTrajetStmt.setDate(2, proposal.getDate());
                    insertTrajetStmt.setTimestamp(3, vs.heureDepart);
                    insertTrajetStmt.setTimestamp(4, vs.heureArrivee);
                    insertTrajetStmt.setString(5, sb.toString());
                    insertTrajetStmt.setDouble(6, vs.estimatedKilometrage);
                    insertTrajetStmt.executeUpdate();
                    int trajetId = 0;
                    try (java.sql.ResultSet gk2 = insertTrajetStmt.getGeneratedKeys()) {
                        if (gk2.next()) trajetId = gk2.getInt(1);
                    }

                    // update reservation_vehicule rows to reference trajet
                    for (Integer createdRvId : createdRvIds) {
                        updateRvTrajetStmt.setInt(1, trajetId);
                        updateRvTrajetStmt.setInt(2, createdRvId);
                        updateRvTrajetStmt.executeUpdate();
                    }

                    // update vehicule.available_from if heureArrivee present
                    if (vs.heureArrivee != null) {
                        updateVehiculeStmt.setTimestamp(1, vs.heureArrivee);
                        updateVehiculeStmt.setInt(2, vs.vehiculeId);
                        updateVehiculeStmt.executeUpdate();
                    }

                    // increment trajets_effectues counter for the vehicle
                    try {
                        updateTrajetsEffectuesStmt.setInt(1, vs.vehiculeId);
                        updateTrajetsEffectuesStmt.executeUpdate();
                    } catch (SQLException se) {
                        // Non critique : log and continuer
                        System.err.println("Warning: failed to increment trajets_effectues for vehicule " + vs.vehiculeId + ": " + se.getMessage());
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(previousAuto);
            }

            // connection closed by try-with-resources
        }


    }
    
    /**
     * Sprint 7: Compute assignments using the new allocation algorithm with fragmentation support.
     * Returns an AssignmentProposal compatible with the existing UI.
     */
    public models.AssignmentProposal computeAssignmentsSprint7ForDate(Date date) throws SQLException {
        models.AssignmentProposal proposal = new models.AssignmentProposal();
        proposal.setDate(date);

        List<Group> groups = groupReservationsByDate(date);
        List<Vehicule> allVehicules = vehiculeDAO.findAll();

        // Disponibilité virtuelle des véhicules (mise à jour après chaque groupe)
        Map<Integer, Timestamp> virtualAvailableFrom = new HashMap<>();

        for (Group g : groups) {
            models.AssignmentProposal.GroupProposal gp = new models.AssignmentProposal.GroupProposal();
            gp.departureTime = g.departureTime;

            // Calculer le timestamp du départ du groupe
            java.time.LocalDateTime groupDepartLdt = java.time.LocalDateTime.of(date.toLocalDate(), g.departureTime.toLocalTime());
            java.sql.Timestamp groupDepartTs = java.sql.Timestamp.valueOf(groupDepartLdt);

            // Filtrer les véhicules disponibles pour ce groupe
            List<Vehicule> availableVehicules = new ArrayList<>();
            for (Vehicule v : allVehicules) {
                Timestamp virtualAv = virtualAvailableFrom.get(v.getId());
                Timestamp realAv = v.getAvailableFrom();
                Timestamp effectiveAv = null;

                if (virtualAv != null && realAv != null) {
                    effectiveAv = virtualAv.after(realAv) ? virtualAv : realAv;
                } else if (virtualAv != null) {
                    effectiveAv = virtualAv;
                } else if (realAv != null) {
                    effectiveAv = realAv;
                }

                // Véhicule disponible si effectiveAv <= groupDepartTs ou pas de contrainte
                if (effectiveAv == null || !effectiveAv.after(groupDepartTs)) {
                    availableVehicules.add(v);
                }
            }

            // Utiliser allocateForGroup avec les véhicules disponibles
            AllocationResult allocResult = allocateForGroup(date, g.departureTime, g.reservations, availableVehicules);

            // Convertir AllocationResult en ReservationProposal pour l'UI
            // Map reservationId -> list of (vehiculeId, passengersAssigned)
            Map<Integer, List<int[]>> assignmentsByReservation = new HashMap<>();
            for (ReservationVehicule rv : allocResult.assignments) {
                assignmentsByReservation.computeIfAbsent(rv.getIdReservation(), k -> new ArrayList<>())
                    .add(new int[]{rv.getIdVehicule(), rv.getPassengersAssigned()});
            }

            // Créer les ReservationProposal pour chaque réservation du groupe
            for (Reservation r : g.reservations) {
                models.AssignmentProposal.ReservationProposal rp = new models.AssignmentProposal.ReservationProposal();
                rp.reservationId = r.getId();

                List<int[]> assignments = assignmentsByReservation.get(r.getId());
                if (assignments != null && !assignments.isEmpty()) {
                    // Sprint 7: peut avoir plusieurs véhicules, on prend le premier pour proposedVehiculeId
                    // et on stocke les détails dans les champs supplémentaires
                    rp.proposedVehiculeId = assignments.get(0)[0];

                    // Calculer le total assigné pour cette réservation
                    int totalAssigned = 0;
                    StringBuilder vehicleDetails = new StringBuilder();
                    for (int[] a : assignments) {
                        totalAssigned += a[1];
                        if (vehicleDetails.length() > 0) vehicleDetails.append(", ");
                        vehicleDetails.append("V").append(a[0]).append(":").append(a[1]).append("p");
                    }
                    rp.passengersAssigned = totalAssigned;
                    rp.vehicleAssignments = vehicleDetails.toString();

                    // Si partiellement assigné
                    if (totalAssigned < r.getNombrePersonnes()) {
                        rp.reason = "PARTIAL";
                    }
                } else {
                    rp.proposedVehiculeId = null;
                    rp.reason = "NO_VEHICLE";
                    rp.passengersAssigned = 0;
                }
                gp.reservations.add(rp);
            }

            // Calculer l'heure de départ effective (dernier vol assigné)
            Time actualDepartureTime = null;
            for (Reservation r : g.reservations) {
                List<int[]> assignments = assignmentsByReservation.get(r.getId());
                if (assignments != null && !assignments.isEmpty()) {
                    if (actualDepartureTime == null || r.getHeureArrivee().after(actualDepartureTime)) {
                        actualDepartureTime = r.getHeureArrivee();
                    }
                }
            }
            if (actualDepartureTime != null) {
                gp.departureTime = actualDepartureTime;
            }

            // Construire les VehicleSummary pour chaque véhicule utilisé
            Map<Integer, List<ReservationVehicule>> assignmentsByVehicle = new HashMap<>();
            for (ReservationVehicule rv : allocResult.assignments) {
                assignmentsByVehicle.computeIfAbsent(rv.getIdVehicule(), k -> new ArrayList<>()).add(rv);
            }

            for (Map.Entry<Integer, List<ReservationVehicule>> entry : assignmentsByVehicle.entrySet()) {
                int vid = entry.getKey();
                List<ReservationVehicule> rvList = entry.getValue();

                models.AssignmentProposal.VehicleSummary vs = proposal.getVehicleSummaries()
                    .getOrDefault(vid, new models.AssignmentProposal.VehicleSummary());
                vs.vehiculeId = vid;

                // Ajouter les réservations et calculer les métriques
                List<Reservation> assignedResas = new ArrayList<>();
                int totalPassengers = 0;
                for (ReservationVehicule rv : rvList) {
                    vs.reservationIds.add(rv.getIdReservation());
                    vs.passengersPerReservation.put(rv.getIdReservation(), rv.getPassengersAssigned());
                    totalPassengers += rv.getPassengersAssigned();
                    Reservation res = reservationDAO.findById(rv.getIdReservation());
                    if (res != null) assignedResas.add(res);
                }
                vs.totalPassengers = totalPassengers;

                // Calculer les métriques du trajet
                try {
                    vs.estimatedKilometrage = tracabiliteService.calculerDistanceTotale(assignedResas);
                    if (actualDepartureTime != null) {
                        java.time.LocalDateTime departLdt = java.time.LocalDateTime.of(date.toLocalDate(), actualDepartureTime.toLocalTime());
                        vs.heureDepart = java.sql.Timestamp.valueOf(departLdt);
                    }

                    Vehicule veh = vehiculeDAO.findById(vid);
                    if (veh != null && vs.estimatedKilometrage > 0 && veh.getVitesseMoyenne() != null
                            && veh.getVitesseMoyenne().compareTo(BigDecimal.ZERO) != 0) {
                        double vitesse = veh.getVitesseMoyenne().doubleValue();
                        double heures = vs.estimatedKilometrage / vitesse;
                        long ms = (long) (heures * 3600 * 1000);
                        if (vs.heureDepart != null) {
                            vs.heureArrivee = new java.sql.Timestamp(vs.heureDepart.getTime() + ms);
                        }
                    }
                } catch (SQLException ex) {
                    // ignore estimation failures
                }

                proposal.getVehicleSummaries().put(vid, vs);

                // Mettre à jour la disponibilité virtuelle
                if (vs.heureArrivee != null) {
                    Timestamp currentVirtual = virtualAvailableFrom.get(vid);
                    if (currentVirtual == null || vs.heureArrivee.after(currentVirtual)) {
                        virtualAvailableFrom.put(vid, vs.heureArrivee);
                    }
                }
            }

            // Stocker les infos de fragmentation dans le groupe
            gp.allocationResult = allocResult;

            proposal.getGroups().add(gp);
        }

        return proposal;
    }

    /**
     * Persist an AllocationResult produced by `allocateForGroup` in a single DB transaction.
     */
    public void persistAllocationResult(Date date, AllocationResult alloc, Time windowStart) throws SQLException {
        String url = System.getProperty("db.url", "jdbc:mysql://localhost:3306/hotel_db?serverTimezone=UTC");
        String user = System.getProperty("db.user", "root");
        String pass = System.getProperty("db.password", "");

        try (java.sql.Connection conn = DriverManager.getConnection(url, user, pass)) {
            boolean previousAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);

            String insertRvSql = "INSERT INTO reservation_vehicule (id_reservation, id_vehicule, passengers_assigned) VALUES (?, ?, ?)";
            String insertTrajetSql = "INSERT INTO vehicule_trajet (vehicule_id, date, heure_depart, heure_arrivee, liste_reservation, kilometrage_parcouru) VALUES (?, ?, ?, ?, ?, ?)";
            String updateRvTrajetSql = "UPDATE reservation_vehicule SET vehicule_trajet_id = ? WHERE id = ?";
            String updateVehiculeAvailable = "UPDATE vehicules SET available_from = ? WHERE id = ?";
            String updateTrajetsEffectues = "UPDATE vehicules SET trajets_effectues = trajets_effectues + 1 WHERE id = ?";

            try (java.sql.PreparedStatement insertRvStmt = conn.prepareStatement(insertRvSql, java.sql.Statement.RETURN_GENERATED_KEYS);
                 java.sql.PreparedStatement insertTrajetStmt = conn.prepareStatement(insertTrajetSql, java.sql.Statement.RETURN_GENERATED_KEYS);
                 java.sql.PreparedStatement updateRvTrajetStmt = conn.prepareStatement(updateRvTrajetSql);
                 java.sql.PreparedStatement updateVehiculeStmt = conn.prepareStatement(updateVehiculeAvailable);
                 java.sql.PreparedStatement updateTrajetsStmt = conn.prepareStatement(updateTrajetsEffectues);
                 java.sql.PreparedStatement updateReservationAssignedStmt = conn.prepareStatement("UPDATE reservations SET assigned_count = assigned_count + ? WHERE id = ?");
                 java.sql.PreparedStatement updateReservationStatusStmt = conn.prepareStatement("UPDATE reservations SET status = CASE WHEN assigned_count >= nombre_personnes THEN 'ASSIGNE' WHEN assigned_count > 0 THEN 'ASSIGNE_PARTIEL' ELSE 'EN_ATTENTE' END WHERE id = ?");) {

                // 1) mark non-assigned reservations as NON_ASSIGNE
                for (Reservation r : alloc.remainingReservations) {
                    try (java.sql.PreparedStatement st = conn.prepareStatement("UPDATE reservations SET status = ? WHERE id = ?")) {
                        st.setString(1, "NON_ASSIGNE");
                        st.setInt(2, r.getId());
                        st.executeUpdate();
                    }
                }

                // 2) insert reservation_vehicule rows and collect created ids per vehicle
                // Map vehicleId -> list of created reservation_vehicule ids
                Map<Integer, List<Integer>> createdRvIdsByVeh = new HashMap<>();
                // Track per-reservation total assigned in this batch
                Map<Integer, Integer> assignedDeltaPerReservation = new HashMap<>();

                for (ReservationVehicule rv : alloc.assignments) {
                    // Sprint 7: ne créer que si passengers_assigned > 0
                    if (rv.getPassengersAssigned() <= 0) continue;

                    insertRvStmt.setInt(1, rv.getIdReservation());
                    insertRvStmt.setInt(2, rv.getIdVehicule());
                    insertRvStmt.setInt(3, rv.getPassengersAssigned());
                    insertRvStmt.executeUpdate();
                    int createdId = 0;
                    try (java.sql.ResultSet gk = insertRvStmt.getGeneratedKeys()) {
                        if (gk.next()) createdId = gk.getInt(1);
                    }
                    createdRvIdsByVeh.computeIfAbsent(rv.getIdVehicule(), k -> new ArrayList<>()).add(createdId);
                    assignedDeltaPerReservation.put(rv.getIdReservation(), assignedDeltaPerReservation.getOrDefault(rv.getIdReservation(), 0) + rv.getPassengersAssigned());
                }

                // 3) update reservations.assigned_count for each affected reservation
                for (Map.Entry<Integer, Integer> e : assignedDeltaPerReservation.entrySet()) {
                    int reservationId = e.getKey();
                    int delta = e.getValue();
                    updateReservationAssignedStmt.setInt(1, delta);
                    updateReservationAssignedStmt.setInt(2, reservationId);
                    updateReservationAssignedStmt.executeUpdate();

                    // update status based on new assigned_count vs nombre_personnes
                    updateReservationStatusStmt.setInt(1, reservationId);
                    updateReservationStatusStmt.executeUpdate();
                }

                // 4) create vehicule_trajet per vehicle and update reservation_vehicule rows to reference it
                for (Map.Entry<Integer, List<Integer>> e : createdRvIdsByVeh.entrySet()) {
                    int vid = e.getKey();
                    List<Integer> createdIds = e.getValue();
                    if (createdIds.isEmpty()) continue;

                    // Build JSON list of reservation ids for this vehicule_trajet
                    // We need reservation ids, fetch them by querying reservation_vehicule entries
                    StringBuilder sb = new StringBuilder();
                    sb.append('[');
                    // We will also build list of Reservation objects to estimate trajet metrics
                    List<Reservation> assignedResas = new ArrayList<>();
                    for (Integer createdRvId : createdIds) {
                        // retrieve reservation id for this reservation_vehicule
                        try (java.sql.PreparedStatement p = conn.prepareStatement("SELECT id_reservation FROM reservation_vehicule WHERE id = ?")) {
                            p.setInt(1, createdRvId);
                            try (java.sql.ResultSet rs = p.executeQuery()) {
                                if (rs.next()) {
                                    int rid = rs.getInt(1);
                                    if (sb.length() > 1) sb.append(','); sb.append(rid);
                                    Reservation res = reservationDAO.findById(rid);
                                    if (res != null) assignedResas.add(res);
                                }
                            }
                        }
                    }
                    sb.append(']');

                    // insert trajet: use windowStart as heure_depart
                    insertTrajetStmt.setInt(1, vid);
                    insertTrajetStmt.setDate(2, date);
                    // heure_depart
                    java.sql.Timestamp departTs = null;
                    if (windowStart != null) {
                        java.time.LocalDate d = date.toLocalDate();
                        java.time.LocalDateTime dt = java.time.LocalDateTime.of(d, windowStart.toLocalTime());
                        departTs = java.sql.Timestamp.valueOf(dt);
                    }
                    insertTrajetStmt.setTimestamp(3, departTs);

                    // heure_arrivee estimation
                    java.sql.Timestamp arriveeTs = null;
                    double km = 0.0;
                    try {
                        java.sql.Time heureRetour = tracabiliteService.calculerHeureRetour(vehiculeDAO.findById(vid), assignedResas);
                        if (heureRetour != null) {
                            java.time.LocalDate d = date.toLocalDate();
                            java.time.LocalDateTime ldt = java.time.LocalDateTime.of(d, heureRetour.toLocalTime());
                            arriveeTs = java.sql.Timestamp.valueOf(ldt);
                        }
                        km = tracabiliteService.calculerDistanceTotale(assignedResas);
                    } catch (SQLException ex) {
                        // ignore estimation failures
                    }
                    insertTrajetStmt.setTimestamp(4, arriveeTs);
                    insertTrajetStmt.setString(5, sb.toString());
                    insertTrajetStmt.setDouble(6, km);
                    insertTrajetStmt.executeUpdate();
                    int trajetId = 0;
                    try (java.sql.ResultSet gk = insertTrajetStmt.getGeneratedKeys()) { if (gk.next()) trajetId = gk.getInt(1); }

                    // update reservation_vehicule rows to reference trajet
                    for (Integer createdRvId : createdIds) {
                        updateRvTrajetStmt.setInt(1, trajetId);
                        updateRvTrajetStmt.setInt(2, createdRvId);
                        updateRvTrajetStmt.executeUpdate();
                    }

                    // update vehicule.available_from if arriveeTs present
                    if (arriveeTs != null) {
                        updateVehiculeStmt.setTimestamp(1, arriveeTs);
                        updateVehiculeStmt.setInt(2, vid);
                        updateVehiculeStmt.executeUpdate();
                    }

                    // increment trajets_effectues
                    try {
                        updateTrajetsStmt.setInt(1, vid);
                        updateTrajetsStmt.executeUpdate();
                    } catch (SQLException se) {
                        System.err.println("Warning: failed to increment trajets_effectues for vehicule " + vid + ": " + se.getMessage());
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(previousAuto);
            }
        }
    }
    
}
