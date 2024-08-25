package com.example.progetto_mobile;

import android.app.Activity;
import android.content.Intent;
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

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.appcompat.app.AppCompatActivity;

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

public class DenominazioneImmaginiActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final int RQ_SPEECH_REC = 102;
    private TextView tvText;
    private TextToSpeech tts;
    private FirebaseFirestore db;
    private EsercizioTipo1 currentExercise;
    private KonfettiView konfettiView;
    private MediaPlayer successSound;
    private boolean isSuggestion1Used = false;
    private boolean isSuggestion2Used = false;
    private boolean isSuggestion3Used = false;

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
        successSound = MediaPlayer.create(this, R.raw.success_sound);

        fetchExerciseData();

        // Button and TTS initialization for suggestion 1
        Button speakButton = findViewById(R.id.suggerimento1);
        speakButton.setOnClickListener(v -> {
            String textToSpeak = currentExercise.getSuggerimento();
            if (!isSuggestion1Used) {
                updateSuggerimentiUsatiInFirebase(0);
                isSuggestion1Used = true;
            }
            speakText(textToSpeak);
        });

        // Button and TTS initialization for suggestion 2
        Button speakButton_2 = findViewById(R.id.suggerimento2);
        speakButton_2.setOnClickListener(v -> {
            String textToSpeak = currentExercise.getSuggerimento2();
            if (!isSuggestion2Used) {
                updateSuggerimentiUsatiInFirebase(1);
                isSuggestion2Used = true;
            }
            speakText(textToSpeak);
        });

        // Button and TTS initialization for suggestion 3
        Button speakButton_3 = findViewById(R.id.suggerimento3);
        speakButton_3.setOnClickListener(v -> {
            String textToSpeak = currentExercise.getSuggerimento3();
            if (!isSuggestion3Used) {
                updateSuggerimentiUsatiInFirebase(2);
                isSuggestion3Used = true;
            }
            speakText(textToSpeak);
        });

        tts = new TextToSpeech(this, this);

        ImageButton btnButton = findViewById(R.id.btn_button);
        tvText = findViewById(R.id.tv_text);

        btnButton.setOnClickListener(v -> askSpeechInput());
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d("TTS", "TextToSpeech initialized successfully");
        } else {
            Log.e("TTS", "TextToSpeech initialization failed");
        }
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

                            // Initialize suggestion usage status
                            ArrayList<Boolean> suggerimentiList = (ArrayList<Boolean>) document.get("suggerimenti");
                            if (suggerimentiList != null && suggerimentiList.size() == 3) {
                                isSuggestion1Used = suggerimentiList.get(0);
                                isSuggestion2Used = suggerimentiList.get(1);
                                isSuggestion3Used = suggerimentiList.get(2);
                            }

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
        String imageFileName = exercise.getRisposta_corretta() + ".png";
        StorageReference imageRef = imagesRef.child(imageFileName);

        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl = uri.toString();
            Log.d("Image URL", imageUrl);
            displayImage(imageUrl);
        }).addOnFailureListener(exception -> {
            Log.e("Firebase Storage", "Failed to get download URL", exception);
        });
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
                        ImageButton btnButton = findViewById(R.id.btn_button);
                        incrementTentativiInFirebase();
                        btnButton.setEnabled(false);
                    }
                } else {
                    Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show();
                    incrementTentativiInFirebase();
                }
            }
        }
    }


    private void incrementTentativiInFirebase() {
        db.collection("esercizi")
                .document(bambinoId)
                .collection("tipo1")
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
                                    .collection("tipo1")
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
        db.collection("bambini").document(bambinoId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Long currentCoins = document.getLong("coins");
                            if (currentCoins != null) {
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
                                            Log.d("Firestore", "Exercise marked as completed.");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error marking exercise as completed", e);
                                        });
                            }
                        }
                    } else {
                        Log.d("Firestore", "Failed to fetch document: ", task.getException());
                    }
                });
    }

    private void updateSuggerimentiUsatiInFirebase(int suggerimentoIndex) {
        db.collection("esercizi")
                .document(bambinoId)
                .collection("tipo1")
                .document(selectedDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Long currentSuggerimentiUsati = document.getLong("suggerimentiUsati");
                            ArrayList<Boolean> suggerimentiList = (ArrayList<Boolean>) document.get("suggerimenti");

                            if (currentSuggerimentiUsati == null) {
                                currentSuggerimentiUsati = 0L;
                            }

                            if (suggerimentiList == null || suggerimentiList.size() != 3) {
                                // Initialize array if it doesn't exist or is not the correct size
                                suggerimentiList = new ArrayList<>(Arrays.asList(false, false, false));
                            }

                            if (!suggerimentiList.get(suggerimentoIndex)) {
                                // If the suggestion has not been used yet
                                suggerimentiList.set(suggerimentoIndex, true);
                                db.collection("esercizi")
                                        .document(bambinoId)
                                        .collection("tipo1")
                                        .document(selectedDate)
                                        .update("suggerimentiUsati", currentSuggerimentiUsati + 1,
                                                "suggerimenti", suggerimentiList)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Firestore", "SuggerimentiUsati updated successfully.");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error updating suggerimentiUsati", e);
                                        });
                            }
                        }
                    } else {
                        Log.d("Firestore", "Failed to fetch document: ", task.getException());
                    }
                });
    }

    private void speakText(String textToSpeak) {
        if (tts != null) {
            float speechRate = 1.0f; // Adjust this value as needed
            tts.setSpeechRate(speechRate);
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
}
