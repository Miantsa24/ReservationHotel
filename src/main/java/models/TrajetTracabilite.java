package models;

import java.sql.Time;
import java.util.List;

public class TrajetTracabilite {

    private VehiculeTrajet trajet;
    private List<Reservation> reservations;
    private List<String> hotels;
    private List<Time> etapeHeures;

    public VehiculeTrajet getTrajet() { return trajet; }
    public void setTrajet(VehiculeTrajet trajet) { this.trajet = trajet; }

    public List<Reservation> getReservations() { return reservations; }
    public void setReservations(List<Reservation> reservations) { this.reservations = reservations; }

    public List<String> getHotels() { return hotels; }
    public void setHotels(List<String> hotels) { this.hotels = hotels; }

    public List<Time> getEtapeHeures() { return etapeHeures; }
    public void setEtapeHeures(List<Time> etapeHeures) { this.etapeHeures = etapeHeures; }
}