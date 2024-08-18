package com.example.progetto_mobile;

import com.google.firebase.firestore.DocumentReference;

import java.io.Serializable;
import java.util.List;

public class Child implements Serializable {
    private String nome;
    private int progresso;
    private List<DocumentReference> eserciziTipo1;
    private List<DocumentReference> eserciziTipo2;
    private List<DocumentReference> eserciziTipo3;

    public Child() {
        // Default constructor required for Firestore
    }

    public Child(String nome, int progresso, List<DocumentReference> eserciziTipo1,
                 List<DocumentReference> eserciziTipo2, List<DocumentReference> eserciziTipo3) {
        this.nome = nome;
        this.progresso = progresso;
        this.eserciziTipo1 = eserciziTipo1;
        this.eserciziTipo2 = eserciziTipo2;
        this.eserciziTipo3 = eserciziTipo3;
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

    public List<DocumentReference> getEserciziTipo1() {
        return eserciziTipo1;
    }

    public void setEserciziTipo1(List<DocumentReference> eserciziTipo1) {
        this.eserciziTipo1 = eserciziTipo1;
    }

    public List<DocumentReference> getEserciziTipo2() {
        return eserciziTipo2;
    }

    public void setEserciziTipo2(List<DocumentReference> eserciziTipo2) {
        this.eserciziTipo2 = eserciziTipo2;
    }

    public List<DocumentReference> getEserciziTipo3() {
        return eserciziTipo3;
    }

    public void setEserciziTipo3(List<DocumentReference> eserciziTipo3) {
        this.eserciziTipo3 = eserciziTipo3;
    }

    public List<List<DocumentReference>> getAllEsercizi() {
        return List.of(eserciziTipo1, eserciziTipo2, eserciziTipo3);
    }
}