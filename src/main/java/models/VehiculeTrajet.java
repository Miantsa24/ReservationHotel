package models;

import java.sql.Date;
import java.sql.Timestamp;

public class VehiculeTrajet {
    private int id;
    private int vehiculeId;
    private Date date;
    private Timestamp heureDepart;
    private Timestamp heureArrivee;
    private String listeReservation; // JSON text
    private double kilometrageParcouru;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public VehiculeTrajet() {}

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(int vehiculeId) { this.vehiculeId = vehiculeId; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public Timestamp getHeureDepart() { return heureDepart; }
    public void setHeureDepart(Timestamp heureDepart) { this.heureDepart = heureDepart; }

    public Timestamp getHeureArrivee() { return heureArrivee; }
    public void setHeureArrivee(Timestamp heureArrivee) { this.heureArrivee = heureArrivee; }

    public String getListeReservation() { return listeReservation; }
    public void setListeReservation(String listeReservation) { this.listeReservation = listeReservation; }

    public double getKilometrageParcouru() { return kilometrageParcouru; }
    public void setKilometrageParcouru(double kilometrageParcouru) { this.kilometrageParcouru = kilometrageParcouru; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
