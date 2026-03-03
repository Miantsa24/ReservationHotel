package controllers;

import src.Controller;
import src.GetMapping;
import src.PostMapping;
import src.Json;
import src.RequestParam;

import dao.TokenDAO;
import dao.ReservationDAO;
import models.Reservation;

import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API REST pour les réservations avec protection par token.
 * 
 * Endpoints disponibles :
 * - GET /reservations?token=<votre-token> : Liste toutes les réservations (JSON)
 * - POST /reservations/create : Crée une nouvelle réservation (token requis dans le formulaire)
 * 
 * Note: Le token doit exister en base et ne pas être expiré (expiration > now)
 */
@Controller
public class ReservationApiController {

    private TokenDAO tokenDAO = new TokenDAO();
    private ReservationDAO reservationDAO = new ReservationDAO();

    /**
     * Liste toutes les réservations en JSON.
     * Requiert un token valide passé en paramètre.
     * 
     * Usage: GET /reservations?token=<votre-token>
     * 
     * @param token Le token d'authentification
     * @return JSON avec les réservations ou erreur si token invalide
     */
    @GetMapping("/api/reservations")
    @Json
    public Map<String, Object> getReservations(@RequestParam("token") String token) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Vérification du token
            if (token == null || token.trim().isEmpty()) {
                result.put("success", false);
                result.put("status", 401);
                result.put("message", "Token manquant. Veuillez fournir un token valide.");
                return result;
            }
            
            if (!tokenDAO.isValidToken(token)) {
                result.put("success", false);
                result.put("status", 401);
                result.put("message", "Token invalide ou expiré");
                return result;
            }

            // Token valide, récupérer les réservations
            List<Reservation> reservations = reservationDAO.findAll();
            result.put("success", true);
            result.put("count", reservations.size());
            result.put("reservations", reservations);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("status", 500);
            result.put("message", "Erreur lors de la récupération des réservations : " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Crée une nouvelle réservation via API.
     * Requiert un token valide dans les champs du formulaire.
     * 
     * Usage: POST /reservations/create
     * Champs requis: token, hotelId, dateArrivee, heureArrivee, nombrePersonnes, refClient
     * 
     * @param token Le token d'authentification
     * @param hotelId ID de l'hôtel
     * @param dateArrivee Date d'arrivée (format YYYY-MM-DD)
     * @param heureArrivee Heure d'arrivée (format HH:MM)
     * @param nombrePersonnes Nombre de personnes
     * @param refClient Référence client
     * @return JSON avec le résultat de la création
     */
    @PostMapping("/api/reservations/create")
    @Json
    public Map<String, Object> createReservation(
            @RequestParam("token") String token,
            @RequestParam("hotelId") String hotelId,
            @RequestParam("dateArrivee") String dateArrivee,
            @RequestParam("heureArrivee") String heureArrivee,
            @RequestParam("nombrePersonnes") String nombrePersonnes,
            @RequestParam("refClient") String refClient) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Vérification du token
            if (token == null || token.trim().isEmpty()) {
                result.put("success", false);
                result.put("status", 401);
                result.put("message", "Token manquant. Veuillez fournir un token valide.");
                return result;
            }
            
            if (!tokenDAO.isValidToken(token)) {
                result.put("success", false);
                result.put("status", 401);
                result.put("message", "Token invalide ou expiré");
                return result;
            }

            // Validation des champs requis
            if (hotelId == null || hotelId.trim().isEmpty()) {
                result.put("success", false);
                result.put("status", 400);
                result.put("message", "L'ID de l'hôtel est requis");
                return result;
            }
            
            if (dateArrivee == null || dateArrivee.trim().isEmpty()) {
                result.put("success", false);
                result.put("status", 400);
                result.put("message", "La date d'arrivée est requise");
                return result;
            }
            
            if (heureArrivee == null || heureArrivee.trim().isEmpty()) {
                result.put("success", false);
                result.put("status", 400);
                result.put("message", "L'heure d'arrivée est requise");
                return result;
            }
            
            if (nombrePersonnes == null || nombrePersonnes.trim().isEmpty()) {
                result.put("success", false);
                result.put("status", 400);
                result.put("message", "Le nombre de personnes est requis");
                return result;
            }

            // Création de la réservation
            Reservation reservation = new Reservation();
            reservation.setHotelId(Integer.parseInt(hotelId));
            reservation.setDateArrivee(Date.valueOf(dateArrivee));
            
            // Gestion du format de l'heure (HH:MM ou HH:MM:SS)
            String heureFormatted = heureArrivee.contains(":") && heureArrivee.split(":").length == 2 
                    ? heureArrivee + ":00" 
                    : heureArrivee;
            reservation.setHeureArrivee(Time.valueOf(heureFormatted));
            
            reservation.setNombrePersonnes(Integer.parseInt(nombrePersonnes));
            reservation.setRefClient(refClient != null ? refClient : "");

            // Sauvegarde
            reservationDAO.save(reservation);

            result.put("success", true);
            result.put("message", "Réservation créée avec succès");
            result.put("reservationId", reservation.getId());
            
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("status", 400);
            result.put("message", "Format numérique invalide pour hotelId ou nombrePersonnes");
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("status", 400);
            result.put("message", "Format de date ou heure invalide. Utilisez YYYY-MM-DD pour la date et HH:MM pour l'heure.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("status", 500);
            result.put("message", "Erreur lors de la création de la réservation : " + e.getMessage());
        }
        
        return result;
    }
}
