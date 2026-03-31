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
    private int priorityOrder = 0;           // Ordre de priorité (0 = normal, >0 = prioritaire)
    private Integer windowOriginId = null;   // ID de la fenêtre d'origine
    private Timestamp firstWindowTime = null; // Timestamp de la première fenêtre

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
    // Sprint 8 : priorisation des non assignés
    // ===============================
    public int getPriorityOrder() { return priorityOrder; }
    public void setPriorityOrder(int priorityOrder) { this.priorityOrder = priorityOrder; }

    public Integer getWindowOriginId() { return windowOriginId; }
    public void setWindowOriginId(Integer windowOriginId) { this.windowOriginId = windowOriginId; }

    public Timestamp getFirstWindowTime() { return firstWindowTime; }
    public void setFirstWindowTime(Timestamp firstWindowTime) { this.firstWindowTime = firstWindowTime; }

    /**
     * Sprint 8 : Vérifie si cette réservation a des passagers non assignés
     * @return true si remaining > 0
     */
    public boolean hasUnassignedPassengers() {
        return getRemaining() > 0;
    }

    /**
     * Sprint 8 : Vérifie si cette réservation est prioritaire (non assigné d'une fenêtre précédente)
     * @return true si priorityOrder > 0
     */
    public boolean isPriority() {
        return priorityOrder > 0;
    }
}