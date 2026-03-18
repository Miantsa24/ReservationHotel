package models;

public class ReservationVehicule {
    private int id;
    private int idReservation;
    private int idVehicule;
    private Integer vehiculeTrajetId; // Nullable FK to vehicule_trajet

    // Constructeurs
    public ReservationVehicule() {}

    public ReservationVehicule(int idReservation, int idVehicule) {
        this.idReservation = idReservation;
        this.idVehicule = idVehicule;
    }

    public Integer getVehiculeTrajetId() { return vehiculeTrajetId; }
    public void setVehiculeTrajetId(Integer vehiculeTrajetId) { this.vehiculeTrajetId = vehiculeTrajetId; }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdReservation() { return idReservation; }
    public void setIdReservation(int idReservation) { this.idReservation = idReservation; }

    public int getIdVehicule() { return idVehicule; }
    public void setIdVehicule(int idVehicule) { this.idVehicule = idVehicule; }
}
