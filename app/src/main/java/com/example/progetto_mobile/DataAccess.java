package com.example.progetto_mobile;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class DataAccess extends AppCompatActivity {

    private static final String TAG = "FirestoreExample";
    private FirebaseFirestore db;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.home_bambino);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Query the collection and store data
        queryAndStoreData("lucaxl10@gmail.com");
    }

    private void queryAndStoreData(String email) {
        CollectionReference collection = db.collection("users");

        collection.whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                // Map the document to your User data model class
                                user = document.toObject(User.class);

                                // Now that data is available, update the UI
                                if (user != null) {
                                    updateUI();
                                }
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void updateUI() {
        // Access individual fields and update the UI
        TextView nomeField = findViewById(R.id.Nome);
        TextView cognomeField = findViewById(R.id.Cognome);
        TextView etaField = findViewById(R.id.Eta);

        // Log all user data for debugging
        Log.d(TAG, "User Data:");
        Log.d(TAG, "Cognome: " + user.getCognome());
        Log.d(TAG, "Eta: " + user.getEta());
        Log.d(TAG, "Nome: " + user.getNome());
        Log.d(TAG, "Tipologia: " + user.getTipologia());
        Log.d(TAG, "Email: " + user.getEmail());

        nomeField.setText(user.getNome());
        cognomeField.setText(user.getCognome());
        etaField.setText(String.valueOf(user.getEta()));
    }
}
