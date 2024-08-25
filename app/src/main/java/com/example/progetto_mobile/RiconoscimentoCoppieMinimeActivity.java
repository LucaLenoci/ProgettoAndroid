package com.example.progetto_mobile;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class RiconoscimentoCoppieMinimeActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private FirebaseFirestore db;
    private EsercizioTipo2 currentExercise;
    private KonfettiView konfettiView;
    private MediaPlayer successSound;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference imagesRef = storage.getReferenceFromUrl("gs://progetto-mobile-24.appspot.com/immagini");

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

        ImageButton speakButton = findViewById(R.id.speak_button);
        speakButton.setOnClickListener(v -> {
            String textToSpeak = currentExercise.getRisposta_corretta();
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
        });


        tts = new TextToSpeech(this, this);
        float speechRate = 1.0f;
        tts.setSpeechRate(speechRate);

        ImageButton button1 = findViewById(R.id.button1);
        ImageButton button2 = findViewById(R.id.button2);

        button1.setOnClickListener(v -> checkAnswer(1));
        button2.setOnClickListener(v -> checkAnswer(2));

        successSound = MediaPlayer.create(this, R.raw.success_sound);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.ITALIAN);
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
        db.collection("esercizi")
                .document(bambinoId)
                .collection("tipo2")
                .document(selectedDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            currentExercise = document.toObject(EsercizioTipo2.class);
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
        String imageFileName1 = exercise.getRisposta_corretta() + ".png";
        String imageFileName2 = exercise.getRisposta_sbagliata() + ".png";

        StorageReference imageRef1 = imagesRef.child(imageFileName1);
        StorageReference imageRef2 = imagesRef.child(imageFileName2);

        imageRef1.getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl1 = uri.toString();
            imageRef2.getDownloadUrl().addOnSuccessListener(uri2 -> {
                String imageUrl2 = uri2.toString();
                displayImages(imageUrl1, imageUrl2);
            }).addOnFailureListener(exception -> Log.e("Firebase Storage", "Failed to get download URL", exception));
        }).addOnFailureListener(exception -> Log.e("Firebase Storage", "Failed to get download URL", exception));
    }

    private void displayImages(String imageUrl1, String imageUrl2) {
        ImageView imageView1 = findViewById(R.id.image1);
        ImageView imageView2 = findViewById(R.id.image2);

        if (currentExercise.getImmagine_corretta() == 1) {
            Glide.with(this).load(imageUrl1).into(imageView1);
            Glide.with(this).load(imageUrl2).into(imageView2);
        } else {
            Glide.with(this).load(imageUrl2).into(imageView1);
            Glide.with(this).load(imageUrl1).into(imageView2);
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
                    incrementTentativiInFirebase();
                    disableButtons();
                }
            } else {
                Toast.makeText(this, "Incorrect. Try again!", Toast.LENGTH_SHORT).show();
                incrementTentativiInFirebase();
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
                                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "Coins updated successfully."))
                                        .addOnFailureListener(e -> Log.e("Firestore", "Error updating coins", e));

                                db.collection("esercizi")
                                        .document(bambinoId)
                                        .collection("tipo2")
                                        .document(selectedDate)
                                        .update("esercizio_corretto", true)
                                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "Esercizio corretto updated successfully."))
                                        .addOnFailureListener(e -> Log.e("Firestore", "Error updating esercizio corretto", e));
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
                .collection("tipo2")
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
                                    .collection("tipo2")
                                    .document(selectedDate)
                                    .update("tentativi", currentTentativi + 1)
                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Tentativi updated successfully."))
                                    .addOnFailureListener(e -> Log.e("Firestore", "Error updating tentativi", e));
                        }
                    } else {
                        Log.d("Firestore", "Failed to fetch document: ", task.getException());
                    }
                });
    }

    private void disableButtons() {
        findViewById(R.id.button1).setEnabled(false);
        findViewById(R.id.button2).setEnabled(false);
    }
}
