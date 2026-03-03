package service;

import dao.VehiculeDAO;
import dao.ReservationVehiculeDAO;
import models.Vehicule;
import models.ReservationVehicule;

import java.sql.SQLException;
import java.util.*;

/**
 * Service partagé pour l'assignation automatique d'un véhicule à une réservation.
 */
public class VehiculeSelectionService {

    private VehiculeDAO vehiculeDAO = new VehiculeDAO();
    private ReservationVehiculeDAO reservationVehiculeDAO = new ReservationVehiculeDAO();

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
        Vehicule vehicule = selectionnerVehicule(nombrePersonnes);
        if (vehicule != null) {
            ReservationVehicule rv = new ReservationVehicule(idReservation, vehicule.getId());
            reservationVehiculeDAO.save(rv);
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

    private int getPrioriteCarburant(String typeCarburant) {
        return PRIORITE_CARBURANT.getOrDefault(typeCarburant, 99);
    }
}
