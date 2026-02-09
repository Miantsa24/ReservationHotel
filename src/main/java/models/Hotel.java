package models;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Hotel {
    private int id;
    private String nom;
    private String adresse;
    private String ville;
    private int etoiles;
    private BigDecimal prixParNuit;
    private Timestamp createdAt;

    // Constructeurs
    public Hotel() {}

    public Hotel(int id, String nom, String adresse, String ville, int etoiles, BigDecimal prixParNuit) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.ville = ville;
        this.etoiles = etoiles;
        this.prixParNuit = prixParNuit;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public int getEtoiles() { return etoiles; }
    public void setEtoiles(int etoiles) { this.etoiles = etoiles; }

    public BigDecimal getPrixParNuit() { return prixParNuit; }
    public void setPrixParNuit(BigDecimal prixParNuit) { this.prixParNuit = prixParNuit; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
