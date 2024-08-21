package com.example.progetto_mobile;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        String email = intent.getStringExtra("key");
        // Query the collection and store data
        queryAndStoreData(email);
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
        Intent intent = null;

        switch (user.getTipologia()) {
            case 0:
                intent = new Intent(DataAccess.this, HomeBambinoActivity.class);
                break;

            case 1:
                intent = new Intent(DataAccess.this, HomeGenitoreActivity.class);
                break;

            case 2:
                intent = new Intent(DataAccess.this, HomeLogopedistaActivity.class);
                break;

            default:
                // Handle unexpected cases if necessary
                break;
        }

        if (intent != null) {
            intent.putExtra("user", user);
            startActivity(intent);
            finish();
        }
    }
}
