package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeBambinoActivity extends AppCompatActivity {

    private String selectedDate;
    private TextView tvNome, tvCognome, tvEta;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_bambino);

        tvNome = findViewById(R.id.Nome);
        tvCognome = findViewById(R.id.Cognome);
        tvEta = findViewById(R.id.Eta);
        progressBar = findViewById(R.id.progressBar);
        CalendarView calendarView = findViewById(R.id.calendarView);
        Button esercizioButton = findViewById(R.id.button);

        // Set default selected date to the current date
        selectedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        // Listener to capture the selected date
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Month is 0-based, so add 1
            int correctedMonth = month + 1;
            selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d", dayOfMonth, correctedMonth, year);
        });

        Intent intent = getIntent();
        User user = (User) intent.getSerializableExtra("user");
        if (user != null) {
            String nome = "Nome: " + user.getNome();
            String cognome = "Cognome: " + user.getCognome();
            String eta = "Età: " + user.getEta();
            tvNome.setText(nome);
            tvCognome.setText(cognome);
            tvEta.setText(eta);
            getProgressoFromFirestore(user.getEmail());
        }

        // Set up the button click listener
        esercizioButton.setOnClickListener(v -> {
            // Intent to open EserciziBambinoActivity
            Intent intentEsercizi = new Intent(HomeBambinoActivity.this, HomeEserciziBambinoActivity.class);
            intentEsercizi.putExtra("selectedDate", selectedDate);
            startActivity(intentEsercizi);
        });
    }

    private void getProgressoFromFirestore(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        DocumentReference infoRef = (DocumentReference) documentSnapshot.get("infoRef");

                        if (infoRef == null) {
                            Toast.makeText(HomeBambinoActivity.this, "Riferimento non trovato", Toast.LENGTH_SHORT).show();
                        } else {
                            infoRef.get().addOnSuccessListener(infoSnapshot -> {
                                if (infoSnapshot.exists()) {
                                    int progresso = infoSnapshot.getLong("progresso").intValue();
                                    progressBar.setProgress(progresso);
                                } else {
                                    Toast.makeText(HomeBambinoActivity.this, "Bambino non trovato", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(HomeBambinoActivity.this, "Utente non trovato", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
