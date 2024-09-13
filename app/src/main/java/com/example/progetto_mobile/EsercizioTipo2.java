package com.example.progetto_mobile;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

public class EsercizioTipo2 implements Serializable {

    private String placeholder;
    private boolean esercizio_corretto;
    private String risposta_corretta;
    private String risposta_sbagliata;
    private int immagine_corretta;

    public EsercizioTipo2() {
        // Default constructor required for serialization or other purposes
    }

    public EsercizioTipo2(String placeholder, boolean esercizioCorretto, String rispostaCorretta, String rispostaSbagliata, int immagineCorretta) {
        this.placeholder = placeholder;
        this.esercizio_corretto = esercizioCorretto;
        this.risposta_corretta = rispostaCorretta;
        this.risposta_sbagliata = rispostaSbagliata;
        this.immagine_corretta = immagineCorretta;
    }

    public EsercizioTipo2(boolean esercizioCorretto, String rispostaCorretta, String rispostaSbagliata, int immagineCorretta) {
        this.esercizio_corretto = esercizioCorretto;
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

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.ITALY,
                "Esercizio corretto: %b,\n" +
                        "Risposta corretta: %s\n" +
                        "Risposta sbagliata: %s\n" +
                        "Immagine corretta: %d",
                isEsercizio_corretto(), getRisposta_corretta(), getRisposta_sbagliata(), getImmagine_corretta());
    }
}
