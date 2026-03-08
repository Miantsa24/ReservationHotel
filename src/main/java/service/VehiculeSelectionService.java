package service;

import dao.VehiculeDAO;
import service.TracabiliteService;
import dao.ReservationVehiculeDAO;
import dao.ReservationDAO;
import models.Vehicule;
import models.ReservationVehicule;
import models.Reservation;

import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import java.util.*;

/**
 * Service partagé pour l'assignation automatique d'un véhicule à une réservation.
 */
public class VehiculeSelectionService {

    private VehiculeDAO vehiculeDAO = new VehiculeDAO();
    private ReservationVehiculeDAO reservationVehiculeDAO = new ReservationVehiculeDAO();
    private ReservationDAO reservationDAO = new ReservationDAO();
    private TracabiliteService tracabiliteService = new TracabiliteService();

    // Priorité carburant : Diesel > Essence > Hybride > Électrique
    private static final Map<String, Integer> PRIORITE_CARBURANT = new HashMap<>();
    static {
        PRIORITE_CARBURANT.put("Diesel", 1);
        PRIORITE_CARBURANT.put("Essence", 2);
        PRIORITE_CARBURANT.put("Hybride", 3);
        PRIORITE_CARBURANT.put("Électrique", 4);
        PRIORITE_CARBURANT.put("Electrique", 4);
    }

    /**
     * Sélectionne le meilleur véhicule selon les règles métier et l'assigne à la réservation.
     * 
     * @param idReservation l'ID de la réservation créée
     * @param nombrePersonnes le nombre de personnes de la réservation
     * @return le Vehicule assigné, ou null si aucun véhicule disponible
     */
    public Vehicule assignerVehicule(int idReservation, int nombrePersonnes) throws SQLException {
        Reservation reservation = reservationDAO.findById(idReservation);
        if (reservation == null) {
            // Fallback: use previous behaviour
            Vehicule vehicule = selectionnerVehicule(nombrePersonnes);
            if (vehicule != null) {
                ReservationVehicule rv = new ReservationVehicule(idReservation, vehicule.getId());
                reservationVehiculeDAO.save(rv);
            }
            return vehicule;
        }

        Vehicule vehicule = selectionnerVehiculeForReservation(reservation);
        if (vehicule != null) {
            ReservationVehicule rv = new ReservationVehicule(idReservation, vehicule.getId());
            reservationVehiculeDAO.save(rv);

            // Mettre à jour available_from du véhicule :
            // - si le véhicule est désormais plein pour la date, ou
            // - si la réservation donnée atteint/le dépasse le seuil d'attente (maxPickup + tempsAttente),
            //   auquel cas le véhicule va partir et doit être marqué indisponible jusqu'au retour.
            try {
                List<Reservation> resasVehicule = reservationVehiculeDAO.findReservationsByVehiculeAndDate(vehicule.getId(), reservation.getDateArrivee());

                int occupiedForDate = reservationVehiculeDAO.getOccupiedCapacityForDate(vehicule.getId(), reservation.getDateArrivee());

                // compute last assigned pickup time
                java.sql.Time maxTime = null;
                for (Reservation ar : resasVehicule) {
                    if (maxTime == null || ar.getHeureArrivee().after(maxTime)) {
                        maxTime = ar.getHeureArrivee();
                    }
                }

                boolean shouldSetAvailableFrom = false;

                if (occupiedForDate >= vehicule.getCapacite()) {
                    shouldSetAvailableFrom = true;
                } else if (maxTime != null) {
                    java.time.LocalDate dateLocal = reservation.getDateArrivee().toLocalDate();
                    java.time.LocalTime maxLocal = maxTime.toLocalTime();
                    java.time.LocalDateTime allowedLocal = java.time.LocalDateTime.of(dateLocal, maxLocal).plusMinutes(vehicule.getTempsAttente());
                    java.sql.Timestamp allowedTs = java.sql.Timestamp.valueOf(allowedLocal);
                    java.sql.Timestamp reservationTs = new java.sql.Timestamp(reservation.getDateArrivee().getTime() + reservation.getHeureArrivee().getTime());
                    if (!reservationTs.before(allowedTs)) { // reservationTs >= allowedTs
                        shouldSetAvailableFrom = true;
                    }
                }

                if (shouldSetAvailableFrom) {
                    java.sql.Time heureRetour = tracabiliteService.calculerHeureRetour(vehicule, resasVehicule);
                    if (heureRetour != null) {
                        java.time.LocalDate dateLocal = reservation.getDateArrivee().toLocalDate();
                        java.time.LocalTime timeLocal = heureRetour.toLocalTime();
                        java.time.LocalDateTime ldt = java.time.LocalDateTime.of(dateLocal, timeLocal);
                        java.sql.Timestamp ts = java.sql.Timestamp.valueOf(ldt);
                        vehiculeDAO.updateAvailableFrom(vehicule.getId(), ts);
                    }
                }
            } catch (Exception e) {
                // Ne pas casser le flux d'assignation si l'update échoue; log minimal
                System.err.println("Warning: failed to update available_from for vehicule " + vehicule.getId() + ": " + e.getMessage());
            }
        }
        return vehicule;
    }

    /**
     * Sélectionne le meilleur véhicule selon les 3 règles métier.
     * 
     * Règle 1 : Capacité la plus proche >= nombrePersonnes (écart minimal).
     * Règle 2 : En cas d'égalité de capacité, priorité carburant : Diesel > Essence > Hybride > Électrique.
     * Règle 3 : En cas d'égalité totale, sélection aléatoire.
     */
    public Vehicule selectionnerVehicule(int nombrePersonnes) throws SQLException {
        // Backwards-compatible: select disregarding dates (legacy behaviour)
        List<Vehicule> tous = vehiculeDAO.findAll();

        // Filtrer : capacité >= nombrePersonnes
        List<Vehicule> eligibles = new ArrayList<>();
        for (Vehicule v : tous) {
            if (v.getCapacite() >= nombrePersonnes) {
                eligibles.add(v);
            }
        }

        if (eligibles.isEmpty()) {
            return null;
        }

        // Règle 1 : Trouver l'écart minimal
        int ecartMin = Integer.MAX_VALUE;
        for (Vehicule v : eligibles) {
            int ecart = v.getCapacite() - nombrePersonnes;
            if (ecart < ecartMin) {
                ecartMin = ecart;
            }
        }

        // Garder uniquement ceux avec l'écart minimal
        List<Vehicule> meilleurCapacite = new ArrayList<>();
        for (Vehicule v : eligibles) {
            if (v.getCapacite() - nombrePersonnes == ecartMin) {
                meilleurCapacite.add(v);
            }
        }

        if (meilleurCapacite.size() == 1) {
            return meilleurCapacite.get(0);
        }

        // Règle 2 : Priorité carburant
        int meilleurePriorite = Integer.MAX_VALUE;
        for (Vehicule v : meilleurCapacite) {
            int prio = getPrioriteCarburant(v.getTypeCarburant());
            if (prio < meilleurePriorite) {
                meilleurePriorite = prio;
            }
        }

        List<Vehicule> meilleurCarburant = new ArrayList<>();
        for (Vehicule v : meilleurCapacite) {
            if (getPrioriteCarburant(v.getTypeCarburant()) == meilleurePriorite) {
                meilleurCarburant.add(v);
            }
        }

        if (meilleurCarburant.size() == 1) {
            return meilleurCarburant.get(0);
        }

        // Règle 3 : Sélection aléatoire parmi les restants
        Random random = new Random();
        return meilleurCarburant.get(random.nextInt(meilleurCarburant.size()));
    }

    /**
     * Selectionner en tenant compte de la date+heure de la réservation.
     * Autorise plusieurs réservations par véhicule si la capacité libre suffit.
     */
    public Vehicule selectionnerVehiculeForReservation(Reservation reservation) throws SQLException {
        List<Vehicule> tous = vehiculeDAO.findAll();
        Date date = reservation.getDateArrivee();
        Time time = reservation.getHeureArrivee();
        int personnes = reservation.getNombrePersonnes();

        // Filtrer par disponibilité (available_from == null OR <= reservation datetime)
        // Et prendre en compte le dernier horaire assigné + tempsAttente : si la réservation
        // arrive après ce seuil, le véhicule est considéré parti pour cette date.
        List<Vehicule> candidats = new ArrayList<>();
        java.sql.Timestamp reservationTs = new java.sql.Timestamp(date.getTime() + time.getTime());
        for (Vehicule v : tous) {
            java.sql.Timestamp av = v.getAvailableFrom();
            if (av != null) {
                // vehicle has a hard available_from set -> check against reservation datetime
                if (!av.after(reservationTs)) { // av <= reservationTs
                    candidats.add(v);
                }
                continue;
            }

            // No available_from set: check assigned reservations for that date
            List<Reservation> assigned = reservationVehiculeDAO.findReservationsByVehiculeAndDate(v.getId(), date);
            if (assigned == null || assigned.isEmpty()) {
                // no assignments yet -> candidate
                candidats.add(v);
                continue;
            }

            // compute last assigned pickup time
            java.sql.Time maxTime = null;
            for (Reservation ar : assigned) {
                if (maxTime == null || ar.getHeureArrivee().after(maxTime)) {
                    maxTime = ar.getHeureArrivee();
                }
            }

            if (maxTime == null) {
                candidats.add(v);
                continue;
            }

            // allowed latest pickup = maxTime + tempsAttente (minutes)
            java.time.LocalDate dateLocal = date.toLocalDate();
            java.time.LocalTime maxLocal = maxTime.toLocalTime();
            java.time.LocalDateTime allowedLocal = java.time.LocalDateTime.of(dateLocal, maxLocal).plusMinutes(v.getTempsAttente());
            java.sql.Timestamp allowedTs = java.sql.Timestamp.valueOf(allowedLocal);

            // If the requested reservation time is before or equal to allowedTs, candidate.
            // If it's after allowedTs, vehicle would have left before this pickup.
            if (!reservationTs.after(allowedTs)) {
                candidats.add(v);
            }
        }

        if (candidats.isEmpty()) {
            return null;
        }

        // Pour chaque candidat, calculer capacité libre pour la meme date+heure
        List<Vehicule> eligibles = new ArrayList<>();
        Map<Integer, Integer> freeCapacity = new HashMap<>();
        for (Vehicule v : candidats) {
            int occupied = reservationVehiculeDAO.getOccupiedCapacityForDateTime(v.getId(), date, time);
            int free = v.getCapacite() - occupied;
            freeCapacity.put(v.getId(), free);
            if (free >= personnes) {
                eligibles.add(v);
            }
        }

        if (eligibles.isEmpty()) {
            return null;
        }

        // Règle 1: choisir véhicule avec écart minimal après assignation (free - personnes minimal)
        int ecartMin = Integer.MAX_VALUE;
        for (Vehicule v : eligibles) {
            int ecart = freeCapacity.get(v.getId()) - personnes;
            if (ecart < ecartMin) ecartMin = ecart;
        }

        List<Vehicule> meilleurCapacite = new ArrayList<>();
        for (Vehicule v : eligibles) {
            if (freeCapacity.get(v.getId()) - personnes == ecartMin) {
                meilleurCapacite.add(v);
            }
        }

        if (meilleurCapacite.size() == 1) return meilleurCapacite.get(0);

        // Règle 2: Priorité carburant
        int meilleurePriorite = Integer.MAX_VALUE;
        for (Vehicule v : meilleurCapacite) {
            int prio = getPrioriteCarburant(v.getTypeCarburant());
            if (prio < meilleurePriorite) meilleurePriorite = prio;
        }

        List<Vehicule> meilleurCarburant = new ArrayList<>();
        for (Vehicule v : meilleurCapacite) {
            if (getPrioriteCarburant(v.getTypeCarburant()) == meilleurePriorite) {
                meilleurCarburant.add(v);
            }
        }

        if (meilleurCarburant.size() == 1) return meilleurCarburant.get(0);

        // Règle 3: aléatoire
        Random random = new Random();
        return meilleurCarburant.get(random.nextInt(meilleurCarburant.size()));
    }

    /**
     * Assign a batch of reservations (in-memory) sorted by descending nombrePersonnes.
     * Returns a map reservationId -> vehiculeAssigned (or null if none).
     */
    public Map<Integer, Vehicule> assignReservationsBatch(List<Reservation> reservations) throws SQLException {
        Map<Integer, Vehicule> result = new HashMap<>();
        // Sort by descending nombrePersonnes
        reservations.sort((a,b) -> Integer.compare(b.getNombrePersonnes(), a.getNombrePersonnes()));
        for (Reservation r : reservations) {
            Vehicule v = selectionnerVehiculeForReservation(r);
            if (v != null) {
                reservationVehiculeDAO.save(new ReservationVehicule(r.getId(), v.getId()));
            }
            result.put(r.getId(), v);
        }
        return result;
    }

    private int getPrioriteCarburant(String typeCarburant) {
        return PRIORITE_CARBURANT.getOrDefault(typeCarburant, 99);
    }
}
