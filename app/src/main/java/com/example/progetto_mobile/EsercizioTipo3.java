package com.example.progetto_mobile;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class EsercizioTipo3 implements Serializable {

    private String placeholder;
    private boolean esercizio_corretto;
    private String risposta_corretta;
    private String audio_url;
    private int tentativi;
    private String tipo;

    public EsercizioTipo3() {
        // Default constructor required for serialization or other purposes
    }

    public EsercizioTipo3(String placeholder, boolean esercizioCorretto, String rispostaCorretta,
                          String audio_url, int tentativi, String tipo) {
        this.placeholder = placeholder;
        this.esercizio_corretto = esercizioCorretto;
        this.risposta_corretta = rispostaCorretta;
        this.audio_url = audio_url;
        this.tentativi = tentativi;
        this.tipo = tipo;
    }

    public EsercizioTipo3(String placeholder, boolean esercizioCorretto, String rispostaCorretta) {
        this.placeholder = placeholder;
        this.esercizio_corretto = esercizioCorretto;
        this.risposta_corretta = rispostaCorretta;
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

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getAudio_url() {
        return audio_url;
    }

    public void setAudio_url(String audio_url) {
        this.audio_url = audio_url;
    }

    public int getTentativi() {
        return tentativi;
    }

    public void setTentativi(int tentativi) {
        this.tentativi = tentativi;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(
                "Esercizio corretto: %b,\n" +
                        "Risposta corretta: %s",
                isEsercizio_corretto(), getRisposta_corretta());
    }
}
