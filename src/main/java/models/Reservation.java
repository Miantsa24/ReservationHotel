package models;

import java.sql.Date;
import java.sql.Time;

public class Reservation {
    private int id;
    private int hotelId;
    private Date dateArrivee;
    private Time heureArrivee;
    private int nombrePersonnes;
    private String refClient;
    
    // Pour affichage : nom de l'hôtel
    private String hotelNom;

    // Constructeurs
    public Reservation() {}

    public Reservation(int hotelId, Date dateArrivee, Time heureArrivee, 
                       int nombrePersonnes, String refClient) {
        this.hotelId = hotelId;
        this.dateArrivee = dateArrivee;
        this.heureArrivee = heureArrivee;
        this.nombrePersonnes = nombrePersonnes;
        this.refClient = refClient;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHotelId() { return hotelId; }
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    public Date getDateArrivee() { return dateArrivee; }
    public void setDateArrivee(Date dateArrivee) { this.dateArrivee = dateArrivee; }

    public Time getHeureArrivee() { return heureArrivee; }
    public void setHeureArrivee(Time heureArrivee) { this.heureArrivee = heureArrivee; }

    public int getNombrePersonnes() { return nombrePersonnes; }
    public void setNombrePersonnes(int nombrePersonnes) { this.nombrePersonnes = nombrePersonnes; }

    public String getRefClient() { return refClient; }
    public void setRefClient(String refClient) { this.refClient = refClient; }

    public String getHotelNom() { return hotelNom; }
    public void setHotelNom(String hotelNom) { this.hotelNom = hotelNom; }
}
