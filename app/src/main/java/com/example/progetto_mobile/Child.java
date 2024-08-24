package com.example.progetto_mobile;

import java.io.Serializable;
import java.util.List;

public class Child implements Serializable {
    private String nome;
    private int progresso;
    int coins;
    private List<EsercizioTipo1> eserciziTipo1;
    private List<EsercizioTipo2> eserciziTipo2;
    private List<EsercizioTipo3> eserciziTipo3;

    public Child() {}

    public Child(String nome, int progresso, List<EsercizioTipo1> eserciziTipo1,
                 List<EsercizioTipo2> eserciziTipo2, List<EsercizioTipo3> eserciziTipo3) {
        this.nome = nome;
        this.progresso = progresso;
        this.eserciziTipo1 = eserciziTipo1;
        this.eserciziTipo2 = eserciziTipo2;
        this.eserciziTipo3 = eserciziTipo3;
    }

    public Child(String name, int i) {
        this.nome = name;
        this.coins = i;
    }

    public int getCoins() {
        return coins;
    }



    // Getters and setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getProgresso() {
        return progresso;
    }

    public void setProgresso(int progresso) {
        this.progresso = progresso;
    }

    public List<EsercizioTipo1> getEserciziTipo1() {
        return eserciziTipo1;
    }

    public void setEserciziTipo1(List<EsercizioTipo1> eserciziTipo1) {
        this.eserciziTipo1 = eserciziTipo1;
    }

    public List<EsercizioTipo2> getEserciziTipo2() {
        return eserciziTipo2;
    }

    public void setEserciziTipo2(List<EsercizioTipo2> eserciziTipo2) {
        this.eserciziTipo2 = eserciziTipo2;
    }

    public List<EsercizioTipo3> getEserciziTipo3() {
        return eserciziTipo3;
    }

    public void setEserciziTipo3(List<EsercizioTipo3> eserciziTipo3) {
        this.eserciziTipo3 = eserciziTipo3;
    }

    public List<List<?>> getAllEsercizi() {
        return List.of(eserciziTipo1, eserciziTipo2, eserciziTipo3);
    }
}