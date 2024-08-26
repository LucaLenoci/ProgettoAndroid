package com.example.progetto_mobile;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class User implements Serializable {

    private String nome;
    private String cognome;
    private int eta;
    private String email;
    private int tipologia;

    public User() {
        // Default constructor required for Firestore
    }

    public User(String nome) {
        this.nome = nome;
    }

    public User(String nome, String cognome) {
        this.nome = nome;
        this.cognome = cognome;
    }

    public User(String nome, String cognome, int eta, String email, int tipologia) {
        this.nome = nome;
        this.cognome = cognome;
        this.eta = eta;
        this.email = email;
        this.tipologia = tipologia;
    }

    // Getters and Setters
    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public int getEta() {
        return eta;
    }

    public void setEta(int eta) {
        this.eta = eta;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getTipologia() {
        return tipologia;
    }

    public void setTipologia(int tipologia) {
        this.tipologia = tipologia;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @NonNull
    @Override
    @SuppressLint("DefaultLocale")
    public String toString() {
        return String.format(
                "User {\n" +
                        "\tNome: <%s>\n" +
                        "\tCognome: <%s>\n" +
                        "\tEta': <%d>\n" +
                        "\tEmail: <%s>\n" +
                        "\tTipologia: <%d>\n}",
                getNome(), getCognome(), getEta(), getEmail(), getTipologia());
    }
}
