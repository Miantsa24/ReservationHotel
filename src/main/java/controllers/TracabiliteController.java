package controllers;

import src.Controller;
import src.GetMapping;
import src.PostMapping;
import src.ModelView;
import src.RequestParam;

import dao.ReservationDAO;
import dao.ReservationVehiculeDAO;
import dao.VehiculeDAO;
import dao.HotelDAO;
import models.Reservation;
import models.ReservationVehicule;
import models.Vehicule;
import models.Hotel;
import models.VehiculeTracabilite;
import service.TracabiliteService;
import service.HotelRoutingService;

import java.sql.Date;
import java.sql.SQLException;
import java.util.*;

@Controller
public class TracabiliteController {

    private ReservationDAO reservationDAO = new ReservationDAO();
    private ReservationVehiculeDAO reservationVehiculeDAO = new ReservationVehiculeDAO();
    private VehiculeDAO vehiculeDAO = new VehiculeDAO();
    private HotelDAO hotelDAO = new HotelDAO();
    private TracabiliteService tracabiliteService = new TracabiliteService();
    private HotelRoutingService hotelRoutingService = new HotelRoutingService();

    /**
     * Page 1 — Affiche le formulaire de saisie de date
     */
    @GetMapping("/tracabilite")
    public ModelView showForm() {
        return new ModelView("/WEB-INF/views/tracabilite-form.jsp");
    }

    /**
     * Page 2 — Affiche la traçabilité des véhicules pour la date saisie.
     * Pour chaque véhicule ayant des réservations à cette date :
     *   - infos véhicule (marque, capacité, carburant, vitesse)
     *   - réservations assignées
     *   - hôtels parcourus
     *   - heure de départ et heure de retour à l'aéroport
     */
    @PostMapping("/tracabilite/resultat")
    public ModelView showResultat(@RequestParam("date") String date) {
        ModelView mv = new ModelView("/WEB-INF/views/tracabilite-resultat.jsp");
        mv.addItem("date", date);

        try {
            Date dateSql = Date.valueOf(date); // format yyyy-MM-dd
            List<Reservation> reservations = reservationDAO.findByDateArrivee(dateSql);

            // Grouper les réservations par véhicule
            Map<Integer, List<Reservation>> parVehicule = new LinkedHashMap<>();

            for (Reservation r : reservations) {
                ReservationVehicule rv = reservationVehiculeDAO.findByReservationId(r.getId());
                if (rv != null) {
                    int idVehicule = rv.getIdVehicule();
                    if (!parVehicule.containsKey(idVehicule)) {
                        parVehicule.put(idVehicule, new ArrayList<>());
                    }
                    parVehicule.get(idVehicule).add(r);
                }
            }

            // Construire la liste de VehiculeTracabilite
            List<VehiculeTracabilite> tracabilites = new ArrayList<>();

            for (Map.Entry<Integer, List<Reservation>> entry : parVehicule.entrySet()) {
                int idVehicule = entry.getKey();
                List<Reservation> resasVehicule = entry.getValue();

                Vehicule vehicule = vehiculeDAO.findById(idVehicule);
                if (vehicule == null) continue;

                VehiculeTracabilite vt = new VehiculeTracabilite();
                vt.setVehicule(vehicule);
                vt.setReservations(resasVehicule);

                // Collecter les hôtels parcourus (sans doublons)
                List<String> hotels = new ArrayList<>();
                for (Reservation r : resasVehicule) {
                    String nomHotel = r.getHotelNom();
                    if (nomHotel == null) {
                        Hotel h = hotelDAO.findById(r.getHotelId());
                        if (h != null) nomHotel = h.getNom();
                    }
                    if (nomHotel != null && !hotels.contains(nomHotel)) {
                        hotels.add(nomHotel);
                    }
                }

                // Ordonner les hôtels selon l'algorithme nearest-neighbour
                List<String> hotelsOrdonnes = hotelRoutingService.ordonnerHotels(hotels);
                vt.setHotels(hotelsOrdonnes);

                // Construire la chaîne de parcours : Aéroport -> H1 -> H2 -> Aéroport
                StringBuilder sb = new StringBuilder("Aéroport");
                for (String hname : hotelsOrdonnes) {
                    sb.append(" -> ").append(hname);
                }
                sb.append(" -> Aéroport");
                vt.setParcours(sb.toString());

                // Heure de départ et de retour
                vt.setHeureDepart(tracabiliteService.getHeureDepart(resasVehicule));
                java.sql.Time heureRetour = tracabiliteService.calculerHeureRetour(vehicule, resasVehicule);
                if (heureRetour == null) {
                    // fallback: prefer vehicule.available_from if present
                    try {
                        java.sql.Timestamp av = vehiculeDAO.findAvailableFrom(vehicule.getId());
                        if (av != null) {
                            heureRetour = new java.sql.Time(av.getTime());
                        } else {
                            // fallback: latest reservation heure_arrivee + 2 hours
                            java.time.LocalTime latest = resasVehicule.stream()
                                    .map(Reservation::getHeureArrivee)
                                    .filter(Objects::nonNull)
                                    .map(java.sql.Time::toLocalTime)
                                    .max(Comparator.naturalOrder())
                                    .orElse(java.time.LocalTime.MIDNIGHT);
                            java.time.LocalDate dateLocal = dateSql.toLocalDate();
                            java.time.LocalDateTime ldt = java.time.LocalDateTime.of(dateLocal, latest).plusHours(2);
                            heureRetour = new java.sql.Time(java.sql.Timestamp.valueOf(ldt).getTime());
                        }
                    } catch (Exception ex) {
                        // ignore and leave heureRetour null
                    }
                }
                vt.setHeureRetour(heureRetour);
                vt.setDistanceTotale(tracabiliteService.calculerDistanceTotale(resasVehicule));

                tracabilites.add(vt);
            }

            mv.addItem("tracabilites", tracabilites);
            mv.addItem("totalVehicules", tracabilites.size());

            // Dashboard stats: count reservations by status for this date
            int countAssignees = reservationDAO.countByDateAndStatus(dateSql, "ASSIGNE");
            int countNonAssignees = reservationDAO.countByDateAndStatus(dateSql, "NON_ASSIGNE");
            int countEnAttente = reservationDAO.countByDateAndStatus(dateSql, "EN_ATTENTE");
            int totalReservations = countAssignees + countNonAssignees + countEnAttente;
            
            mv.addItem("countAssignees", countAssignees);
            mv.addItem("countNonAssignees", countNonAssignees);
            mv.addItem("countEnAttente", countEnAttente);
            mv.addItem("totalReservations", totalReservations);

        } catch (Exception e) {
            e.printStackTrace();
            mv.addItem("error", "Erreur lors du chargement : " + e.getMessage());
        }

        return mv;
    }
}
