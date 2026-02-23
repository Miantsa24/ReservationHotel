package models;

import java.math.BigDecimal;

public class Vehicule {
    private int id;
    private String marque;
    private int capacite;
    private String typeCarburant;
    private BigDecimal vitesseMoyenne;
    private int tempsAttente;

    // Constructeurs
    public Vehicule() {}

    public Vehicule(int id, String marque, int capacite, String typeCarburant, BigDecimal vitesseMoyenne, int tempsAttente) {
        this.id = id;
        this.marque = marque;
        this.capacite = capacite;
        this.typeCarburant = typeCarburant;
        this.vitesseMoyenne = vitesseMoyenne;
        this.tempsAttente = tempsAttente;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public String getTypeCarburant() { return typeCarburant; }
    public void setTypeCarburant(String typeCarburant) { this.typeCarburant = typeCarburant; }

    public BigDecimal getVitesseMoyenne() { return vitesseMoyenne; }
    public void setVitesseMoyenne(BigDecimal vitesseMoyenne) { this.vitesseMoyenne = vitesseMoyenne; }

    public int getTempsAttente() { return tempsAttente; }
    public void setTempsAttente(int tempsAttente) { this.tempsAttente = tempsAttente; }
}