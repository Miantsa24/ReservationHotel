package service;

import dao.VehiculeDAO;
import dao.ReservationVehiculeDAO;
import dao.ReservationDAO;
import models.Vehicule;
import models.ReservationVehicule;
import models.Reservation;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
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
        Vehicule vehicule;
        if (reservation != null) {
            // Filtrer selon available_from ET appliquer les règles métier
            vehicule = selectionnerVehicule(nombrePersonnes, reservation.getDateArrivee(), reservation.getHeureArrivee());
        } else {
            vehicule = selectionnerVehicule(nombrePersonnes);
        }
        if (vehicule != null) {
            ReservationVehicule rv = new ReservationVehicule(idReservation, vehicule.getId());
            reservationVehiculeDAO.save(rv);
            // Mettre à jour available_from après assignation
            mettreAJourDisponibilite(vehicule, idReservation);
        }
        return vehicule;
    }

    /**
     * Met à jour available_from du véhicule après assignation.
     * Calcule l'heure de retour à l'aéroport pour toutes les réservations du véhicule
     * à la même date, puis persiste le Timestamp résultant dans la colonne available_from.
     */
    private void mettreAJourDisponibilite(Vehicule vehicule, int idReservation) throws SQLException {
        Reservation reservation = reservationDAO.findById(idReservation);
        if (reservation == null) return;

        Date date = reservation.getDateArrivee();
        // Toutes les réservations assignées à ce véhicule pour cette date (inclut la nouvelle)
        List<Reservation> reservations = reservationDAO.findByVehiculeAndDate(vehicule.getId(), date);
        if (reservations.isEmpty()) return;

        Time heureRetour = tracabiliteService.calculerHeureRetour(vehicule, reservations);
        if (heureRetour == null) return;

        // Combiner la date de réservation et l'heure de retour en un Timestamp
        java.time.LocalDateTime ldt = java.time.LocalDateTime.of(
                date.toLocalDate(), heureRetour.toLocalTime());
        Timestamp availableFrom = Timestamp.valueOf(ldt);
        vehiculeDAO.updateAvailableFrom(vehicule.getId(), availableFrom);
    }

    /**
     * Sélectionne le meilleur véhicule en filtrant d'abord par disponibilité (available_from).
     *
     * Un véhicule est éligible si :
     *   - available_from IS NULL  (jamais utilisé)
     *   - OU available_from <= Timestamp(date, heure)  (déjà revenu à l'aéroport)
     *
     * Règle 1 : Capacité la plus proche >= nombrePersonnes (écart minimal).
     * Règle 2 : En cas d'égalité de capacité, priorité carburant : Diesel > Essence > Hybride > Électrique.
     * Règle 3 : En cas d'égalité totale, sélection aléatoire.
     */
    public Vehicule selectionnerVehicule(int nombrePersonnes, Date date, Time time) throws SQLException {
        List<Vehicule> tous = vehiculeDAO.findAll();
        Timestamp reservationTs = buildTimestamp(date, time);

        // Filtrer : disponible ET capacité suffisante
        List<Vehicule> eligibles = new ArrayList<>();
        for (Vehicule v : tous) {
            if (v.getCapacite() >= nombrePersonnes && estDisponible(v, reservationTs)) {
                eligibles.add(v);
            }
        }

        return appliquerReglesSelection(eligibles, nombrePersonnes);
    }

    /**
     * Sélectionne le meilleur véhicule selon les 3 règles métier (sans filtre de disponibilité).
     * Conservé pour compatibilité ascendante.
     *
     * Règle 1 : Capacité la plus proche >= nombrePersonnes (écart minimal).
     * Règle 2 : En cas d'égalité de capacité, priorité carburant : Diesel > Essence > Hybride > Électrique.
     * Règle 3 : En cas d'égalité totale, sélection aléatoire.
     */
    public Vehicule selectionnerVehicule(int nombrePersonnes) throws SQLException {
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

    // -------------------------------------------------------------------------
    // Méthodes utilitaires privées
    // -------------------------------------------------------------------------

    /**
     * Extrait la logique de sélection (règles 1-2-3) sur une liste déjà filtrée.
     */
    private Vehicule appliquerReglesSelection(List<Vehicule> eligibles, int nombrePersonnes) {
        if (eligibles.isEmpty()) return null;

        // Règle 1 : écart minimal de capacité
        int ecartMin = Integer.MAX_VALUE;
        for (Vehicule v : eligibles) {
            int ecart = v.getCapacite() - nombrePersonnes;
            if (ecart < ecartMin) ecartMin = ecart;
        }
        List<Vehicule> meilleurCapacite = new ArrayList<>();
        for (Vehicule v : eligibles) {
            if (v.getCapacite() - nombrePersonnes == ecartMin) meilleurCapacite.add(v);
        }
        if (meilleurCapacite.size() == 1) return meilleurCapacite.get(0);

        // Règle 2 : priorité carburant
        int meilleurePriorite = Integer.MAX_VALUE;
        for (Vehicule v : meilleurCapacite) {
            int prio = getPrioriteCarburant(v.getTypeCarburant());
            if (prio < meilleurePriorite) meilleurePriorite = prio;
        }
        List<Vehicule> meilleurCarburant = new ArrayList<>();
        for (Vehicule v : meilleurCapacite) {
            if (getPrioriteCarburant(v.getTypeCarburant()) == meilleurePriorite) meilleurCarburant.add(v);
        }
        if (meilleurCarburant.size() == 1) return meilleurCarburant.get(0);

        // Règle 3 : aléatoire
        return meilleurCarburant.get(new Random().nextInt(meilleurCarburant.size()));
    }

    /**
     * Retourne true si le véhicule est disponible à la date+heure demandée.
     * Condition : available_from IS NULL  OU  available_from <= reservationTs
     */
    private boolean estDisponible(Vehicule v, Timestamp reservationTs) {
        Timestamp av = v.getAvailableFrom();
        return av == null || !av.after(reservationTs);
    }

    /**
     * Construit un Timestamp à partir d'une Date SQL et d'une Time SQL.
     */
    private Timestamp buildTimestamp(Date date, Time time) {
        java.time.LocalDateTime ldt = java.time.LocalDateTime.of(
                date.toLocalDate(), time.toLocalTime());
        return Timestamp.valueOf(ldt);
    }

    private int getPrioriteCarburant(String typeCarburant) {
        return PRIORITE_CARBURANT.getOrDefault(typeCarburant, 99);
    }
}
