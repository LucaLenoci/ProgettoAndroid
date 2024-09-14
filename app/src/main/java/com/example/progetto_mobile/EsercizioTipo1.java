package com.example.progetto_mobile;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

public class EsercizioTipo1 implements Serializable {

    private String placeholder;
    private boolean esercizio_corretto;
    private String risposta;
    private String risposta_corretta;
    private List<Boolean> suggerimenti;
    private String suggerimento;
    private String suggerimento2;
    private String suggerimento3;
    private int suggerimentiUsati;
    private int tentativi;
    private String audio_url;
    private String tipo;

    public EsercizioTipo1() {
        // Default constructor required for serialization or other purposes
    }

    public EsercizioTipo1(String placeholder, boolean esercizioCorretto, String risposta, String rispostaCorretta,
                          List<Boolean> suggerimenti, String suggerimento, String suggerimento2, String suggerimento3,
                          int suggerimentiUsati, int tentativi, String audio_url, String tipo) {
        this.placeholder = placeholder;
        this.esercizio_corretto = esercizioCorretto;
        this.risposta = risposta;
        this.risposta_corretta = rispostaCorretta;
        this.suggerimenti = suggerimenti;
        this.suggerimento = suggerimento;
        this.suggerimento2 = suggerimento2;
        this.suggerimento3 = suggerimento3;
        this.suggerimentiUsati = suggerimentiUsati;
        this.tentativi = tentativi;
        this.audio_url = audio_url;
        this.tipo = tipo;
    }

    public EsercizioTipo1(String placeholder, boolean esercizioCorretto, String risposta, String rispostaCorretta,
                          String suggerimento, String suggerimento2, String suggerimento3, int suggerimentiUsati) {
        this.placeholder = placeholder;
        this.esercizio_corretto = esercizioCorretto;
        this.risposta = risposta;
        this.risposta_corretta = rispostaCorretta;
        this.suggerimento = suggerimento;
        this.suggerimento2 = suggerimento2;
        this.suggerimento3 = suggerimento3;
        this.suggerimentiUsati = suggerimentiUsati;
    }

    public EsercizioTipo1(boolean esercizioCorretto, String risposta, String rispostaCorretta,
                          String suggerimento, String suggerimento2, String suggerimento3) {
        this.esercizio_corretto = esercizioCorretto;
        this.risposta = risposta;
        this.risposta_corretta = rispostaCorretta;
        this.suggerimento = suggerimento;
        this.suggerimento2 = suggerimento2;
        this.suggerimento3 = suggerimento3;
    }

    public EsercizioTipo1(boolean esercizioCorretto, String risposta, String rispostaCorretta,
                          String suggerimento, String suggerimento2, String suggerimento3, int suggerimentiUsati) {
        this.esercizio_corretto = esercizioCorretto;
        this.risposta = risposta;
        this.risposta_corretta = rispostaCorretta;
        this.suggerimento = suggerimento;
        this.suggerimento2 = suggerimento2;
        this.suggerimento3 = suggerimento3;
        this.suggerimentiUsati = suggerimentiUsati;
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

    public void setRisposta_corretta(String risposta_corretta) {
        this.risposta_corretta = risposta_corretta;
    }

    public String getSuggerimento() {
        return suggerimento;
    }
    public String getSuggerimento2() {
        return suggerimento2;
    }
    public String getSuggerimento3() {
        return suggerimento3;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public void setSuggerimento(String suggerimento) {
        this.suggerimento = suggerimento;
    }

    public int getSuggerimentiUsati() {
        return suggerimentiUsati;
    }

    public void setSuggerimentiUsati(int suggerimentiUsati) {
        this.suggerimentiUsati = suggerimentiUsati;
    }

    public String getAudio_url() {
        return audio_url;
    }

    public void setAudio_url(String audio_url) {
        this.audio_url = audio_url;
    }

    public List<Boolean> getSuggerimenti() {
        return suggerimenti;
    }

    public void setSuggerimenti(List<Boolean> suggerimenti) {
        this.suggerimenti = suggerimenti;
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
                        "Risposta: %s,\n" +
                        "Risposta corretta: %s,\n" +
                        "Suggerimento: %s",
                isEsercizio_corretto(), getRisposta(), getRisposta_corretta(), getSuggerimento());
    }
}
