package com.example.progetto_mobile;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.util.Size;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.emitter.PartyEmitter;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.xml.KonfettiView;


import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RiconoscimentoCoppieMinimeActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference imagesRef = storage.getReferenceFromUrl("gs://progetto-mobile-24.appspot.com/immagini");
    private FirebaseFirestore db;
    private EsercizioTipo2 currentExercise;
    private KonfettiView konfettiView;
    private MediaPlayer successSound;

    String selectedDate;
    String bambinoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedDate = getIntent().getStringExtra("selectedDate");
        bambinoId = getIntent().getStringExtra("bambinoId");

        setContentView(R.layout.riconoscimento_coppie_minime);
        konfettiView = findViewById(R.id.konfettiView);
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        fetchExerciseData();

        Button speakButton = findViewById(R.id.speak_button);
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

        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);

        button1.setOnClickListener(v -> checkAnswer(1));
        button2.setOnClickListener(v -> checkAnswer(2));

        successSound = MediaPlayer.create(this, R.raw.success_sound); // Replace `success_sound` with your actual sound file name

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

    private void fetchExerciseData() {
        // Firestore query to get the specific document
        db.collection("esercizi")
                .document(bambinoId)
                .collection("tipo2")
                .document(selectedDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Map the document data to the currentExercise object
                            currentExercise = document.toObject(EsercizioTipo2.class);

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

    private void loadExerciseData(EsercizioTipo2 exercise) {
        // Assuming Exercise class has a method to get the image file name
        String imageFileName = exercise.getRisposta_corretta() + ".jpg";  // Modify according to your Exercise class structure
        StorageReference imageRef = imagesRef.child(imageFileName);
        String imageFileName_2 = exercise.getRisposta_sbagliata() + ".jpg";  // Modify according to your Exercise class structure
        StorageReference imageRef_2 = imagesRef.child(imageFileName_2);


        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl_1 = uri.toString();
            Log.d("Image URL", imageUrl_1);
            imageRef_2.getDownloadUrl().addOnSuccessListener(uri_2 -> {
                String imageUrl_2 = uri_2.toString();
                Log.d("Image URL", imageUrl_2);
                displayImages(imageUrl_1, imageUrl_2);
            }).addOnFailureListener(exception -> {
                Log.e("Firebase Storage", "Failed to get download URL", exception);
            });
        }).addOnFailureListener(exception -> {
            Log.e("Firebase Storage", "Failed to get download URL", exception);
        });
    }

    private void displayImages(String imageUrl_1, String imageUrl_2) {
        ImageView imageView = findViewById(R.id.image1);
        ImageView imageView2 = findViewById(R.id.image2);

        if(currentExercise.getImmagine_corretta() == 1){
            Glide.with(this)
                    .load(imageUrl_1)
                    .into(imageView);

            Glide.with(this)
                    .load(imageUrl_2)
                    .into(imageView2);
        }else{
            Glide.with(this)
                    .load(imageUrl_2)
                    .into(imageView);


            Glide.with(this)
                    .load(imageUrl_1)
                    .into(imageView2);
        }

    }

    private void checkAnswer(int selectedButton) {
        if (currentExercise != null) {
            if (selectedButton == currentExercise.getImmagine_corretta()) {
                Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
                showConfettiEffect();
                if (successSound != null) {
                    successSound.start();
                    updateCoinsInFirebase();
                    Button btnButton = findViewById(R.id.button1);
                    btnButton.setEnabled(false);
                    Button btnButton_2 = findViewById(R.id.button2);
                    btnButton_2.setEnabled(false);
                }
            } else {
                Toast.makeText(this, "Incorrect. Try again!", Toast.LENGTH_SHORT).show();
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
                        .build());
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
                                        .collection("tipo2")
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