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
    private static final int DEFAULT_EXPIRATION_HOURS = 24;

    /**
     * Génère un token aléatoire et l'insère en base avec expiration de 24 heures
     * Retourne le token en JSON : {"token": "...", "expires_in_hours": 24}
     * Endpoint: GET /tokens/create
     */
    @GetMapping("/tokens/create")
    @Json
    public Map<String, Object> generateToken() {
        Map<String, Object> response = new HashMap<>();
        try {
            Token token = tokenDAO.generateAndInsertToken(DEFAULT_EXPIRATION_HOURS);
            if (token != null) {
                response.put("token", token.getToken());
                response.put("expires_in_hours", DEFAULT_EXPIRATION_HOURS);
            } else {
                response.put("error", "Erreur lors de la génération du token");
            }
        } catch (Exception e) {
            response.put("error", "Erreur: " + e.getMessage());
        }
        return response;
    }
}