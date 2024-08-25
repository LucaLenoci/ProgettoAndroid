package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class HomeBambinoActivity extends AppCompatActivity {

    private static final String TAG = "HomeBambinoActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String selectedDate;
    private TextView tvNome, tvCoins;
    private ProgressBar progressBar;
    private ImageView ProfilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_bambino);
        ProfilePic = findViewById(R.id.ProfilePic);
        loadCurrentAvatar("1");
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
        String bambinoPath = intent.getStringExtra("bambino");

        // Set up the button click listener
        esercizioButton.setOnClickListener(v -> checkAndProceedToExercises());

        ProfilePic.setOnClickListener(v -> {
            Intent intentAvatar = new Intent(HomeBambinoActivity.this, AvatarActivity.class);
            intentAvatar.putExtra("bambinoId", "1");  // Pass the bambino ID to AvatarActivity
            startActivity(intentAvatar);
        });
    }

    private void checkAndProceedToExercises() {
        checkIfExerciseExists(selectedDate, new FirestoreCallback() {
            @Override
            public void onCallback(boolean hasExercises) {
                if (hasExercises) {
                    Intent intentEsercizi = new Intent(HomeBambinoActivity.this, HomeEserciziBambinoActivity.class);
                    intentEsercizi.putExtra("selectedDate", selectedDate);
                    intentEsercizi.putExtra("bambinoId", "1");
                    startActivity(intentEsercizi);
                } else {
                    showNoExercisesPopup();
                }
            }
        });
    }

    private void checkIfExerciseExists(String date, FirestoreCallback callback) {
        // Reference to the specific document based on the date
        db.collection("esercizi")
                .document("1")
                .collection("tipo1")
                .document(date)
                .get()
                .addOnCompleteListener(task -> {
                    boolean exists = task.isSuccessful() && task.getResult().exists();

                    if (callback != null) {
                        // Invoke the callback with the result
                        db.collection("esercizi")
                                .document("1")
                                .collection("tipo2")
                                .document(date)
                                .get()
                                .addOnCompleteListener(task_2 -> {
                                    boolean exists_2 = task_2.isSuccessful() && task_2.getResult().exists();

                                    if (callback != null) {
                                        // Invoke the callback with the result
                                        db.collection("esercizi")
                                                .document("1")
                                                .collection("tipo3")
                                                .document(date)
                                                .get()
                                                .addOnCompleteListener(task_3 -> {
                                                    boolean exists_3 = task_3.isSuccessful() && task_3.getResult().exists();

                                                    if (callback != null) {
                                                        // Invoke the callback with the result
                                                        callback.onCallback(exists);
                                                    }

                                                    if (!exists_3) {
                                                        Log.d(TAG, "No exercise found for the date: " + date);
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error checking exercise for date: " + date, e);

                                                    if (callback != null) {
                                                        // Notify the callback about the failure
                                                        callback.onCallback(false);
                                                    }
                                                });
                                    }

                                    if (!exists_2) {
                                        Log.d(TAG, "No exercise found for the date: " + date);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error checking exercise for date: " + date, e);

                                    if (callback != null) {
                                        // Notify the callback about the failure
                                        callback.onCallback(false);
                                    }
                                });
                    }

                    if (!exists) {
                        Log.d(TAG, "No exercise found for the date: " + date);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking exercise for date: " + date, e);

                    if (callback != null) {
                        // Notify the callback about the failure
                        callback.onCallback(false);
                    }
                });
    }


    private void showNoExercisesPopup() {
        new AlertDialog.Builder(this)
                .setTitle("Nessun esercizio")
                .setMessage("Non ci sono esercizi per la data selezionata.")
                .setPositiveButton(android.R.string.ok, null)
                .show();
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
                            Log.d("FirestoreData", "Document data: " + data.toString());

                            Long progressoLong = (Long) data.get("progresso");
                            String nomeString = (String) data.get("nome");
                            Long coinsLong = (Long) data.get("coins");

                            if (progressoLong != null) {
                                int progresso = progressoLong.intValue();
                                progressBar.setProgress(progresso);
                            } else {
                                Log.d("FirestoreData", "Progresso not found");
                            }

                            if (coinsLong != null) {
                                String coins = coinsLong.toString();
                                tvCoins.append(coins);
                            } else {
                                Log.d("FirestoreData", "Coins not found");
                            }

                            if (nomeString != null) {
                                tvNome.append(nomeString);
                            } else {
                                Log.d("FirestoreData", "Nome not found");
                            }
                        } else {
                            Log.d("FirestoreData", "No data found in the document");
                        }
                    } else {
                        Log.d("FirestoreData", "No such document");
                        Toast.makeText(HomeBambinoActivity.this, "Documento non trovato", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error fetching document", e);
                    Toast.makeText(HomeBambinoActivity.this, "Errore di lettura documento", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCurrentAvatar(String bambinoId) {
        db.collection("bambini")
                .document(bambinoId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String avatarFilename = documentSnapshot.getString("avatarCorrente");
                        if (avatarFilename != null) {
                            db.collection("avatars").document(avatarFilename).get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                String imagePath = document.getString("imageUrl");

                                                if (imagePath != null && !imagePath.isEmpty()) {
                                                    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imagePath);
                                                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                                        Log.d(TAG, "Image URL retrieved successfully for " + avatarFilename);
                                                        Glide.with(HomeBambinoActivity.this).load(uri).into(ProfilePic);
                                                    }).addOnFailureListener(exception -> {
                                                        Log.e(TAG, "Failed to get download URL for " + avatarFilename, exception);
                                                    });
                                                } else {
                                                    Log.e(TAG, "Image path is null or empty for " + avatarFilename);
                                                }
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("LoadAvatar", "Failed to load avatar image", e);
                                    });
                        }
                    }
                });
    }

    private interface FirestoreCallback {
        void onCallback(boolean hasExercises);
    }
}
