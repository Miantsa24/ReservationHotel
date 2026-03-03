package models;

public class Distance {
    private int id;
    private String from;
    private String to;
    private double km;

    // Constructeurs
    public Distance() {}

    public Distance(int id, String from, String to, double km) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.km = km;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public double getKm() { return km; }
    public void setKm(double km) { this.km = km; }
}
