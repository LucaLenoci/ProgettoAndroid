package com.example.progetto_mobile;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

public class Logopedista extends User implements Serializable {

    private List<String> genitoriRef;
    private List<String> bambiniRef;

    public Logopedista() {
        // Default constructor required for Firestore
    }

    public Logopedista(String nome, String cognome, int eta, String email, int tipologia,
                       List<String> genitoriRef, List<String> bambiniRef) {
        super(nome, cognome, eta, email, tipologia);
        this.genitoriRef = genitoriRef;
        this.bambiniRef = bambiniRef;
    }

    public List<String> getGenitoriRef() {
        return genitoriRef;
    }

    public void setGenitoriRef(List<String> genitoriRef) {
        this.genitoriRef = genitoriRef;
    }

    public List<String> getBambiniRef() {
        return bambiniRef;
    }

    public void setBambiniRef(List<String> bambiniRef) {
        this.bambiniRef = bambiniRef;
    }

    @NonNull
    @Override
    @SuppressLint("DefaultLocale")
    public String toString() {
        return String.format(
                "Logopedista {" +
                        "\tNome: <%s>\n" +
                        "\tCognome: <%s>\n" +
                        "\tEta': <%d>\n" +
                        "\tEmail: <%s>\n" +
                        "\tTipologia: <%d>\n" +
                        "\tGenitori: <%s> (%d)\n" +
                        "\tBambini: <%s> (%d)\n}",
                getNome(), getCognome(), getEta(), getEmail(), getTipologia(),
                getGenitoriRef(), genitoriRef.size(),
                getBambiniRef(), bambiniRef.size());
    }
}
