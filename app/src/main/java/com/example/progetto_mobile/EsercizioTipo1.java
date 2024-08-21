package com.example.progetto_mobile;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class EsercizioTipo1 implements Serializable {
    private boolean esercizio_corretto;
    private String risposta;
    private String risposta_corretta;
    private String suggerimento;
    private String suggerimento2;
    private String suggerimento3;

    public EsercizioTipo1() {
        // Default constructor required for serialization or other purposes
    }

    public EsercizioTipo1(boolean esercizioCorretto, String risposta, String rispostaCorretta, String suggerimento, String suggerimento2, String suggerimento3) {
        this.esercizio_corretto = esercizioCorretto;
        this.risposta = risposta;
        this.risposta_corretta = rispostaCorretta;
        this.suggerimento = suggerimento;
        this.suggerimento2 = suggerimento2;
        this.suggerimento3 = suggerimento3;
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


    public void setSuggerimento(String suggerimento) {
        this.suggerimento = suggerimento;
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
