package com.example.progetto_mobile;

import android.app.Activity;
import android.content.Intent;
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
import java.util.Locale;

public class DenominazioneImmaginiActivity extends AppCompatActivity {
    private static final int RQ_SPEECH_REC = 102;
    private TextView tvText;
    private TextView tvText_2;
    private FirebaseFirestore db;
    private EsercizioTipo1 currentExercise;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference imagesRef = storage.getReferenceFromUrl("gs://progetto-mobile-24.appspot.com/immagini");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.denominazione_immagini);  // Assuming you have a separate layout file

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch the exercise data from Firestore
        fetchExerciseData();

        Button btnButton = findViewById(R.id.btn_button);
        tvText = findViewById(R.id.tv_text);
        tvText_2 = findViewById(R.id.textView9);

        btnButton.setOnClickListener(v -> askSpeechInput());
    }

    private void fetchExerciseData() {
        // Firestore query to get the specific document
        db.collection("esercizi")
                .document("1")
                .collection("tipo1")
                .document("16-08-2024")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Map the document data to the currentExercise object
                            currentExercise = document.toObject(EsercizioTipo1.class);

                            // Logging the data (optional)
                            Log.d("Firestore", "DocumentSnapshot data: " + document.getData());

                            // Use the data (e.g., display it or trigger other actions)
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
        // Assuming Exercise class has a method to get the image file name
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

                // Compare the recognized text with the correct answer
                if (currentExercise != null && recognizedText.equalsIgnoreCase(currentExercise.getRisposta_corretta())) {
                    Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
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
}
