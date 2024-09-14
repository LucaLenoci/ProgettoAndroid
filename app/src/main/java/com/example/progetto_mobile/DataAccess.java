package com.example.progetto_mobile;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class DataAccess {

    private static final String TAG = "FirestoreExample";
    private FirebaseFirestore db;
    private Context context;  // Pass context externally

    // Constructor to accept a context
    public DataAccess(Context context) {
        this.context = context;
        db = FirebaseFirestore.getInstance();
    }

    // Method to be called with email from the caller (instead of onCreate)
    public void getCollection(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

                        DocumentReference infoRef = documentSnapshot.getDocumentReference("infoRef");
                        if (infoRef != null) {
                            Log.d("InfoRefPath", infoRef.getPath());
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

    // Querying Firestore for data
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

    // Updating the UI, now using the passed context
    private void updateUI(int tipologia, String path) {
        String key = "user";
        Intent intent = null;

        switch (tipologia) {
            case 0:
                key = "bambino";
                break;

            case 1:
                key = "genitore";
                intent = new Intent(context, ScegliAccountActivity.class);
                break;

            case 2:
                key = "logopedista";
                intent = new Intent(context, LogopedistaActivity.class);
                break;

            default:
                Log.w(TAG, "Tipologia utente sconosciuta: " + tipologia);
                break;
        }

        if (intent != null) {
            intent.putExtra(key, path);
            context.startActivity(intent);  // Use context to start the activity
        }
    }
}
