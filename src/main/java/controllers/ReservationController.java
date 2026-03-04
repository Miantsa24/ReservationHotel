package controllers;

import src.Controller;
import src.GetMapping;
import src.PostMapping;
import src.ModelView;
import src.RequestParam;

import dao.HotelDAO;
import dao.ReservationDAO;
import dao.TokenDAO;
import dao.ConfigReader;
import models.Hotel;
import models.Reservation;
import models.Vehicule;
import service.VehiculeSelectionService;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

@Controller
public class ReservationController {

    private HotelDAO hotelDAO = new HotelDAO();
    private ReservationDAO reservationDAO = new ReservationDAO();
    private TokenDAO tokenDAO = new TokenDAO();
    private VehiculeSelectionService vehiculeSelectionService = new VehiculeSelectionService();

    // ==================== FRONT-OFFICE ====================

    /**
     * Affiche la liste de toutes les réservations (Front-Office)
     * Le token est lu depuis application.properties (pas besoin de le passer dans l'URL)
     * 
     * Usage: /reservations
     */
    @GetMapping("/reservations")
    public ModelView listReservations() {
        // Lire le token depuis application.properties
        String token = ConfigReader.getCurrentToken();
        
        // Vérification du token
        if (!isTokenValid(token)) {
            return createErrorView(token);
        }
        
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
     * Le token est lu depuis application.properties
     */
    @GetMapping("/reservations/filter")
    public ModelView filterReservations(@RequestParam("dateArrivee") String dateArrivee) {
        // Lire le token depuis application.properties
        String token = ConfigReader.getCurrentToken();
        
        // Vérification du token
        if (!isTokenValid(token)) {
            return createErrorView(token);
        }
        
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
     * Vérifie si le token est valide (existe et non expiré)
     */
    private boolean isTokenValid(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        try {
            return tokenDAO.isValidToken(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Crée une vue d'erreur pour accès non autorisé
     */
    private ModelView createErrorView(String token) {
        ModelView mv = new ModelView("/WEB-INF/views/auth-error.jsp");
        if (token == null || token.trim().isEmpty()) {
            mv.addItem("errorTitle", "Token non configuré");
            mv.addItem("errorMessage", "Aucun token n'est configuré dans application.properties.");
            mv.addItem("errorDetail", "Exécutez TokenGenerator pour générer un token, ou ajoutez manuellement api.token=VOTRE_TOKEN dans application.properties");
        } else {
            mv.addItem("errorTitle", "Token invalide ou expiré");
            mv.addItem("errorMessage", "Le token configuré dans application.properties n'est pas valide ou a expiré.");
            mv.addItem("errorDetail", "Exécutez TokenGenerator pour générer un nouveau token valide, ou modifiez api.token dans application.properties");
        }
        mv.addItem("providedToken", token);
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
            @RequestParam("nombrePersonnes") String nombrePersonnes, 
            @RequestParam("refClient") String refClient) {
        
        ModelView mv = new ModelView("/WEB-INF/views/reservation-form.jsp");
        
        try {
            // Création de la réservation
            Reservation reservation = new Reservation();
            reservation.setHotelId(Integer.parseInt(hotelId));
            reservation.setDateArrivee(Date.valueOf(dateArrivee));
            reservation.setHeureArrivee(Time.valueOf(heureArrivee + ":00"));
            reservation.setNombrePersonnes(Integer.parseInt(nombrePersonnes));
            reservation.setRefClient(refClient);

            // Sauvegarde en base
            reservationDAO.save(reservation);

            // Assignation automatique d'un véhicule
            Vehicule vehiculeAssigne = vehiculeSelectionService.assignerVehicule(
                reservation.getId(), reservation.getNombrePersonnes());

            String successMsg = "Réservation enregistrée avec succès ! (ID: " + reservation.getId() + ")";
            if (vehiculeAssigne != null) {
                successMsg += " — Véhicule assigné : " + vehiculeAssigne.getMarque() 
                    + " (capacité: " + vehiculeAssigne.getCapacite() 
                    + ", carburant: " + vehiculeAssigne.getTypeCarburant() + ")";
            } else {
                successMsg += " — Aucun véhicule disponible pour " + reservation.getNombrePersonnes() + " personnes.";
            }
            mv.addItem("success", successMsg);
            
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
