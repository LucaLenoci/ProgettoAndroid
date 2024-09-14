package com.example.progetto_mobile;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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

public class RiconoscimentoCoppieMinimeFragment extends Fragment implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private static final String TAG = "RiconoscimentoCoppieMinimeFragment";
    private FirebaseFirestore db;
    private EsercizioTipo2 currentExercise;
    private KonfettiView konfettiView;
    private MediaPlayer successSound;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference imagesRef = storage.getReferenceFromUrl("gs://progetto-mobile-24.appspot.com/immagini");

    private String selectedDate;
    private String bambinoId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.riconoscimento_coppie_minime, container, false);

        selectedDate = getArguments().getString("selectedDate");
        bambinoId = getArguments().getString("bambinoId");
        konfettiView = view.findViewById(R.id.konfettiView);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        fetchTema();
        fetchExerciseData();

        ImageButton speakButton = view.findViewById(R.id.speak_button);
        speakButton.setOnClickListener(v -> {
            String textToSpeak = currentExercise.getRisposta_corretta();
            tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
        });


        tts = new TextToSpeech(getContext(), this);
        float speechRate = 1.0f;
        tts.setSpeechRate(speechRate);

        ImageButton button1 = view.findViewById(R.id.button1);
        ImageButton button2 = view.findViewById(R.id.button2);

        button1.setOnClickListener(v -> checkAnswer(1));
        button2.setOnClickListener(v -> checkAnswer(2));

        successSound = MediaPlayer.create(getContext(), R.raw.success_sound);

        return view;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.ITALIAN);
        }
    }

    @Override
    public void onDestroy() {
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
        ImageView imageView1 = getView().findViewById(R.id.image1);
        ImageView imageView2 = getView().findViewById(R.id.image2);

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
                Toast.makeText(getContext(), "Corretto!", Toast.LENGTH_SHORT).show();
                showConfettiEffect();

                disableButtons();

                if (successSound != null) {
                    successSound.start();
                }

                // Aggiungi un handler per ritornare alla pagina precedente dopo l'animazione
                new Handler().postDelayed(() -> {
                    updateCoinsInFirebase();
                    incrementTentativiInFirebase();
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    if (fragmentManager.getBackStackEntryCount() > 0) {
                        fragmentManager.popBackStack();
                    }                  }, 3000); // Aspetta 3 secondi (modifica se necessario per adattare la durata dell'animazione)

            } else {
                Toast.makeText(getContext(), "Sbagliato, riprova!", Toast.LENGTH_SHORT).show();
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
                            Long currentProgress= document.getLong("progresso");
                            if (currentCoins != null || currentProgress != null) {
                                // Increment the coins by 1
                                db.collection("bambini").document(bambinoId)
                                        .update("coins", currentCoins + 1)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Firestore", "Coins updated successfully.");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error updating coins", e);
                                        });
                                db.collection("bambini").document(bambinoId)
                                        .update("progresso", currentProgress + 1)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Firestore", "Progresso updated successfully.");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("Firestore", "Error updating Progresso", e);
                                        });

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
        getView().findViewById(R.id.speak_button).setEnabled(false);
        getView().findViewById(R.id.button1).setEnabled(false);
        getView().findViewById(R.id.button2).setEnabled(false);
    }

    private void fetchTema() {
        db.collection("bambini").document(bambinoId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String temaCorrente = document.getString("tema");
                            updateRoundRectColors(temaCorrente);
                            updateConstraintLayoutBackground(temaCorrente);
                            updateLinearLayoutBackground(temaCorrente);
                        } else {
                            Log.e(TAG, "No such document for " + bambinoId);
                        }
                    } else {
                        Log.e(TAG, "Firestore get failed for " + bambinoId, task.getException());
                    }
                });
    }

    public void updateRoundRectColors(String theme) {
        ImageView imageView = getView().findViewById(R.id.imageView6); // Your ImageView containing round_rect

        int startColor = 0;
        int centerColor = 0;
        int endColor = 0;

        // Set colors based on the theme
        switch (theme) {
            case "supereroi":
            case "cartoni_animati":
                startColor = ContextCompat.getColor(getContext(), R.color.supereroi1);
                centerColor = ContextCompat.getColor(getContext(), R.color.supereroi2);
                endColor = ContextCompat.getColor(getContext(), R.color.supereroi3);
                break;
            case "favole":
            case "videogiochi":
                startColor = ContextCompat.getColor(getContext(), R.color.videogiochi1);
                centerColor = ContextCompat.getColor(getContext(), R.color.videogiochi2);
                endColor = ContextCompat.getColor(getContext(), R.color.videogiochi3);
                break;
        }

        // Update the drawable with the new colors
        GradientDrawable gradientDrawable = (GradientDrawable) imageView.getBackground();
        gradientDrawable.setColors(new int[]{startColor, centerColor, endColor});
        imageView.setBackground(gradientDrawable);
    }

    public void updateConstraintLayoutBackground(String theme) {
        ConstraintLayout constraintLayout = getView().findViewById(R.id.constraintLayout2);// Your ConstraintLayout

        int backgroundColor = 0;

        // Set background color based on the theme
        switch (theme) {
            case "supereroi":
            case "cartoni_animati":
                backgroundColor = ContextCompat.getColor(getContext(), R.color.supereroibackground); // Replace with actual color resource
                break;
            case "favole":
            case "videogiochi":
                backgroundColor = ContextCompat.getColor(getContext(), R.color.videogiochibackground); // Replace with actual color resource
                break;
        }

        // Apply the background color to the ConstraintLayout
        constraintLayout.setBackgroundColor(backgroundColor);
    }

    public void updateLinearLayoutBackground(String theme) {
        LinearLayout linearLayout = getView().findViewById(R.id.linearLayout);// Your ConstraintLayout

        int backgroundColor = 0;

        // Set background color based on the theme
        switch (theme) {
            case "supereroi":
            case "cartoni_animati":
                backgroundColor = ContextCompat.getColor(getContext(), R.color.supereroibackground); // Replace with actual color resource
                break;
            case "favole":
            case "videogiochi":
                backgroundColor = ContextCompat.getColor(getContext(), R.color.videogiochibackground); // Replace with actual color resource
                break;
        }

        // Apply the background color to the ConstraintLayout
        linearLayout.setBackgroundColor(backgroundColor);
    }

}

