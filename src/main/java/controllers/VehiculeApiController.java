package controllers;

import src.Controller;
import src.GetMapping;
import src.Json;
import src.RequestParam;

import dao.TokenDAO;
import dao.VehiculeDAO;
import models.Vehicule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class VehiculeApiController {

    private TokenDAO tokenDAO = new TokenDAO();
    private VehiculeDAO vehiculeDAO = new VehiculeDAO();

    /**
     * Point d'accès JSON pour la liste des véhicules. Requiert un token valide.
     * Si le token n'existe pas ou est expiré, une erreur 401 est renvoyée.
     */
    @GetMapping("/api/vehicules")
    @Json
    public Map<String, Object> getVehicules(@RequestParam("token") String token) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (token == null || token.trim().isEmpty() || !tokenDAO.isValidToken(token)) {
                result.put("success", false);
                result.put("status", 401);
                result.put("message", "Token invalide ou expiré");
                return result;
            }

            List<Vehicule> liste = vehiculeDAO.findAll();
            result.put("success", true);
            result.put("vehicules", liste);
        } catch (Exception e) {
            result.put("success", false);
            result.put("status", 500);
            result.put("message", "Erreur lors de la récupération des véhicules : " + e.getMessage());
        }
        return result;
    }
}
