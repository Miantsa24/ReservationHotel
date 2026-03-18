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
import java.util.ArrayList;

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
     * 1. Heure de départ = heure d'arrivée du DERNIER client (tri par heure)
     * 2. Ordonner les hôtels via l'algorithme de routing
     * 3. total_km = distance du parcours (Aéroport -> h1 -> ... -> hn -> Aéroport)
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

        // Heure de départ du véhicule = heure d'arrivée du DERNIER client (règle Sprint-5)
        Time heureDepart = reservations.get(reservations.size() - 1).getHeureArrivee();

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
        // Construire la liste unique d'hôtels et calculer strictement
        // la distance du parcours ordonné : Aéroport -> h1 -> ... -> hn -> Aéroport
        List<String> hotels = new java.util.ArrayList<>();
        for (Reservation r : reservations) {
            Hotel h = hotelDAO.findById(r.getHotelId());
            if (h != null && !hotels.contains(h.getNom())) hotels.add(h.getNom());
        }
        if (hotels.isEmpty()) return 0;
        List<String> ordered = hotelRoutingService.ordonnerHotels(hotels);
        // calculerDistanceParcours effectue Aéroport->h1 + h1->h2 + ... + hn->Aéroport
        return calculerDistanceParcours(ordered);
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
            if (Double.isInfinite(seg)) {
                System.err.println("[TracabiliteService] distance manquante entre '" + current + "' et '" + h + "'");
                return Double.POSITIVE_INFINITY;
            }
            totalKm += seg;
            current = h;
        }
        // retour au point de départ
        double last = distanceDAO.getKm(current, AEROPORT);
        if (Double.isInfinite(last)) {
            System.err.println("[TracabiliteService] distance manquante entre '" + current + "' et '" + AEROPORT + "'");
            return Double.POSITIVE_INFINITY;
        }
        totalKm += last;
        return totalKm;
    }

    /**
     * Calcule les heures d'arrivée pour chaque étape du parcours ordonné.
     * Retourne une liste de Time correspondant à : [Aéroport (départ), h1, h2, ..., Aéroport (retour)].
     * Les heures sont calculées en partant de `heureDepart` et en ajoutant les durées segment par segment
     * en utilisant la vitesse moyenne du véhicule.
     */
    public List<java.sql.Time> calculerHeuresParcours(List<String> orderedHotels, java.sql.Time heureDepart, models.Vehicule vehicule) throws SQLException {
        List<java.sql.Time> heures = new ArrayList<>();
        if (heureDepart == null) return heures;
        heures.add(heureDepart); // Aéroport (départ)

        double vitesse = 0.0;
        if (vehicule != null && vehicule.getVitesseMoyenne() != null) {
            vitesse = vehicule.getVitesseMoyenne().doubleValue();
        }

        long currentMs = heureDepart.getTime();
        String current = AEROPORT;

        for (String h : orderedHotels) {
            double seg = distanceDAO.getKm(current, h);
            if (Double.isInfinite(seg) || vitesse <= 0) {
                // cannot compute -> add null marker
                heures.add(null);
            } else {
                long ms = (long) ((seg / vitesse) * 3600 * 1000);
                currentMs += ms;
                heures.add(new java.sql.Time(currentMs));
            }
            current = h;
        }

        // return leg
        double last = distanceDAO.getKm(current, AEROPORT);
        if (Double.isInfinite(last) || vitesse <= 0) {
            heures.add(null);
        } else {
            long ms = (long) ((last / vitesse) * 3600 * 1000);
            currentMs += ms;
            heures.add(new java.sql.Time(currentMs));
        }

        return heures;
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
        // Retourner l'heure d'arrivée du DERNIER client (règle Sprint-5)
        return reservations.get(reservations.size() - 1).getHeureArrivee();
    }
}
