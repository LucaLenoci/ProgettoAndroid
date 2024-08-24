package com.example.progetto_mobile;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

public class Genitore extends User implements Serializable {

    private List<String> bambiniRef;

    public Genitore() {}

    public Genitore(String nome, String cognome, int eta, String email, int tipologia, List<String> bambiniRef) {
        super(nome, cognome, eta, email, tipologia);
        this.bambiniRef = bambiniRef;
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
                "Genitore {\n" +
                        "\tNome: <%s>\n" +
                        "\tCognome: <%s>\n" +
                        "\tEta': <%d>\n" +
                        "\tEmail: <%s>\n" +
                        "\tTipologia: <%d>\n" +
                        "\tBambini: <%s> (%d)\n}",
                getNome(), getCognome(), getEta(), getEmail(), getTipologia(),
                getBambiniRef(), bambiniRef.size());
    }

}
