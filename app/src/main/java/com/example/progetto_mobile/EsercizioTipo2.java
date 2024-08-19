package com.example.progetto_mobile;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

public class EsercizioTipo2 implements Serializable {
    private boolean esercizio_corretto;
    // TODO: su firestore il campo 'risposta' non c'è
    private String risposta;
    private String risposta_corretta;
    private String risposta_sbagliata;
    private int immagine_corretta;


    public EsercizioTipo2() {
        // Default constructor required for serialization or other purposes
    }

    public EsercizioTipo2(boolean esercizioCorretto, String risposta, String rispostaCorretta, String rispostaSbagliata, String suggerimento, int immagineCorretta) {
        this.esercizio_corretto = esercizioCorretto;
        this.risposta = risposta;
        this.risposta_corretta = rispostaCorretta;
        this.risposta_sbagliata = rispostaSbagliata;
        this.immagine_corretta = immagineCorretta;

    }

    // Getters and Setters
    public boolean isEsercizio_corretto() {
        return esercizio_corretto;
    }

    public void setEsercizio_corretto(boolean esercizio_corretto) {
        this.esercizio_corretto = esercizio_corretto;
    }

    public String getRisposta() {
        return risposta;
    }

    public void setRisposta(String risposta) {
        this.risposta = risposta;
    }

    public String getRisposta_corretta() {
        return risposta_corretta;
    }

    public String getRisposta_sbagliata() {
        return risposta_sbagliata;
    }

    public int getImmagine_corretta() {
        return immagine_corretta;
    }


    public void setRisposta_corretta(String risposta_corretta) {
        this.risposta_corretta = risposta_corretta;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.ITALY,
                "Esercizio corretto: %b,\n" +
                        "Risposta: %s\n" +
                        "Risposta corretta: %s\n" +
                        "Risposta sbagliata: %s\n" +
                        "Immagine corretta: %d",
                isEsercizio_corretto(), getRisposta(), getRisposta_corretta(), getRisposta_sbagliata(), getImmagine_corretta());
    }
}
