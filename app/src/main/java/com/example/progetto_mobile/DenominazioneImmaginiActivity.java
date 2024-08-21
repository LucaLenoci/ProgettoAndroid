package com.example.progetto_mobile;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class DenominazioneImmaginiActivity extends AppCompatActivity {
    private static final int RQ_SPEECH_REC = 102;
    private TextView tvText;
    private TextView tvText_2;
    private FirebaseFirestore db;
    private EsercizioTipo1 currentExercise;
    private KonfettiView konfettiView;
    private MediaPlayer successSound;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference imagesRef = storage.getReferenceFromUrl("gs://progetto-mobile-24.appspot.com/immagini");
    String selectedDate;
    String bambinoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.denominazione_immagini);

        selectedDate = getIntent().getStringExtra("selectedDate");
        bambinoId = getIntent().getStringExtra("bambinoId");

        // Initialize Firestore and other components
        db = FirebaseFirestore.getInstance();
        konfettiView = findViewById(R.id.konfettiView);
        successSound = MediaPlayer.create(this, R.raw.success_sound); // Ensure the sound file is in res/raw

        fetchExerciseData();

        Button btnButton = findViewById(R.id.btn_button);
        tvText = findViewById(R.id.tv_text);
        tvText_2 = findViewById(R.id.textView9);

        btnButton.setOnClickListener(v -> askSpeechInput());
    }

    private void fetchExerciseData() {
        db.collection("esercizi")
                .document(bambinoId)
                .collection("tipo1")
                .document(selectedDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            currentExercise = document.toObject(EsercizioTipo1.class);
                            Log.d("Firestore", "DocumentSnapshot data: " + document.getData());
                            loadExerciseData(currentExercise);
                        } else {
                            Log.d("Firestore", "No such document");
                        }
                    } else {
                        Log.d("Firestore", "get failed with ", task.getException());
                    }
                });
    }

    private void loadExerciseData(EsercizioTipo1 exercise) {
        String imageFileName = exercise.getRisposta_corretta() + ".jpg";  // Modify according to your Exercise class structure
        StorageReference imageRef = imagesRef.child(imageFileName);

        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl = uri.toString();
            Log.d("Image URL", imageUrl);
            displayImage(imageUrl);
        }).addOnFailureListener(exception -> {
            Log.e("Firebase Storage", "Failed to get download URL", exception);
        });

        tvText_2.setText(exercise.getRisposta_corretta());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RQ_SPEECH_REC && resultCode == Activity.RESULT_OK) {
            ArrayList<String> result = data != null ? data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) : null;
            if (result != null && !result.isEmpty()) {
                String recognizedText = result.get(0);
                tvText.setText(recognizedText);

                if (currentExercise != null && recognizedText.equalsIgnoreCase(currentExercise.getRisposta_corretta())) {
                    Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
                    showConfettiEffect();
                    if (successSound != null) {
                        successSound.start();
                        updateCoinsInFirebase();
                        Button btnButton = findViewById(R.id.btn_button);
                        btnButton.setEnabled(false);
                    }
                } else {
                    Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void askSpeechInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition is not available", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something!");
            startActivityForResult(intent, RQ_SPEECH_REC);
        }
    }

    private void displayImage(String imageUrl) {
        ImageView imageView = findViewById(R.id.imageView);

        Glide.with(this)
                .load(imageUrl)
                .into(imageView);
    }

    private void showConfettiEffect() {
        EmitterConfig emitterConfig = new Emitter(100L, TimeUnit.MILLISECONDS).max(100);
        konfettiView.start(
                new PartyFactory(emitterConfig)
                        .spread(360)
                        .shapes(Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE))
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(0f, 30f)
                        .position(new Position.Relative(0.5, 0.3))
                        .build()
        );
    }

    @Override
    protected void onDestroy() {
        if (successSound != null) {
            successSound.release();
            successSound = null;
        }
        super.onDestroy();
    }

    private void updateCoinsInFirebase() {
        // Reference to the child's document in Firestore
        db.collection("bambini").document(bambinoId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Long currentCoins = document.getLong("coins");
                            if (currentCoins != null) {
                                // Increment the coins by 1
                                db.collection("bambini").document(bambinoId)
                                        .update("coins", currentCoins + 1)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Firestore", "Coins updated successfully.");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error updating coins", e);
                                        });

                                db.collection("esercizi")
                                        .document(bambinoId)
                                        .collection("tipo1")
                                        .document(selectedDate)
                                        .update("esercizio_corretto", true)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Firestore", "Esercizio corretto updated successfully.");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error updating esercizio corretto", e);
                                        });
                            }

                        }
                    } else {
                        Log.d("Firestore", "Failed to fetch document: ", task.getException());
                    }
                });
    }
}
