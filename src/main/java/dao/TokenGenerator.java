package dao;

import models.Token;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Utilitaire pour générer et insérer un token en base de données.
 * 
 * Ce main génère UN SEUL token valide et met à jour application.properties.
 * Pour tester le refus d'accès (token expiré), modifiez manuellement
 * la valeur de api.token dans application.properties.
 * 
 * Usage: mvn compile && mvn exec:java -Dexec.mainClass=dao.TokenGenerator
 */
public class TokenGenerator {

    private static final String PROPERTIES_FILE = "src/main/resources/application.properties";

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("   GÉNÉRATEUR DE TOKEN - Hôtel API");
        System.out.println("===========================================\n");

        TokenDAO tokenDAO = new TokenDAO();

        try {
            // ========== GÉNÉRER UN TOKEN VALIDE ==========
            String tokenValue = "TOKEN-" + UUID.randomUUID().toString().substring(0, 8);
            // Valide pour les prochaines 24 heures
            Timestamp expiration = new Timestamp(System.currentTimeMillis() + 24L * 60 * 60 * 1000);
            
            Token token = tokenDAO.insertToken(tokenValue, expiration);
            
            System.out.println("TOKEN GÉNÉRÉ:");
            System.out.println("   Valeur: " + token.getToken());
            System.out.println("   Expiration: " + token.getHeureExpiration());
            System.out.println("   Statut: VALIDE (expire dans 24h)");
            System.out.println();

            // ========== MISE À JOUR DU FICHIER application.properties ==========
            updatePropertiesFile(token);

            // ========== INSTRUCTIONS ==========
            System.out.println("===========================================");
            System.out.println("   UTILISATION");
            System.out.println("===========================================\n");
            
            System.out.println("# Accéder à la liste des réservations:");
            System.out.println("  http://localhost:8080/hotel-app-1.0-SNAPSHOT/reservations");
            System.out.println();
            
            System.out.println("# Pour tester le refus (token invalide):");
            System.out.println("  1. Ouvrir src/main/resources/application.properties");
            System.out.println("  2. Modifier api.token=TOKEN-INVALIDE");
            System.out.println("  3. Redéployer: mvn package");
            System.out.println("  4. Redémarrer Tomcat");
            System.out.println();

            System.out.println(">>> Token généré et application.properties mis à jour ! <<<");

        } catch (Exception e) {
            System.err.println("ERREUR lors de la génération du token:");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Met à jour le fichier application.properties avec le nouveau token
     */
    private static void updatePropertiesFile(Token token) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = sdf.format(new Date());

        try (PrintWriter writer = new PrintWriter(new FileWriter(PROPERTIES_FILE))) {
            writer.println("# ===========================================");
            writer.println("# Configuration du Token API - Hotel App");
            writer.println("# ===========================================");
            writer.println("# Derniere mise a jour: " + now);
            writer.println("#");
            writer.println("# Ce token est verifie a chaque acces a /reservations");
            writer.println("# Pour tester le refus, remplacez par un token invalide");
            writer.println("# ===========================================");
            writer.println();
            writer.println("# Token actuel (modifiez cette valeur pour tester)");
            writer.println("api.token=" + token.getToken());
            writer.println();
            writer.println("# Informations du token genere");
            writer.println("api.token.expiration=" + token.getHeureExpiration());
            writer.println("api.token.generated=" + now);
            writer.println();
            writer.println("# ===========================================");
            writer.println("# TESTS");
            writer.println("# ===========================================");
            writer.println("# URL: http://localhost:8080/hotel-app-1.0-SNAPSHOT/reservations");
            writer.println("#");
            writer.println("# Pour tester le REFUS:");
            writer.println("#   Remplacez api.token par: TOKEN-INVALIDE");
            writer.println("#   Puis: mvn package et redemarrer Tomcat");

            System.out.println("===========================================");
            System.out.println("   application.properties MIS À JOUR");
            System.out.println("===========================================");
            System.out.println("Fichier: " + PROPERTIES_FILE);
            System.out.println("Token: " + token.getToken());
            System.out.println();

        } catch (IOException e) {
            System.err.println("ATTENTION: Impossible de mettre à jour application.properties");
            System.err.println("Erreur: " + e.getMessage());
            System.out.println();
            System.out.println("Ajoutez manuellement dans application.properties:");
            System.out.println("api.token=" + token.getToken());
        }
    }
}
