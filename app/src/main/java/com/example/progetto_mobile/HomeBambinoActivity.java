package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Map;

public class HomeBambinoActivity extends AppCompatActivity {

    private String selectedDate;
    private TextView tvNome, tvCoins, tvEta;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_bambino);

        tvNome = findViewById(R.id.Nome);
        tvCoins = findViewById(R.id.Coins);
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

        getChildFromFirestore();

        Intent intent = getIntent();
        User user = (User) intent.getSerializableExtra("user");
        if (user != null) {
            String nome = "Nome: " + user.getNome();
            String eta = "Età: " + user.getEta();
            tvNome.setText(nome);
            tvEta.setText(eta);

        }

        // Set up the button click listener
        esercizioButton.setOnClickListener(v -> {
            // Intent to open EserciziBambinoActivity
            Intent intentEsercizi = new Intent(HomeBambinoActivity.this, HomeEserciziBambinoActivity.class);
            intentEsercizi.putExtra("selectedDate", selectedDate);
            intentEsercizi.putExtra("bambinoId", "1");
            startActivity(intentEsercizi);
        });
    }

    private void getChildFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the specific document with ID "1" in the "bambini" collection
        DocumentReference docRef = db.collection("bambini").document("1");

        // Fetch the document
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Document exists, retrieve data
                        Map<String, Object> data = documentSnapshot.getData();

                        if (data != null) {
                            // Data retrieved successfully, handle the data
                            // For example, print the data to log
                            Log.d("FirestoreData", "Document data: " + data.toString());

                            // Access specific fields if needed
                            Object infoRefObject = data.get("infoRef");
                            Long progressoLong = (Long) data.get("progresso");
                            String nomeString = (String) data.get("nome");
                            Long coinsLong = (Long) data.get("coins");


                            // Example handling of fields
                            if (infoRefObject != null) {
                                Log.d("FirestoreData", "InfoRef: " + infoRefObject.toString());
                            } else {
                                Log.d("FirestoreData", "InfoRef not found");
                            }

                            if (progressoLong != null) {
                                int progresso = progressoLong.intValue();
                                // Update UI with the progress value
                                progressBar.setProgress(progresso);
                            } else {
                                Log.d("FirestoreData", "Progresso not found");
                            }

                            if (coinsLong != null) {
                                String coins = coinsLong.toString();
                                // Update UI with the progress value
                                tvCoins.append(coins);
                            } else {
                                Log.d("FirestoreData", "Coins not found");
                            }

                            if (nomeString != null) {
                                // Update UI with the progress value
                                tvNome.append(nomeString);
                            } else {
                                Log.d("FirestoreData", "Nome not found");
                            }
                        } else {
                            Log.d("FirestoreData", "No data found in the document");
                        }
                    } else {
                        // Document does not exist
                        Log.d("FirestoreData", "No such document");
                        Toast.makeText(HomeBambinoActivity.this, "Documento non trovato", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    Log.e("FirestoreError", "Error fetching document", e);
                    Toast.makeText(HomeBambinoActivity.this, "Errore di lettura documento", Toast.LENGTH_SHORT).show();
                });
    }

}
