package controllers;

import src.Controller;
import src.GetMapping;
import src.PostMapping;
import src.ModelView;
import src.RequestParam;

import dao.ReservationDAO;
import dao.ReservationVehiculeDAO;
import dao.VehiculeDAO;
import dao.VehiculeTrajetDAO;
import dao.HotelDAO;
import models.Reservation;
import models.ReservationVehicule;
import models.TrajetTracabilite;
import models.Vehicule;
import models.Hotel;
import models.VehiculeTracabilite;
import models.VehiculeTrajet;
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
    private VehiculeTrajetDAO vehiculeTrajetDAO = new VehiculeTrajetDAO();
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

           List<VehiculeTrajet> trajetsDB = vehiculeTrajetDAO.findByDate(dateSql);

            // Grouper les trajets par véhicule
            Map<Integer, List<VehiculeTrajet>> trajetsParVehicule = new LinkedHashMap<>();

            for (VehiculeTrajet t : trajetsDB) {
                trajetsParVehicule
                    .computeIfAbsent(t.getVehiculeId(), k -> new ArrayList<>())
                    .add(t);
            }

            // Construire la liste de VehiculeTracabilite
            List<VehiculeTracabilite> tracabilites = new ArrayList<>();

            for (Map.Entry<Integer, List<VehiculeTrajet>> entry : trajetsParVehicule.entrySet()) {

                int idVehicule = entry.getKey();
                Vehicule vehicule = vehiculeDAO.findById(idVehicule);
                if (vehicule == null) continue;

                VehiculeTracabilite vt = new VehiculeTracabilite();
                vt.setVehicule(vehicule);

                List<TrajetTracabilite> trajetsList = new ArrayList<>();

                for (VehiculeTrajet trajet : entry.getValue()) {

                    TrajetTracabilite tt = new TrajetTracabilite();
                    tt.setTrajet(trajet);

                    // 🔹 Parser listeReservation
                    List<Reservation> resas = new ArrayList<>();
                    String json = trajet.getListeReservation(); // ex: [1,2,3]

                    if (json != null && json.length() > 2) {
                        String cleaned = json.replace("[", "").replace("]", "");
                        String[] ids = cleaned.split(",");

                        for (String idStr : ids) {
                            try {
                                int rid = Integer.parseInt(idStr.trim());
                                Reservation r = reservationDAO.findById(rid);
                                if (r != null) resas.add(r);
                            } catch (Exception ignore) {}
                        }
                    }

                    tt.setReservations(resas);

                    // 🔹 hôtels
                    List<String> hotels = new ArrayList<>();
                    for (Reservation r : resas) {
                        String h = r.getHotelNom();
                        if (h != null && !hotels.contains(h)) hotels.add(h);
                    }

                    List<String> ordered = hotelRoutingService.ordonnerHotels(hotels);
                    tt.setHotels(ordered);

                    // 🔹 étapes
                    try {
                        java.sql.Time depart = new java.sql.Time(trajet.getHeureDepart().getTime());
                        List<java.sql.Time> etapes = tracabiliteService.calculerHeuresParcours(ordered, depart, vehicule);
                        tt.setEtapeHeures(etapes);
                    } catch (Exception e) {}

                    trajetsList.add(tt);
                }

                vt.setTrajets(trajetsList);

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
