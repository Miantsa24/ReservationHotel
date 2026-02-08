package models;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class Reservation {
    private int id;
    private int hotelId;
    private Date dateArrivee;
    private Time heureArrivee;
    private Date dateDepart;
    private int nombrePersonnes;
    private String nomClient;
    private String emailClient;
    private String telephoneClient;
    private Timestamp createdAt;
    
    // Pour affichage : nom de l'hôtel
    private String hotelNom;

    // Constructeurs
    public Reservation() {}

    public Reservation(int hotelId, Date dateArrivee, Time heureArrivee, Date dateDepart, 
                       int nombrePersonnes, String nomClient, String emailClient, String telephoneClient) {
        this.hotelId = hotelId;
        this.dateArrivee = dateArrivee;
        this.heureArrivee = heureArrivee;
        this.dateDepart = dateDepart;
        this.nombrePersonnes = nombrePersonnes;
        this.nomClient = nomClient;
        this.emailClient = emailClient;
        this.telephoneClient = telephoneClient;
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

    public Date getDateDepart() { return dateDepart; }
    public void setDateDepart(Date dateDepart) { this.dateDepart = dateDepart; }

    public int getNombrePersonnes() { return nombrePersonnes; }
    public void setNombrePersonnes(int nombrePersonnes) { this.nombrePersonnes = nombrePersonnes; }

    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }

    public String getEmailClient() { return emailClient; }
    public void setEmailClient(String emailClient) { this.emailClient = emailClient; }

    public String getTelephoneClient() { return telephoneClient; }
    public void setTelephoneClient(String telephoneClient) { this.telephoneClient = telephoneClient; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getHotelNom() { return hotelNom; }
    public void setHotelNom(String hotelNom) { this.hotelNom = hotelNom; }
}
