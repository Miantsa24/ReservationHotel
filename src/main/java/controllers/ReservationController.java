package controllers;

import src.Controller;
import src.GetMapping;
import src.PostMapping;
import src.ModelView;
import src.RequestParam;

import dao.HotelDAO;
import dao.ReservationDAO;
import models.Hotel;
import models.Reservation;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

@Controller
public class ReservationController {

    private HotelDAO hotelDAO = new HotelDAO();
    private ReservationDAO reservationDAO = new ReservationDAO();

    // ==================== FRONT-OFFICE ====================

    /**
     * Affiche la liste de toutes les réservations (Front-Office)
     */
    @GetMapping("/reservations")
    public ModelView listReservations() {
        ModelView mv = new ModelView("/WEB-INF/views/reservation-list.jsp");
        try {
            List<Reservation> reservations = reservationDAO.findAll();
            mv.addItem("reservations", reservations);
        } catch (Exception e) {
            mv.addItem("error", "Erreur lors du chargement des réservations: " + e.getMessage());
        }
        return mv;
    }

    /**
     * Filtre les réservations par date d'arrivée (Front-Office)
     */
    @GetMapping("/reservations/filter")
    public ModelView filterReservations(@RequestParam("dateArrivee") String dateArrivee) {
        ModelView mv = new ModelView("/WEB-INF/views/reservation-list.jsp");
        try {
            List<Reservation> reservations;
            if (dateArrivee != null && !dateArrivee.trim().isEmpty()) {
                Date date = Date.valueOf(dateArrivee);
                reservations = reservationDAO.findByDateArrivee(date);
                mv.addItem("filterDate", dateArrivee);
            } else {
                reservations = reservationDAO.findAll();
            }
            mv.addItem("reservations", reservations);
        } catch (Exception e) {
            mv.addItem("error", "Erreur lors du filtrage: " + e.getMessage());
            try {
                mv.addItem("reservations", reservationDAO.findAll());
            } catch (Exception ex) {
                // Ignorer
            }
        }
        return mv;
    }

    /**
     * Affiche le formulaire de réservation (Front-Office)
     */
    @GetMapping("/reservation/form")
    public ModelView showForm() {
        ModelView mv = new ModelView("/WEB-INF/views/reservation-form.jsp");
        try {
            List<Hotel> hotels = hotelDAO.findAll();
            mv.addItem("hotels", hotels);
        } catch (Exception e) {
            mv.addItem("error", "Erreur lors du chargement des hôtels: " + e.getMessage());
        }
        return mv;
    }

    /**
     * Enregistre une nouvelle réservation (Front-Office)
     */
    @PostMapping("/reservation/save")
    public ModelView saveReservation(@RequestParam("hotelId") String hotelId, 
            @RequestParam("dateArrivee") String dateArrivee, 
            @RequestParam("heureArrivee") String heureArrivee, 
            @RequestParam("dateDepart") String dateDepart, 
            @RequestParam("nombrePersonnes") String nombrePersonnes, 
            @RequestParam("nomClient") String nomClient,
            @RequestParam("emailClient") String emailClient, 
            @RequestParam("telephoneClient") String telephoneClient) {
        
        ModelView mv = new ModelView("/WEB-INF/views/reservation-form.jsp");
        
        try {
            // Création de la réservation
            Reservation reservation = new Reservation();
            reservation.setHotelId(Integer.parseInt(hotelId));
            reservation.setDateArrivee(Date.valueOf(dateArrivee));
            reservation.setHeureArrivee(Time.valueOf(heureArrivee + ":00"));
            reservation.setDateDepart(Date.valueOf(dateDepart));
            reservation.setNombrePersonnes(Integer.parseInt(nombrePersonnes));
            reservation.setNomClient(nomClient);
            reservation.setEmailClient(emailClient);
            reservation.setTelephoneClient(telephoneClient);

            // Sauvegarde en base
            reservationDAO.save(reservation);

            mv.addItem("success", "Réservation enregistrée avec succès ! (ID: " + reservation.getId() + ")");
            
            // Recharger les hôtels pour le formulaire
            List<Hotel> hotels = hotelDAO.findAll();
            mv.addItem("hotels", hotels);
            
        } catch (Exception e) {
            mv.addItem("error", "Erreur lors de l'enregistrement: " + e.getMessage());
            try {
                mv.addItem("hotels", hotelDAO.findAll());
            } catch (Exception ex) {
                // Ignorer
            }
        }
        return mv;
    }
}
