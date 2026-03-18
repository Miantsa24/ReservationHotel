package models;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO pour afficher la traçabilité d'un véhicule à une date donnée.
 * Regroupe toutes les informations nécessaires pour la Page 2.
 */
public class VehiculeTracabilite {

    private Vehicule vehicule;
    private List<Reservation> reservations;
    private List<String> hotels;
    private Time heureDepart;
    private Time heureRetour;
    private double distanceTotale;
    private String parcours;
    private java.util.List<java.sql.Time> etapeHeures;

    public VehiculeTracabilite() {
        this.reservations = new ArrayList<>();
        this.hotels = new ArrayList<>();
        this.etapeHeures = new ArrayList<>();
    }

    // Getters et Setters
    public Vehicule getVehicule() { return vehicule; }
    public void setVehicule(Vehicule vehicule) { this.vehicule = vehicule; }

    public List<Reservation> getReservations() { return reservations; }
    public void setReservations(List<Reservation> reservations) { this.reservations = reservations; }

    public List<String> getHotels() { return hotels; }
    public void setHotels(List<String> hotels) { this.hotels = hotels; }

    public Time getHeureDepart() { return heureDepart; }
    public void setHeureDepart(Time heureDepart) { this.heureDepart = heureDepart; }

    public Time getHeureRetour() { return heureRetour; }
    public void setHeureRetour(Time heureRetour) { this.heureRetour = heureRetour; }

    public double getDistanceTotale() { return distanceTotale; }
    public void setDistanceTotale(double distanceTotale) { this.distanceTotale = distanceTotale; }

    public String getParcours() { return parcours; }
    public void setParcours(String parcours) { this.parcours = parcours; }

    public java.util.List<java.sql.Time> getEtapeHeures() { return etapeHeures; }
    public void setEtapeHeures(java.util.List<java.sql.Time> etapeHeures) { this.etapeHeures = etapeHeures; }
}
