package service;

import dao.DistanceDAO;
import dao.HotelDAO;
import service.HotelRoutingService;
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
    private HotelRoutingService hotelRoutingService = new HotelRoutingService();

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

        // Construire la liste des hôtels uniques à visiter
        List<String> hotels = new java.util.ArrayList<>();
        for (Reservation r : reservations) {
            models.Hotel h = hotelDAO.findById(r.getHotelId());
            if (h != null && !hotels.contains(h.getNom())) {
                hotels.add(h.getNom());
            }
        }

        // Ordonner les hôtels via nearest-neighbour
        List<String> ordered = hotelRoutingService.ordonnerHotels(hotels);

        // Calculer la distance le long du parcours ordonné (Aéroport -> ... -> Aéroport)
        double totalKm = calculerDistanceParcours(ordered);

        if (Double.isInfinite(totalKm) || totalKm == 0) {
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
        // Essayer de calculer la distance le long du parcours ordonné
        List<String> hotels = new java.util.ArrayList<>();
        for (Reservation r : reservations) {
            Hotel h = hotelDAO.findById(r.getHotelId());
            if (h != null && !hotels.contains(h.getNom())) hotels.add(h.getNom());
        }
        List<String> ordered = hotelRoutingService.ordonnerHotels(hotels);
        double km = calculerDistanceParcours(ordered);
        if (!Double.isInfinite(km) && km > 0) return km;

        // Fallback : somme des aller-retour Aéroport <-> hôtel
        double totalKm = 0;
        for (Reservation r : reservations) {
            Hotel hotel = hotelDAO.findById(r.getHotelId());
            if (hotel != null) {
                double d = distanceDAO.getKm(AEROPORT, hotel.getNom());
                totalKm += d * 2; // aller + retour
            }
        }
        return totalKm;
    }

    /**
     * Calcule la distance totale le long d'un parcours ordonné de noms d'hôtels.
     * Parcours: Aéroport -> h1 -> h2 -> ... -> hn -> Aéroport
     */
    public double calculerDistanceParcours(List<String> orderedHotels) throws SQLException {
        double totalKm = 0;
        String current = AEROPORT;
        for (String h : orderedHotels) {
            double seg = distanceDAO.getKm(current, h);
            if (Double.isInfinite(seg)) return Double.POSITIVE_INFINITY;
            totalKm += seg;
            current = h;
        }
        // retour au point de départ
        double last = distanceDAO.getKm(current, AEROPORT);
        if (Double.isInfinite(last)) return Double.POSITIVE_INFINITY;
        totalKm += last;
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
