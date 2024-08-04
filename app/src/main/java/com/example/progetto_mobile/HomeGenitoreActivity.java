package com.example.progetto_mobile;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class HomeGenitoreActivity extends AppCompatActivity {

    private static final String[] TipologieEsercizi = {"Denominazione immagini",
            "Ripetizione sequenze di parole",
            "Riconoscimento coppie minime"};

    private void init() {
        /*
        usa volta fatto il login, prende i dati dell'utente dal database
         */
    }

    // funzione che crea dati random per mostrarli nella schermata del genitore
    private void simulaInit() {
        // Ottieni il LinearLayout dal layout XML per aggiungere i bottoni
        LinearLayout layout = findViewById(R.id.linear_layout);
        Random random = new Random();

        // Genera un numero casuale di TextView (da 1 a 5)
        int numeroTextView = random.nextInt(5) + 1;

        for (int i = 0; i < numeroTextView; i++) {
            // Crea una nuova TextView
            TextView textView = new TextView(this);

            // Genera dati casuali
            int index = random.nextInt(TipologieEsercizi.length);
            String tipoEsercizio = TipologieEsercizi[index];

            int minuti = random.nextInt(6) + 5; // Da 5 a 10 minuti
            int secondi = random.nextInt(60); // Da 0 a 59 secondi
            String tempoImpiegato = minuti + "min " + secondi + "sec";

            int aiutiUsati = random.nextInt(3) + 1; // Da 1 a 3 aiuti

            // Imposta il testo della TextView
            String testo = "Esercizio: " + tipoEsercizio + "\n" +
                    "Tempo Impiegato: " + tempoImpiegato + "\n" +
                    "Aiuti utilizzati: " + aiutiUsati;
            textView.setText(testo);
            //textView.setBackgroundResource(R.color.light_grey);

            // Imposta i parametri del layout per la TextView
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(16, 16, 16, 16); // Aggiunge margini intorno alla TextView

            // Aggiungi la TextView al layout
            layout.addView(textView, params);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_genitore);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // inizializza i dati per mostrare i progressi della mappa del figlio
        init();
        simulaInit();
    }
}
