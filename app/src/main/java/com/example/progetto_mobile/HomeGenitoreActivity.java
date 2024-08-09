package com.example.progetto_mobile;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Arrays;
import java.util.Random;

public class HomeGenitoreActivity extends AppCompatActivity {

    private static final String[] tipologieEsercizi = {"Denominazione immagini",
            "Ripetizione sequenze di parole",
            "Riconoscimento coppie minime"};
    private static final String[] paroleEsercizi = {"Casa", "Mela", "Pera", "Giovedi", "Vento",
            "Luna", "Sole", "Stella"};

    private String getRandomString(String[] array) {
        Random random = new Random();
        int index = random.nextInt(array.length);
        return array[index];
    }

    private String[] getRandomStrings(String[] array, int count) {
        String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            result[i] = getRandomString(array);
        }
        return result;
    }

    private String generatePopupText(String tipoEsercizio) {
        switch (tipoEsercizio) {
            case "Denominazione immagini":
                return "Parola immagine: " + getRandomString(paroleEsercizi) + "\n" +
                        "Parola bambino: " + getRandomString(paroleEsercizi);
            case "Ripetizione sequenze di parole":
                return "Sequenza parole: " + Arrays.toString(getRandomStrings(paroleEsercizi, 3)) + "\n" +
                        "Parole bambino: " + Arrays.toString(getRandomStrings(paroleEsercizi, 3));
            case "Riconoscimento coppie minime":
                String[] sequenzaParole = getRandomStrings(paroleEsercizi, 2);
                return "Parole immagini: " + Arrays.toString(sequenzaParole) + "\n" +
                        "Immagine da ripetere: " + getRandomString(sequenzaParole) + "\n" +
                        "Parola bambino: " + getRandomString(sequenzaParole);
            default:
                return "";
        }
    }

    private void showPopup(String tipoEsercizio, TextView textView) {
        // Crea un popup
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dettagli Esercizio");

        // Genera il testo del popup
        String popupText = generatePopupText(tipoEsercizio);

        // Imposta il contenuto del popup
        builder.setMessage(popupText);

        // Aggiungi i bottoni "Corretto" e "Sbagliato"
        builder.setPositiveButton("Corretto", (dialog, which) -> {
            textView.setBackgroundResource(R.drawable.border_green);
        });
        builder.setNegativeButton("Sbagliato", (dialog, which) -> {
            textView.setBackgroundResource(R.drawable.border_red);
        });

        // Mostra il popup
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void init() {
        /*
        usa volta fatto il login, prende i dati dell'utente dal database
         */
    }

    // funzione che crea dati random per mostrarli nella schermata del genitore
    private void simulaInit() {
        // Ottieni il LinearLayout dal layout XML per aggiungere le TextView
        LinearLayout layout = findViewById(R.id.linear_layout);
        Random random = new Random();

        // Genera un numero casuale di TextView (da 1 a 5)
        int numeroTextView = random.nextInt(5) + 1;

        for (int i = 0; i < numeroTextView; i++) {
            // Crea una nuova TextView
            TextView textView = new TextView(this);

            // Genera dati casuali
            int index = random.nextInt(tipologieEsercizi.length);
            String tipoEsercizio = tipologieEsercizi[index];

            int minuti = random.nextInt(6) + 5; // Da 5 a 10 minuti
            int secondi = random.nextInt(60); // Da 0 a 59 secondi
            String tempoImpiegato = minuti + "min " + secondi + "sec";

            int aiutiUsati = random.nextInt(3) + 1; // Da 1 a 3 aiuti

            // Imposta il testo della TextView
            String testo = "Esercizio: " + tipoEsercizio + "\n" +
                    "Tempo Impiegato: " + tempoImpiegato + "\n" +
                    "Aiuti utilizzati: " + aiutiUsati;
            textView.setText(testo);
            textView.setPadding(16, 16, 16, 16); // Aggiunge padding nella TextView

            // Imposta i parametri del layout per la TextView
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(16, 16, 16, 16); // Aggiunge margini intorno alla TextView

            // Aggiungi la TextView al layout
            layout.addView(textView, params);

            // Imposta il click listener per mostrare il popup
            textView.setOnClickListener(v -> showPopup(tipoEsercizio, textView));
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
