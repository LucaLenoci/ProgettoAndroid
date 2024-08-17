package com.example.progetto_mobile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public class RipetizioneSequenzeParoleActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final int RQ_SPEECH_REC = 102;
    private TextView tvText;
    private TextToSpeech tts;
    private FirebaseFirestore db;
    private EsercizioTipo3 currentExercise;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ripetizione_sequenze_parole);  // Assuming you have a separate layout file

        db = FirebaseFirestore.getInstance();

        fetchExerciseData();


        Button btnButton = findViewById(R.id.btn_button_2);
        tvText = findViewById(R.id.tv_text_2);

        btnButton.setOnClickListener(v -> askSpeechInput());

        Button speakButton = findViewById(R.id.speak_button_2);
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
        // Firestore query to get the specific document
        db.collection("esercizi")
                .document("1")
                .collection("tipo3")
                .document("16-08-2024")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Map the document data to the currentExercise object
                            currentExercise = document.toObject(EsercizioTipo3.class);

                            // Logging the data (optional)
                            Log.d("Firestore", "DocumentSnapshot data: " + document.getData());

                            // Use the data (e.g., display it or trigger other actions)
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
        super.onDestroy();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RQ_SPEECH_REC && resultCode == Activity.RESULT_OK) {
            ArrayList<String> result = data != null ? data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) : null;
            if (result != null && !result.isEmpty()) {
                String recognizedText = result.get(0);
                tvText.setText(recognizedText);

                // Compare the recognized text with the correct answer
                if (currentExercise != null && recognizedText.equalsIgnoreCase(currentExercise.getRisposta_corretta())) {
                    Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
