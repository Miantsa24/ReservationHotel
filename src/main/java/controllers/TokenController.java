package controllers;

import src.Controller;
import src.GetMapping;
import src.Json;

import dao.TokenDAO;
import models.Token;

import java.util.HashMap;
import java.util.Map;

@Controller
public class TokenController {

    private TokenDAO tokenDAO = new TokenDAO();

    /**
     * Génère un token aléatoire et l'insère en base avec expiration de 2 heures
     * Retourne le token en JSON
     */
    @GetMapping("/generate-token")
    @Json
    public Map<String, Object> generateToken() {
        Map<String, Object> response = new HashMap<>();
        try {
            Token token = tokenDAO.generateAndInsertToken(2); // Expiration 2 heures
            if (token != null) {
                response.put("success", true);
                response.put("token", token.getToken());
                response.put("id", token.getId());
                response.put("expiration", token.getHeureExpiration().toString());
            } else {
                response.put("success", false);
                response.put("message", "Erreur lors de la génération du token");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
        }
        return response;
    }
}