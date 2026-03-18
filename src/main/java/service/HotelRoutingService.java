package service;

import dao.DistanceDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service utilitaire pour ordonner les hôtels d'un véhicule
 * selon l'algorithme du plus proche voisin (nearest-neighbour).
 *
 * Règles :
 *  1. Départ depuis l'Aéroport.
 *  2. À chaque étape, choisir l'hôtel non encore visité le plus proche du lieu courant.
 *  3. En cas d'égalité de distance, prendre l'hôtel dans l'ordre alphabétique.
 */
public class HotelRoutingService {

    private static final String AEROPORT = "Aéroport";

    private DistanceDAO distanceDAO = new DistanceDAO();

    /**
     * Retourne la liste des noms d'hôtels ordonnée selon nearest-neighbour.
     *
     * @param nomsHotels liste (non ordonnée) des noms d'hôtels à visiter
     * @return la même liste réordonnée : du plus proche de l'aéroport au dernier
     */
    public List<String> ordonnerHotels(List<String> nomsHotels) throws SQLException {
        if (nomsHotels == null || nomsHotels.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> restants = new ArrayList<>(nomsHotels);
        List<String> ordonne = new ArrayList<>();
        String positionCourante = AEROPORT;

        while (!restants.isEmpty()) {
            String prochainHotel = trouverPlusProcheVoisin(positionCourante, restants);
            ordonne.add(prochainHotel);
            restants.remove(prochainHotel);
            positionCourante = prochainHotel;
        }

        return ordonne;
    }

    /**
     * Parmi les hôtels candidats, trouve celui le plus proche du lieu courant.
     * En cas d'égalité, retourne celui dont le nom vient en premier alphabétiquement.
     *
     * @param from       lieu de départ (nom d'hôtel ou "Aéroport")
     * @param candidats  hôtels encore à visiter
     * @return nom de l'hôtel le plus proche
     */
    private String trouverPlusProcheVoisin(String from, List<String> candidats) throws SQLException {
        String meilleur = null;
        double distanceMin = Double.MAX_VALUE;

        for (String hotel : candidats) {
            double km = distanceDAO.getKm(from, hotel);
            if (km < distanceMin
                    || (km == distanceMin && meilleur != null && hotel.compareTo(meilleur) < 0)) {
                distanceMin = km;
                meilleur = hotel;
            }
        }

        // Si aucune distance trouvée pour tous les candidats, fallback alphabétique
        if (meilleur == null) {
            System.err.println("[HotelRoutingService] Aucune distance trouvée depuis '" + from + "' vers candidats: " + candidats);
            meilleur = candidats.stream().sorted().findFirst().orElse(candidats.get(0));
        }

        return meilleur;
    }
}
