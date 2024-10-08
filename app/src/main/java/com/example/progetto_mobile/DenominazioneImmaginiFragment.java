package com.example.progetto_mobile;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Environment;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.assemblyai.api.resources.files.types.UploadedFile;
import com.assemblyai.api.resources.transcripts.requests.TranscriptParams;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.xml.KonfettiView;

import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.transcripts.types.*;

public class DenominazioneImmaginiFragment extends Fragment implements TextToSpeech.OnInitListener {
    private static final String TAG = "DenominazioneImmaginiFragment";
    private TextView tvText;
    private TextToSpeech tts;
    private FirebaseFirestore db;
    private EsercizioTipo1 currentExercise;
    private boolean isSuggestion1Used = false;
    private boolean isSuggestion2Used = false;
    private boolean isSuggestion3Used = false;

    private boolean isRecording = false;
    private MediaRecorder mediaRecorder;
    private String fileName;
    private ImageButton btnButton;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference imagesRef = storage.getReferenceFromUrl("gs://progetto-mobile-24.appspot.com/immagini");


    private String selectedDate;
    private String bambinoId;
    private KonfettiView konfettiView;
    private MediaPlayer successSound;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.denominazione_immagini, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectedDate = getArguments().getString("selectedDate");
        bambinoId = getArguments().getString("bambinoId");

        // Initialize Firestore and other components
        db = FirebaseFirestore.getInstance();
        konfettiView = view.findViewById(R.id.konfettiView);
        successSound = MediaPlayer.create(getContext(), R.raw.success_sound);

        fetchTema();
        fetchExerciseData();

        // Button and TTS initialization for suggestion 1
        Button speakButton = view.findViewById(R.id.suggerimento1);
        speakButton.setOnClickListener(v -> {
            String textToSpeak = currentExercise.getSuggerimento();
            if (!isSuggestion1Used) {
                updateSuggerimentiUsatiInFirebase(0);
                isSuggestion1Used = true;
            }
            speakText(textToSpeak);
        });

        // Button and TTS initialization for suggestion 2
        Button speakButton_2 = view.findViewById(R.id.suggerimento2);
        speakButton_2.setOnClickListener(v -> {
            String textToSpeak = currentExercise.getSuggerimento2();
            if (!isSuggestion2Used) {
                updateSuggerimentiUsatiInFirebase(1);
                isSuggestion2Used = true;
            }
            speakText(textToSpeak);
        });

        // Button and TTS initialization for suggestion 3
        Button speakButton_3 = view.findViewById(R.id.suggerimento3);
        speakButton_3.setOnClickListener(v -> {
            String textToSpeak = currentExercise.getSuggerimento3();
            if (!isSuggestion3Used) {
                updateSuggerimentiUsatiInFirebase(2);
                isSuggestion3Used = true;
            }
            speakText(textToSpeak);
        });

        tts = new TextToSpeech(getContext(), this);

        btnButton = view.findViewById(R.id.btn_button);
        tvText = view.findViewById(R.id.tv_text);

        // Check and request permissions
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 1);
        }

        btnButton.setOnClickListener(v -> Input());
    }
    private void Input() {
        if (!isRecording) {
            // Start recording
            startRecording();

            isRecording = true;
        } else {
            // Stop recording
            stopRecording();

            isRecording = false;
        }
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


    private void startRecording() {
        fileName = getContext().getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.mp3";
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(fileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        mediaRecorder.start();
        requireView().findViewById(R.id.btn_button).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFB31717")));
    }



    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        requireView().findViewById(R.id.loading).setVisibility(View.VISIBLE);
        requireView().findViewById(R.id.btn_button).setClickable(false);
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        int colorPrimary = typedValue.data;

        requireView().findViewById(R.id.btn_button).setBackgroundTintList(ColorStateList.valueOf(colorPrimary));
        transcribeAudioFile();
    }


    private void transcribeAudioFile() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                AssemblyAI client = AssemblyAI.builder()
                        .apiKey("62238600cbcd4e79ac03446267d27a16")
                        .build();

                File file = new File(fileName);
                byte[] fileBytes = readFileToByteArray(file);

                UploadedFile uploadedFile = client.files().upload(fileBytes);
                String fileUrl = uploadedFile.getUploadUrl();

                Transcript transcript = client.transcripts().transcribe(fileUrl);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Result(transcript.getText().get()));
                }            } catch (Exception e) {
                Log.e(TAG, "Transcription failed", e);
            }
        });
    }

    private byte[] readFileToByteArray(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] fileBytes = new byte[(int) file.length()];
            fis.read(fileBytes);
            return fileBytes;
        }
    }

    protected void Result(String text) {
        String recognizedText = text.toLowerCase();

        // Remove the last character if the string length is greater than 0
        if (recognizedText.length() > 0) {
            recognizedText = recognizedText.substring(0, recognizedText.length() - 1);
        }

        getView().findViewById(R.id.loading).setVisibility(View.INVISIBLE);
        tvText.setText(recognizedText);

        if (currentExercise != null && recognizedText.equalsIgnoreCase(currentExercise.getRisposta_corretta())) {
            Toast.makeText(getContext(), "Corretto!", Toast.LENGTH_SHORT).show();
            showConfettiEffect();

            disableButtons();

            if (successSound != null) {
                successSound.start();
            }

            // Aggiungi un handler per ritornare alla pagina precedente dopo l'animazione
            new Handler().postDelayed(() -> {
                updateCoinsInFirebase();
                uploadAudioToFirebase();
                incrementTentativiInFirebase();
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                }            }, 3000); // Aspetta 3 secondi (modifica se necessario per adattare la durata dell'animazione)

        } else {
            Toast.makeText(getContext(), "Riprova!", Toast.LENGTH_SHORT).show();
            incrementTentativiInFirebase();
            requireView().findViewById(R.id.btn_button).setClickable(true);
        }

    }

    private void disableButtons() {
        requireView().findViewById(R.id.btn_button).setEnabled(false);
        requireView().findViewById(R.id.suggerimento1).setEnabled(false);
        requireView().findViewById(R.id.suggerimento2).setEnabled(false);
        requireView().findViewById(R.id.suggerimento3).setEnabled(false);
    }

    private void uploadAudioToFirebase() {
        StorageReference audioRef = storage.getReference().child("audio/" + bambinoId + "/tipo1/" + selectedDate);
        Uri fileUri = Uri.fromFile(new File(fileName));

        // Upload the file to Firebase Storage
        audioRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL of the uploaded audio
                    audioRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String audioDownloadUrl = uri.toString();
                        Log.d("Firebase Storage", "Audio file uploaded successfully: " + audioDownloadUrl);
                        // Now save the reference to Firestore
                        saveAudioReferenceToFirestore(audioDownloadUrl);
                    }).addOnFailureListener(exception -> {
                        Log.e("Firebase Storage", "Failed to get audio download URL", exception);
                    });
                })
                .addOnFailureListener(exception -> {
                    Log.e("Firebase Storage", "Audio upload failed", exception);
                });
    }

    private void saveAudioReferenceToFirestore(String audioUrl) {
        // Update the Firestore document for the exercise with the audio URL
        db.collection("esercizi")
                .document(bambinoId)
                .collection("tipo1")
                .document(selectedDate)
                .update("audio_url", audioUrl)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Audio URL saved to Firestore successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error saving audio URL to Firestore", e);
                });
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


    private void displayImage(String imageUrl) {
        ImageView imageView = requireView().findViewById(R.id.imageView);

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
    public void onDestroy() {
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
        ImageView imageView = requireView().findViewById(R.id.imageView8); // Your ImageView containing round_rect

        int startColor = 0;
        int centerColor = 0;
        int endColor = 0;

        // Set colors based on the theme
        switch (theme) {
            case "supereroi":
            case "cartoni_animati":
                startColor = ContextCompat.getColor(requireContext(), R.color.supereroi1);
                centerColor = ContextCompat.getColor(requireContext(), R.color.supereroi2);
                endColor = ContextCompat.getColor(requireContext(), R.color.supereroi3);
                break;
            case "favole":
            case "videogiochi":
                startColor = ContextCompat.getColor(requireContext(), R.color.videogiochi1);
                centerColor = ContextCompat.getColor(requireContext(), R.color.videogiochi2);
                endColor = ContextCompat.getColor(requireContext(), R.color.videogiochi3);
                break;
        }

        // Update the drawable with the new colors
        GradientDrawable gradientDrawable = (GradientDrawable) imageView.getBackground();
        gradientDrawable.setColors(new int[]{startColor, centerColor, endColor});
        imageView.setBackground(gradientDrawable);
    }

    public void updateConstraintLayoutBackground(String theme) {
        ConstraintLayout constraintLayout = requireView().findViewById(R.id.constraintLayout);// Your ConstraintLayout

        int backgroundColor = 0;

        // Set background color based on the theme
        switch (theme) {
            case "supereroi":
            case "cartoni_animati":
                backgroundColor = ContextCompat.getColor(requireContext(), R.color.supereroibackground); // Replace with actual color resource
                break;
            case "favole":
            case "videogiochi":
                backgroundColor = ContextCompat.getColor(requireContext(), R.color.videogiochibackground); // Replace with actual color resource
                break;
        }

        // Apply the background color to the ConstraintLayout
        constraintLayout.setBackgroundColor(backgroundColor);
    }
}

