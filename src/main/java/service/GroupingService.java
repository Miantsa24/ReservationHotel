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
        java.sql.Timestamp reservationTs = new java.sql.Timestamp(date.getTime() + time.getTime());

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

    /**
     * Compute-only: construit une proposition d'affectation pour une date donnée
     * sans effectuer de persistance. Retourne un AssignmentProposal détaillant
     * les groupes, la proposition par réservation et le résumé par véhicule.
     */
    public models.AssignmentProposal computeAssignmentsForDate(Date date) throws SQLException {
        models.AssignmentProposal proposal = new models.AssignmentProposal();
        proposal.setDate(date);

        List<Group> groups = groupReservationsByDate(date);

        // Disponibilité virtuelle des véhicules (mise à jour après chaque groupe)
        // Ceci permet de savoir quand un véhicule sera disponible après avoir traité un groupe non confirmé
        Map<Integer, Timestamp> virtualAvailableFrom = new HashMap<>();

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

            proposal.getGroups().add(gp);
        }

        return proposal;
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
        String pass = System.getProperty("db.password", "root");
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
                  java.sql.PreparedStatement updateReservationStatusStmt = conn.prepareStatement("UPDATE reservations SET status = ? WHERE id = ?")) {

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
}
