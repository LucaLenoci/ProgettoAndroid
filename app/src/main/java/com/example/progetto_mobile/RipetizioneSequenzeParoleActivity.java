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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public class RipetizioneSequenzeParoleActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final int RQ_SPEECH_REC = 102;
    private TextView tvText;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference imagesRef = storage.getReferenceFromUrl("gs://progetto-mobile-24.appspot.com/lucaxl10@gmail.com/images");
    String fileName = "1.jpg";
    StorageReference imageRef = imagesRef.child(fileName);
    private TextToSpeech tts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ripetizione_sequenze_parole);  // Assuming you have a separate layout file

        Button btnButton = findViewById(R.id.btn_button_2);
        tvText = findViewById(R.id.tv_text_2);

        btnButton.setOnClickListener(v -> askSpeechInput());

        Button speakButton = findViewById(R.id.speak_button_2);
        speakButton.setOnClickListener(v -> {
            String textToSpeak = "Banana Ananas Limone";

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RQ_SPEECH_REC && resultCode == Activity.RESULT_OK) {
            ArrayList<String> result = data != null ? data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) : null;
            if (result != null && !result.isEmpty()) {
                tvText.setText(result.get(0));
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
}
