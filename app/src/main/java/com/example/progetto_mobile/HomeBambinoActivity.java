package com.example.progetto_mobile;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeBambinoActivity extends AppCompatActivity {

    private static final String TAG = "HomeBambinoActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String selectedDate;
    private TextView tvNome, tvCoins;
    private ProgressBar progressBar;
    private ImageView ProfilePic;
    private String bambinoIdraw;
    private String bambinoId;
    private int currentStreak=0;
    private TextView numerostreak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_bambino);
        ProfilePic = findViewById(R.id.ProfilePic);
        bambinoIdraw = getIntent().getStringExtra("bambinoId");
        // Find the last occurrence of '/'
        int lastSlashIndex = bambinoIdraw.lastIndexOf('/');
        // Extract the substring after the last '/'
        bambinoId = bambinoIdraw.substring(lastSlashIndex + 1);
        numerostreak = findViewById(R.id.textView3);
        fetchTema();
        calculateStreak();
        loadCurrentAvatar(bambinoId);
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
        Log.d(TAG, "Bambino path: " + bambinoPath);

        // Set up the button click listener
        esercizioButton.setOnClickListener(v -> checkAndProceedToExercises());

        ProfilePic.setOnClickListener(v -> {
            Intent intentAvatar = new Intent(HomeBambinoActivity.this, AvatarActivity.class);
            intentAvatar.putExtra("bambinoId", bambinoId);  // Pass the bambino ID to AvatarActivity
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
                    intentEsercizi.putExtra("bambinoId", bambinoId);
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
                .document(bambinoId)
                .collection("tipo1")
                .document(date)
                .get()
                .addOnCompleteListener(task -> {
                    boolean exists = task.isSuccessful() && task.getResult().exists();

                    if (callback != null) {
                        // Invoke the callback with the result
                        db.collection("esercizi")
                                .document(bambinoId)
                                .collection("tipo2")
                                .document(date)
                                .get()
                                .addOnCompleteListener(task_2 -> {
                                    boolean exists_2 = task_2.isSuccessful() && task_2.getResult().exists();

                                    if (callback != null) {
                                        // Invoke the callback with the result
                                        db.collection("esercizi")
                                                .document(bambinoId)
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

        // Fetch the document
        db.collection("bambini").document(bambinoId).get()
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
                        String temaCorrente =  documentSnapshot.getString("tema");
                        String sessoBambino = documentSnapshot.getString("sesso");
                        String personaggi_da_visualizzare = "";
                        if (sessoBambino.equals("M")){
                            personaggi_da_visualizzare = "personaggi";
                        }else{
                            personaggi_da_visualizzare = "personaggi_femminili";
                        }
                        if (avatarFilename != null) {
                            db.collection("avatars").document(temaCorrente).collection(personaggi_da_visualizzare).document(avatarFilename).get()
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

    private void fetchTema() {
        db.collection("bambini").document(bambinoId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String temaCorrente =  document.getString("tema");
                            updateRoundRectColors(temaCorrente);
                            updateConstraintLayoutBackground(temaCorrente);
                        } else {
                            Log.e(TAG, "No such document for " + bambinoId);
                        }
                    } else {
                        Log.e(TAG, "Firestore get failed for " + bambinoId, task.getException());
                    }
                });
    }

    public void updateRoundRectColors(String theme){
        ImageView imageView = findViewById(R.id.imageView7); // Your ImageView containing round_rect

        int startColor = 0;
        int centerColor = 0;
        int endColor = 0;

        // Set colors based on the theme
        switch (theme) {
            case "supereroi":
            case "cartoni_animati":
                startColor = ContextCompat.getColor(this, R.color.supereroi1);
                centerColor = ContextCompat.getColor(this, R.color.supereroi2);
                endColor = ContextCompat.getColor(this, R.color.supereroi3);
                break;
            case "favole":
            case "videogiochi":
                startColor = ContextCompat.getColor(this, R.color.videogiochi1);
                centerColor = ContextCompat.getColor(this, R.color.videogiochi2);
                endColor = ContextCompat.getColor(this, R.color.videogiochi3);
                break;
        }

        // Update the drawable with the new colors
        GradientDrawable gradientDrawable = (GradientDrawable) imageView.getBackground();
        gradientDrawable.setColors(new int[]{startColor, centerColor, endColor});
        imageView.setBackground(gradientDrawable);
    }

    public void updateConstraintLayoutBackground(String theme) {
        ConstraintLayout constraintLayout = findViewById(R.id.main);// Your ConstraintLayout

        int backgroundColor = 0;

        // Set background color based on the theme
        switch (theme) {
            case "supereroi":
            case "cartoni_animati":
                backgroundColor = ContextCompat.getColor(this, R.color.supereroibackground); // Replace with actual color resource
                break;
            case "favole":
            case "videogiochi":
                backgroundColor = ContextCompat.getColor(this, R.color.videogiochibackground); // Replace with actual color resource
                break;
        }

        // Apply the background color to the ConstraintLayout
        constraintLayout.setBackgroundColor(backgroundColor);
    }

    private void calculateStreak() {
        Date currentDate = new Date();
        calculateDayStreak(currentDate);
    }

    private void calculateDayStreak(Date date) {
        String formattedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date);

        // Check if exercises exist for all types for the given date
        checkAllExerciseTypesForDate(formattedDate, new FirestoreCallback() {
            @Override
            public void onCallback(boolean allExercisesCompleted) {
                if (allExercisesCompleted) {
                    // If all exercises for this day are completed, increase the streak and check the previous day
                    currentStreak++;

                    // Move to the previous day
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendar.add(Calendar.DAY_OF_MONTH, -1);

                    // Recursively check the previous day
                    calculateDayStreak(calendar.getTime());
                } else {
                    // Not all exercises are completed for this day, streak ends
                    Toast.makeText(HomeBambinoActivity.this, "Current Streak: " + currentStreak + " days", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Current Streak: " + currentStreak + " days");
                    numerostreak.setText(String.valueOf(currentStreak));
                }
            }
        });
    }

    private void checkAllExerciseTypesForDate(String date, FirestoreCallback callback) {
        // Define the types of exercises to check
        String[] exerciseTypes = {"tipo1", "tipo2", "tipo3"};
        int totalTypes = exerciseTypes.length;
        final int[] countCorrect = {0}; // Count of exercise types that are correct
        final int[] countChecked = {0}; // Count of total exercise types checked

        for (String type : exerciseTypes) {
            db.collection("esercizi")
                    .document(bambinoId)
                    .collection(type)
                    .document(date)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Boolean isCorrect = document.getBoolean("esercizio_corretto");
                                if (Boolean.TRUE.equals(isCorrect)) {
                                    countCorrect[0]++;
                                }
                                countChecked[0]++;
                            } else {
                                countChecked[0]++;
                            }

                            // Check if all exercise types have been processed
                            if (countChecked[0] == totalTypes) {
                                // All types have been checked, determine if the streak should continue
                                if (countCorrect[0] == totalTypes) {
                                    callback.onCallback(true); // All exercises are correct
                                } else {
                                    callback.onCallback(false); // Not all exercises are correct
                                }
                            }
                        } else {
                            Log.e(TAG, "Error fetching document for type: " + type + " on date: " + date, task.getException());
                            // If there's an error fetching the document, we assume it affects the streak
                            countChecked[0]++;
                            if (countChecked[0] == totalTypes) {
                                callback.onCallback(false); // Assume failure if any document fetch fails
                            }
                        }
                    });
        }
    }


}
