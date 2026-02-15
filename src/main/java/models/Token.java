package models;

import java.sql.Timestamp;

public class Token {
    private int id;
    private String token;
    private Timestamp heureExpiration;

    // Constructeurs
    public Token() {}

    public Token(int id, String token, Timestamp heureExpiration) {
        this.id = id;
        this.token = token;
        this.heureExpiration = heureExpiration;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Timestamp getHeureExpiration() {
        return heureExpiration;
    }

    public void setHeureExpiration(Timestamp heureExpiration) {
        this.heureExpiration = heureExpiration;
    }
}