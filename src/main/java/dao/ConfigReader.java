package dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utilitaire pour lire la configuration depuis application.properties.
 * 
 * Le fichier application.properties contient le token actuel qui sera
 * utilisé pour authentifier l'accès à la liste des réservations.
 */
public class ConfigReader {

    private static final String PROPERTIES_FILE = "src/main/resources/application.properties";
    private static final String CLASSPATH_FILE = "/application.properties";

    /**
     * Lit le token actuel depuis application.properties
     * @return le token ou null si non trouvé
     */
    public static String getCurrentToken() {
        Properties props = loadProperties();
        return props.getProperty("api.token", "").trim();
    }

    /**
     * Charge les propriétés depuis le fichier
     */
    private static Properties loadProperties() {
        Properties props = new Properties();
        
        // Essayer d'abord depuis le classpath (en production)
        try (InputStream is = ConfigReader.class.getResourceAsStream(CLASSPATH_FILE)) {
            if (is != null) {
                props.load(is);
                return props;
            }
        } catch (IOException e) {
            // Ignorer et essayer le fichier système
        }
        
        // Essayer depuis le système de fichiers (en développement)
        try (FileInputStream fis = new FileInputStream(PROPERTIES_FILE)) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("Impossible de charger application.properties: " + e.getMessage());
        }
        
        return props;
    }
}
