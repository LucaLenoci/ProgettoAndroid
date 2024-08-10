package com.example.progetto_mobile;

import java.io.Serializable;

public class User  implements Serializable {
    private String Cognome;
    private int Eta;
    private String Nome;
    private int Tipologia;
    private String Email;

    public User() {
        // Default constructor required for Firestore
    }

    public User(String Cognome, int Eta, String Nome, int Tipologia, String Email) {
        this.Cognome = Cognome;
        this.Eta = Eta;
        this.Nome = Nome;
        this.Tipologia = Tipologia;
        this.Email = Email;
    }

    // Getters and Setters
    public String getCognome() {
        return Cognome;
    }

    public void setCognome(String cognome) {
        this.Cognome = cognome;
    }

    public int getEta() {
        return Eta;
    }

    public void setEta(int eta) {
        this.Eta = eta;
    }

    public String getNome() {
        return Nome;
    }

    public void setNome(String nome) {
        this.Nome = nome;
    }

    public int getTipologia() {
        return Tipologia;
    }

    public void setTipologia(int tipologia) {
        this.Tipologia = tipologia;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        this.Email = email;
    }
}
