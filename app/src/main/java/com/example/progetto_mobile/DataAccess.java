package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class DataAccess extends AppCompatActivity {

    private static final String TAG = "FirestoreExample";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        String email = intent.getStringExtra("key");
        // Query the collection and store data
        getCollection(email);
    }

    private void getCollection(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

                        DocumentReference infoRef = documentSnapshot.getDocumentReference("infoRef");
                        if (infoRef != null) {
                            Log.d("InfoRefPath", infoRef.getPath());
                            // Ora puoi fare una query alla collezione corretta
                            queryAndStoreData(infoRef);
                        } else {
                            Log.w(TAG, "infoRef non trovato per l'utente.");
                        }
                    } else {
                        Log.w(TAG, "Utente non trovato.");
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Errore nel recupero dell'utente.", e));
    }

    private void queryAndStoreData(DocumentReference infoRef) {
        infoRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null) {
                    Log.d(TAG, document.getId() + " => " + document.getData());

                    int tipologia = document.getLong("tipologia").intValue();
                    updateUI(tipologia, infoRef.getPath());
                }
            } else {
                Log.w(TAG, "Errore nel recupero dei documenti.", task.getException());
            }
        });
    }

    private void updateUI(int tipologia, String path) {
        String key = "user";
        Intent intent = null;

        switch (tipologia) {
            case 0:
                key = "bambino";

                break;

            case 1:
                key = "genitore";
                intent = new Intent(DataAccess.this, ScegliAccountActivity.class);
                break;

            case 2:
                key = "logopedista";
                intent = new Intent(DataAccess.this, HomeLogopedistaActivity.class);
                break;

            default:
                Log.w(TAG, "Tipologia utente sconosciuta: " + tipologia);
                break;
        }

        if (intent != null) {
            intent.putExtra(key, path);
            startActivity(intent);
            finish();
        }
    }
}
