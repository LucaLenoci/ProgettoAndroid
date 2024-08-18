package com.example.progetto_mobile;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class EsercizioTipo1 implements Serializable {
    private boolean esercizio_corretto;
    private String risposta;
    private String risposta_corretta;
    private String suggerimento;

    public EsercizioTipo1() {
        // Default constructor required for serialization or other purposes
    }

    public EsercizioTipo1(boolean esercizioCorretto, String risposta, String rispostaCorretta, String suggerimento) {
        this.esercizio_corretto = esercizioCorretto;
        this.risposta = risposta;
        this.risposta_corretta = rispostaCorretta;
        this.suggerimento = suggerimento;
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
