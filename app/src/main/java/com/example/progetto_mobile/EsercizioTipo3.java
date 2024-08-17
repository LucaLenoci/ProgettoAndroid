package com.example.progetto_mobile;

import java.io.Serializable;

public class EsercizioTipo3 implements Serializable {
    private boolean esercizio_corretto;
    private String risposta_corretta;


    public EsercizioTipo3() {
        // Default constructor required for serialization or other purposes
    }

    public EsercizioTipo3(boolean esercizioCorretto, String rispostaCorretta) {
        this.esercizio_corretto = esercizioCorretto;
        this.risposta_corretta = rispostaCorretta;
    }

    // Getters and Setters
    public boolean isEsercizio_corretto() {
        return esercizio_corretto;
    }

    public void setEsercizio_corretto(boolean esercizio_corretto) {
        this.esercizio_corretto = esercizio_corretto;
    }

    public String getRisposta_corretta() {
        return risposta_corretta;
    }


    public void setRisposta_corretta(String risposta_corretta) {
        this.risposta_corretta = risposta_corretta;
    }

}
