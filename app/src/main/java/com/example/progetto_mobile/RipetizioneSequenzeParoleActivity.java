package com.example.progetto_mobile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class RipetizioneSequenzeParoleActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final int RQ_SPEECH_REC = 102;
    private TextView tvText;
    private static final String TAG = "RipetizioneSequenzeParoleActivity";
    private TextToSpeech tts;
    private FirebaseFirestore db;
    private EsercizioTipo3 currentExercise;
    private KonfettiView konfettiView;
    private MediaPlayer successSound;
    String selectedDate;
    String bambinoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ripetizione_sequenze_parole);

        selectedDate = getIntent().getStringExtra("selectedDate");
        bambinoId = getIntent().getStringExtra("bambinoId");

        db = FirebaseFirestore.getInstance();

        konfettiView = findViewById(R.id.konfettiView_2);
        successSound = MediaPlayer.create(this, R.raw.success_sound); // Ensure you have the correct file in the res/raw folder

        fetchTema();
        fetchExerciseData();

        ImageButton btnButton = findViewById(R.id.btn_button_2);
        tvText = findViewById(R.id.tv_text_2);

        btnButton.setOnClickListener(v -> askSpeechInput());

        ImageButton speakButton = findViewById(R.id.speak_button_2);
        speakButton.setOnClickListener(v -> {
            String textToSpeak = currentExercise.getRisposta_corretta();

            // Get a list of available languages
            Set<Locale> languages = tts.getAvailableLanguages();

            // Find a desired language (e.g., Italian)
            Locale selectedLanguage = Locale.ITALIAN; // Replace with your desired language

            // Set the language
            if (languages.contains(selectedLanguage)) {
                tts.setLanguage(selectedLanguage);
            }

            float speechRate = 1.0f; // Adjust this value as needed
            tts.setSpeechRate(speechRate);

            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
        });
        tts = new TextToSpeech(this, this);
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

    private void fetchExerciseData() {
        db.collection("esercizi")
                .document(bambinoId)
                .collection("tipo3")
                .document(selectedDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            currentExercise = document.toObject(EsercizioTipo3.class);
                            Log.d("Firestore", "DocumentSnapshot data: " + document.getData());
                        } else {
                            Log.d("Firestore", "No such document");
                        }
                    } else {
                        Log.d("Firestore", "get failed with ", task.getException());
                    }
                });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // TTS is ready
        } else {
            // TTS initialization failed
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (successSound != null) {
            successSound.release();
            successSound = null;
        }
        super.onDestroy();
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
                        ImageButton btnButton = findViewById(R.id.btn_button_2);
                        incrementTentativiInFirebase();
                        btnButton.setEnabled(false);
                    }
                } else {
                    incrementTentativiInFirebase();
                    Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show();
                }
            }
        }
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
                                        .collection("tipo3")
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

    private void incrementTentativiInFirebase() {
        db.collection("esercizi")
                .document(bambinoId)
                .collection("tipo3")
                .document(selectedDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Long currentTentativi = document.getLong("tentativi");

                            if (currentTentativi == null) {
                                currentTentativi = 0L;
                            }

                            db.collection("esercizi")
                                    .document(bambinoId)
                                    .collection("tipo3")
                                    .document(selectedDate)
                                    .update("tentativi", currentTentativi + 1)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firestore", "Tentativi incremented successfully.");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firestore", "Error incrementing tentativi", e);
                                    });
                        }
                    } else {
                        Log.d("Firestore", "Failed to fetch document: ", task.getException());
                    }
                });
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
        ImageView imageView = findViewById(R.id.imageView6); // Your ImageView containing round_rect

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
        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout3);// Your ConstraintLayout

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

}
