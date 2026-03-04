package service;

import dao.DistanceDAO;
import dao.HotelDAO;
import models.Hotel;
import models.Reservation;
import models.Vehicule;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Comparator;
import java.util.List;

/**
 * Service utilitaire pour le calcul de traçabilité des véhicules.
 * Calcule l'heure de retour du véhicule à l'aéroport après ses livraisons.
 *
 * Formule : temps_trajet (h) = total_km / vitesse_moyenne
 *           heure_retour = heure_depart + temps_trajet
 */
public class TracabiliteService {

    private static final String AEROPORT = "Aéroport";

    private DistanceDAO distanceDAO = new DistanceDAO();
    private HotelDAO hotelDAO = new HotelDAO();

    /**
     * Calcule l'heure de retour d'un véhicule à l'aéroport.
     *
     * Logique :
     * 1. Heure de départ = heure d'arrivée du 1er client (tri par heure)
     * 2. Pour chaque réservation : distance aller-retour Aéroport ↔ Hôtel
     * 3. total_km = somme des aller-retour
     * 4. temps_trajet (h) = total_km / vitesse_moyenne
     * 5. heure_retour = heure_depart + temps_trajet
     *
     * @param vehicule     le véhicule concerné
     * @param reservations les réservations assignées au véhicule pour la date (triées ou non)
     * @return l'heure de retour à l'aéroport, ou null si calcul impossible
     */
    public Time calculerHeureRetour(Vehicule vehicule, List<Reservation> reservations) throws SQLException {
        if (reservations == null || reservations.isEmpty()) {
            return null;
        }
        if (vehicule.getVitesseMoyenne() == null || vehicule.getVitesseMoyenne().compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        // Trier par heure d'arrivée
        reservations.sort(Comparator.comparing(Reservation::getHeureArrivee));

        // Heure de départ du véhicule = heure d'arrivée du 1er client
        Time heureDepart = reservations.get(0).getHeureArrivee();

        // Distance totale parcourue
        double totalKm = calculerDistanceTotale(reservations);

        if (totalKm == 0) {
            return null;
        }

        // temps_trajet (h) = total_km / vitesse_moyenne
        double vitesse = vehicule.getVitesseMoyenne().doubleValue();
        double tempsTrajetHeures = totalKm / vitesse;

        // Convertir en millisecondes et ajouter à l'heure de départ
        long tempsTrajetMs = (long) (tempsTrajetHeures * 3600 * 1000);
        long heureRetourMs = heureDepart.getTime() + tempsTrajetMs;

        return new Time(heureRetourMs);
    }

    /**
     * Calcule la distance totale parcourue par un véhicule pour ses réservations.
     * Pour chaque réservation : Aéroport → Hôtel + Hôtel → Aéroport (aller-retour).
     *
     * @param reservations les réservations du véhicule
     * @return la distance totale en km
     */
    public double calculerDistanceTotale(List<Reservation> reservations) throws SQLException {
        double totalKm = 0;
        for (Reservation r : reservations) {
            Hotel hotel = hotelDAO.findById(r.getHotelId());
            if (hotel != null) {
                double km = distanceDAO.getKm(AEROPORT, hotel.getNom());
                totalKm += km * 2; // aller + retour
            }
        }
        return totalKm;
    }

    /**
     * Récupère l'heure de départ du véhicule depuis l'aéroport.
     * Correspond à l'heure d'arrivée du premier client (ramassage).
     *
     * @param reservations les réservations assignées au véhicule
     * @return l'heure de départ, ou null si aucune réservation
     */
    public Time getHeureDepart(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return null;
        }
        reservations.sort(Comparator.comparing(Reservation::getHeureArrivee));
        return reservations.get(0).getHeureArrivee();
    }
}
