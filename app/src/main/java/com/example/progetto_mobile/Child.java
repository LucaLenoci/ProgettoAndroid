package com.example.progetto_mobile;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

public class Child extends User implements Serializable {
    private String avatarCorrente;
    private List<String> ownedAvatars;
    private int progresso;
    private int coins;
    private EsercizioTipo1 eserciziTipo1;
    private EsercizioTipo2 eserciziTipo2;
    private EsercizioTipo3 eserciziTipo3;
    private String esercizioTipo1Ref;
    private String esercizioTipo2Ref;
    private String esercizioTipo3Ref;
    private String docId;
    private String logopedistaRef;
    private String sesso;
    private String tema;

    public Child() {}

    public Child(String nome, int coins) {
        super(nome);
        this.coins = coins;
    }

    public Child(String nome, String cognome, int coins) {
        super(nome, cognome);
        this.coins = coins;
    }

    public Child(String id, String nome, String cognome, int coins) {
        super(nome, cognome);
        this.coins = coins;
        this.docId = id;
    }

    public Child(String nome, int progresso, int coins, EsercizioTipo1 eserciziTipo1,
                 EsercizioTipo2 eserciziTipo2, EsercizioTipo3 eserciziTipo3) {
        super(nome);
        this.progresso = progresso;
        this.coins = coins;
        this.eserciziTipo1 = eserciziTipo1;
        this.eserciziTipo2 = eserciziTipo2;
        this.eserciziTipo3 = eserciziTipo3;
    }

    public Child(String nome, String cognome, int eta, int tipologia,
                 String avatarCorrente, int progresso, int coins) {
        super(nome, cognome, eta, tipologia);
        this.avatarCorrente = avatarCorrente;
        this.progresso = progresso;
        this.coins = coins;
    }

    public Child(String nome, String cognome, int eta, int tipologia,
                 String avatarCorrente, int progresso, int coins, EsercizioTipo1 eserciziTipo1,
                 EsercizioTipo2 eserciziTipo2, EsercizioTipo3 eserciziTipo3) {
        super(nome, cognome, eta, tipologia);
        this.avatarCorrente = avatarCorrente;
        this.progresso = progresso;
        this.coins = coins;
        this.eserciziTipo1 = eserciziTipo1;
        this.eserciziTipo2 = eserciziTipo2;
        this.eserciziTipo3 = eserciziTipo3;
    }

    public String getAvatarCorrente() {
        return avatarCorrente;
    }

    public void setAvatarCorrente(String avatarCorrente) {
        this.avatarCorrente = avatarCorrente;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getProgresso() {
        return progresso;
    }

    public void setProgresso(int progresso) {
        this.progresso = progresso;
    }

    public EsercizioTipo1 getEserciziTipo1() {
        return eserciziTipo1;
    }

    public void setEserciziTipo1(EsercizioTipo1 eserciziTipo1) {
        this.eserciziTipo1 = eserciziTipo1;
    }

    public EsercizioTipo2 getEserciziTipo2() {
        return eserciziTipo2;
    }

    public void setEserciziTipo2(EsercizioTipo2 eserciziTipo2) {
        this.eserciziTipo2 = eserciziTipo2;
    }

    public EsercizioTipo3 getEserciziTipo3() {
        return eserciziTipo3;
    }

    public void setEserciziTipo3(EsercizioTipo3 eserciziTipo3) {
        this.eserciziTipo3 = eserciziTipo3;
    }

    public List<?> getAllEsercizi() {
        return List.of(eserciziTipo1, eserciziTipo2, eserciziTipo3);
    }


    public String getDocId() {
        return docId;
    }

    public Child putDocId(String docId) {
        this.docId = docId;
        return this;
    }

    public String getEsercizioTipo1Ref() {
        return esercizioTipo1Ref;
    }

    public Child putEsercizioTipo1Ref(String esercizioTipo1Ref) {
        this.esercizioTipo1Ref = esercizioTipo1Ref;
        return this;
    }

    public String getEsercizioTipo2Ref() {
        return esercizioTipo2Ref;
    }

    public Child putEsercizioTipo2Ref(String esercizioTipo2Ref) {
        this.esercizioTipo2Ref = esercizioTipo2Ref;
        return this;
    }

    public String getEsercizioTipo3Ref() {
        return esercizioTipo3Ref;
    }

    public Child putEsercizioTipo3Ref(String esercizioTipo3Ref) {
        this.esercizioTipo3Ref = esercizioTipo3Ref;
        return this;
    }

    public List<String> getOwnedAvatars() {
        return ownedAvatars;
    }

    public void setOwnedAvatars(List<String> ownedAvatars) {
        this.ownedAvatars = ownedAvatars;
    }

    public String getLogopedistaRef() {
        return logopedistaRef;
    }

    public Child putLogopedistaRef(String logopedistaRef) {
        this.logopedistaRef = logopedistaRef;
        return this;
    }

    public String getSesso() {
        return sesso;
    }

    public void setSesso(String sesso) {
        this.sesso = sesso;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public void setEmail(String email) {
        super.setEmail(null);
    }

    @NonNull
    @Override
    @SuppressLint("DefaultLocale")
    public String toString() {
        return String.format(
                "Bambino {\n" +
                        "\tNome: <%s>\n" +
                        "\tCognome: <%s>\n" +
                        "\tEta': <%d>\n" +
                        "\tTipologia: <%d>\n" +
                        "\tAvatar Corrente: <%s>\n" +
                        "\tProgresso: <%d>\n" +
                        "\tCoins: <%d>\n}",
                getNome(), getCognome(), getEta(), getTipologia(),
                getAvatarCorrente(), getProgresso(), getCoins());
    }

}