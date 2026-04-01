// Reservation.java
package models;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class Reservation {
    private int id;
    private int hotelId;
    private Date dateArrivee;
    private Time heureArrivee;
    private int nombrePersonnes;
    private String refClient;
    private String status = "EN_ATTENTE";
    private String hotelNom;

    // Sprint 7 : suivi assignation
    private int assignedCount = 0;

    // Sprint 8 : priorisation des non assignés
    private int priorityOrder = 0;
    private Integer windowOriginId = null;
    private Timestamp firstWindowTime = null;

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
    // Alias for compatibility
    public int getNombrePassager() { return nombrePersonnes; }
    public void setNombrePassager(int nombrePassager) { this.nombrePersonnes = nombrePassager; }

    public String getRefClient() { return refClient; }
    public void setRefClient(String refClient) { this.refClient = refClient; }
    // Alias for display
    public String getNomClient() { return refClient; }
    public void setNomClient(String nomClient) { this.refClient = nomClient; }

    public String getHotelNom() { return hotelNom; }
    public void setHotelNom(String hotelNom) { this.hotelNom = hotelNom; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // ===============================
    // Sprint 7 : méthodes pour allocation
    // ===============================
    public int getAssignedCount() { return assignedCount; }
    public void setAssignedCount(int assignedCount) { this.assignedCount = assignedCount; }

    public int getRemaining() {
        return this.nombrePersonnes - this.assignedCount;
    }

    // ===============================
    // Sprint 8 : méthodes pour priorisation
    // ===============================
    public int getPriorityOrder() { return priorityOrder; }
    public void setPriorityOrder(int priorityOrder) { this.priorityOrder = priorityOrder; }

    public Integer getWindowOriginId() { return windowOriginId; }
    public void setWindowOriginId(Integer windowOriginId) { this.windowOriginId = windowOriginId; }

    public Timestamp getFirstWindowTime() { return firstWindowTime; }
    public void setFirstWindowTime(Timestamp firstWindowTime) { this.firstWindowTime = firstWindowTime; }

    /**
     * Sprint 8 : Indique si cette réservation est prioritaire (non assignés d'une fenêtre précédente).
     */
    public boolean isPriority() {
        return priorityOrder > 0 || firstWindowTime != null;
    }

    /**
     * Sprint 8 : Indique si cette réservation a des passagers non encore assignés.
     */
    public boolean hasUnassignedPassengers() {
        return getRemaining() > 0;
    }
}