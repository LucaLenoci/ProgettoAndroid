package com.example.progetto_mobile;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class GestisciSingoloTemaActivity extends AppCompatActivity {

    private LinearLayout linearLayoutListaPersonaggi;
    private StorageReference themeFolderRef;
    private ImageView imageViewSfondo;

    private ActivityResultLauncher<Intent> pickImageSfondoLauncher;
    private ActivityResultLauncher<Intent> pickImagePersonaggioLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gestisci_singolo_tema);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Assicurati che i tuoi elementi abbiano l'ID corretto e siano presenti nel layout
        imageViewSfondo = findViewById(R.id.imageViewSfondo);
        ImageView imageViewModificaSfondo = findViewById(R.id.imageViewModificaSfondo);
        TextView textViewPersonaggio = findViewById(R.id.textViewAggiungiPersonaggio);
        linearLayoutListaPersonaggi = findViewById(R.id.linearLayoutListaPersonaggi);

        // Verifica che gli elementi non siano null prima di utilizzarli
        if (imageViewSfondo == null || textViewPersonaggio == null || linearLayoutListaPersonaggi == null) {
            throw new NullPointerException("Impossibile trovare uno o più elementi nel layout. Verifica gli ID e il layout XML.");
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            String themeName = getIntent().getStringExtra("themeName");
            themeFolderRef = storage.getReference().child(currentUser.getEmail()).child(themeName);
            getSfondo();
            listPersonaggi();
        }

        // Inizializza i launcher per l'activity di selezione immagine
        pickImageSfondoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            imageViewSfondo.setImageURI(imageUri);
                            uploadImageToFirebase(imageUri, "sfondo");
                        }
                    }
                });

        pickImagePersonaggioLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadNewPersonaggio(imageUri);
                        }
                    }
                });

        imageViewSfondo.setOnClickListener(v -> selectImageFromGallery(pickImageSfondoLauncher));
        imageViewModificaSfondo.setOnClickListener(v -> selectImageFromGallery(pickImageSfondoLauncher));
        textViewPersonaggio.setOnClickListener(v -> selectImageFromGallery(pickImagePersonaggioLauncher));
    }

    private void selectImageFromGallery(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcher.launch(intent);
    }

    private void uploadImageToFirebase(Uri imageUri, String fileName) {
        StorageReference fileRef = themeFolderRef.child(fileName);
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> Toast.makeText(GestisciSingoloTemaActivity.this, "Immagine caricata con successo!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(GestisciSingoloTemaActivity.this, "Errore durante il caricamento dell'immagine", Toast.LENGTH_SHORT).show());
    }

    private void uploadNewPersonaggio(Uri imageUri) {
        themeFolderRef.child("personaggi").listAll().addOnSuccessListener(listResult -> {
            int nextPersonaggioIndex = listResult.getItems().size();
            String fileName = "personaggio" + nextPersonaggioIndex;

            StorageReference fileRef = themeFolderRef.child("personaggi/" + fileName);
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(GestisciSingoloTemaActivity.this, "Personaggio caricato con successo!", Toast.LENGTH_SHORT).show();
                        listPersonaggi();  // Aggiorna la lista dei personaggi dopo il caricamento
                    })
                    .addOnFailureListener(e -> Toast.makeText(GestisciSingoloTemaActivity.this, "Errore durante il caricamento del personaggio", Toast.LENGTH_SHORT).show());
        });
    }

    private void getSfondo() {
        themeFolderRef.child("sfondo").getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this)
                    .load(uri)
                    .into(imageViewSfondo);
        }).addOnFailureListener(e -> Toast.makeText(GestisciSingoloTemaActivity.this,
                "Errore durante il recupero dell'immagine di sfondo", Toast.LENGTH_SHORT).show());
    }

    private void listPersonaggi() {
        linearLayoutListaPersonaggi.removeAllViews(); // Pulisce la lista esistente
        themeFolderRef.child("personaggi").listAll().addOnSuccessListener(listResult -> {
            for (StorageReference personaggioRef : listResult.getItems()) {
                if (personaggioRef.getName().equals(".temp"))
                    continue;
                addPersonaggioTextView(personaggioRef, personaggioRef.getName());
            }
        }).addOnFailureListener(e -> Toast.makeText(GestisciSingoloTemaActivity.this,
                "Errore durante il recupero dei personaggi", Toast.LENGTH_SHORT).show());
    }

    private void addPersonaggioTextView(StorageReference personaggioRef, String personaggioName) {
        TextView personaggioTextView = new TextView(this);
        personaggioTextView.setGravity(Gravity.CENTER_VERTICAL);
        personaggioTextView.setText(personaggioName);
        personaggioTextView.setPadding(16, 16, 16, 16);

        // TODO: aggiustare l'eliminazione (cambiare la logica di nominazione dei personaggi)
//        // Imposta un listener per i click prolungati
//        personaggioTextView.setOnLongClickListener(v -> {
//            // Crea un dialog di conferma
//            new AlertDialog.Builder(GestisciSingoloTemaActivity.this)
//                    .setTitle("Elimina personaggio")
//                    .setMessage("Sei sicuro di voler eliminare '" + personaggioName + "'?")
//                    .setPositiveButton("Sì", (dialog, which) -> {
//                        // Elimina il personaggio da Firebase Storage
//                        personaggioRef.delete()
//                                .addOnSuccessListener(aVoid -> {
//                                    Toast.makeText(GestisciSingoloTemaActivity.this, "Personaggio eliminato con successo!", Toast.LENGTH_SHORT).show();
//                                    listPersonaggi(); // Aggiorna la lista dopo l'eliminazione
//                                })
//                                .addOnFailureListener(e -> Toast.makeText(GestisciSingoloTemaActivity.this, "Errore durante l'eliminazione del personaggio", Toast.LENGTH_SHORT).show());
//                    })
//                    .setNegativeButton("No", null)
//                    .show();
//
//            return true; // Indica che l'evento è stato gestito
//        });

        personaggioRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this)
                    .asDrawable()
                    .load(uri)
                    .override(100, 100)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            personaggioTextView.setCompoundDrawablesWithIntrinsicBounds(resource, null, null, null);
                            personaggioTextView.setCompoundDrawablePadding(16);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);

                            Drawable defaultDrawable = ContextCompat.getDrawable(GestisciSingoloTemaActivity.this, R.drawable.error_outline_24);
                            personaggioTextView.setCompoundDrawablesWithIntrinsicBounds(defaultDrawable, null, null, null);
                        }
                    });
        }).addOnFailureListener(e -> Toast.makeText(GestisciSingoloTemaActivity.this,
                "Errore durante il recupero del personaggio " + personaggioName, Toast.LENGTH_SHORT).show());

        linearLayoutListaPersonaggi.addView(personaggioTextView);
    }

}
