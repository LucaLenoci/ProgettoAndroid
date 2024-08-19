package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class DashboardBambinoActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private LinearLayout contentLayout;
    private Child child;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard_bambino);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tabLayout = findViewById(R.id.tabLayout);
        contentLayout = findViewById(R.id.contentLayout);

        Intent intent = getIntent();
        child = (Child) intent.getSerializableExtra("child");

        if (child != null) {
            setupTabs();
//            displayChildInfo();
        }
    }

    private void setupTabs() {
        if (!child.getEserciziTipo1().isEmpty()) {
            tabLayout.addTab(tabLayout.newTab().setText("Denominazione immagine"));
        }
        if (!child.getEserciziTipo2().isEmpty()) {
            tabLayout.addTab(tabLayout.newTab().setText("Riconoscimento coppie minime"));
        }
        if (!child.getEserciziTipo3().isEmpty()) {
            tabLayout.addTab(tabLayout.newTab().setText("Ripetizione sequenza di parole"));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateContent(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Select the first tab by default
        if (tabLayout.getTabCount() > 0) {
            updateContent(0);
        }
    }

    private void updateContent(int position) {
        contentLayout.removeAllViews();
        switch (position) {
            case 0:
                if (!child.getEserciziTipo1().isEmpty()) {
                    displayEsercizi(child.getEserciziTipo1(), "Denominazione immagine");
                }
                break;
            case 1:
                if (!child.getEserciziTipo2().isEmpty()) {
                    displayEsercizi(child.getEserciziTipo2(), "Riconoscimento coppie minime");
                }
                break;
            case 2:
                if (!child.getEserciziTipo3().isEmpty()) {
                    displayEsercizi(child.getEserciziTipo3(), "Ripetizione sequenza di parole");
                }
                break;
        }
    }

    private void displayChildInfo() {
        TextView infoTextView = new TextView(this);
        infoTextView.setText(String.format("Nome: %s\nProgresso: %d%%", child.getNome(), child.getProgresso()));
        contentLayout.addView(infoTextView);
    }

    private void displayEsercizi(List<?> esercizi, String tipo) {
        for (int i = 0; i < esercizi.size(); i++) {
            View esercizioView = LayoutInflater.from(this).inflate(R.layout.layout_item_esercizio, contentLayout, false);
            TextView titleTextView = esercizioView.findViewById(R.id.esercizioTitleTextView);
            TextView detailsTextView = esercizioView.findViewById(R.id.esercizioDetailsTextView);

            titleTextView.setText(String.format("%s - %d", tipo, i + 1));
            detailsTextView.setText(getEsercizioDetails(esercizi.get(i)));

            contentLayout.addView(esercizioView);
        }
    }

    private String getEsercizioDetails(Object esercizio) {
        if (esercizio instanceof EsercizioTipo1) {
            EsercizioTipo1 es = (EsercizioTipo1) esercizio;
            return String.format("Corretto: %s\nRisposta: %s\nRisposta corretta: %s\nSuggerimento: %s",
                    es.isEsercizio_corretto(), es.getRisposta(), es.getRisposta_corretta(), es.getSuggerimento());
        } else if (esercizio instanceof EsercizioTipo2) {
            EsercizioTipo2 es = (EsercizioTipo2) esercizio;
            return String.format("Corretto: %s\nRisposta: %s\nRisposta corretta: %s\nRisposta sbagliata: %s\nImmagine corretta: %s",
                    es.isEsercizio_corretto(), es.getRisposta(), es.getRisposta_corretta(), es.getRisposta_sbagliata(), es.getImmagine_corretta());
        } else if (esercizio instanceof EsercizioTipo3) {
            EsercizioTipo3 es = (EsercizioTipo3) esercizio;
            return String.format("Corretto: %s\nRisposta corretta: %s",
                    es.isEsercizio_corretto(), es.getRisposta_corretta());
        }
        return "Tipo di esercizio sconosciuto";
    }
}