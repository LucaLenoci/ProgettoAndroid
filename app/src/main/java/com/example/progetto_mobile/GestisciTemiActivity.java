package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

// TODO: onLongClickListener su un tema ti fa rinominare e eliminare il tema
public class GestisciTemiActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private LinearLayout linearLayoutTemi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gestisci_temi);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        linearLayoutTemi = findViewById(R.id.linearLayoutTemi);
        Button buttonAggiungiTema = findViewById(R.id.buttonAggiungiTema);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null) {
                StorageReference userFolderRef = storage.getReference().child(email);
                checkAndCreateUserFolder(userFolderRef);
                listUserThemes(userFolderRef);
            }
        }

        buttonAggiungiTema.setOnClickListener(v -> showAddThemeDialog());
    }

    private void checkAndCreateUserFolder(StorageReference userFolderRef) {
        userFolderRef.listAll().addOnSuccessListener(listResult -> {
            if (listResult.getItems().isEmpty() && listResult.getPrefixes().isEmpty()) {
                userFolderRef.child(".temp").putBytes(new byte[0]); // Crea una cartella vuota.
            }
        });
    }

    // TODO: togliere il textView 'aggiungi un tema' quando ci sono 0 temi e ne aggiungi uno
    private void listUserThemes(StorageReference userFolderRef) {
        userFolderRef.listAll().addOnSuccessListener(listResult -> {
            List<StorageReference> themeFolders = listResult.getPrefixes();
            if (themeFolders.isEmpty()) {
                TextView noThemesView = new TextView(this);
                noThemesView.setText("Aggiungi un tema!");
                linearLayoutTemi.addView(noThemesView);
            } else {
                for (StorageReference themeFolder : themeFolders) {
                    addThemeTextView(themeFolder.getName());
                }
            }
        });
    }

    private void addThemeTextView(String themeName) {
        TextView themeTextView = new TextView(this);
        themeTextView.setText(themeName);
        themeTextView.setPadding(16, 16, 16, 16);
        themeTextView.setOnClickListener(v -> openThemeFolder(themeName));
        linearLayoutTemi.addView(themeTextView);
    }

    private void openThemeFolder(String themeName) {
        // Logica per aprire la cartella tema e visualizzare il contenuto.
        Intent intent = new Intent(GestisciTemiActivity.this, GestisciSingoloTemaActivity.class);
        intent.putExtra("themeName", themeName);
        startActivity(intent);
    }

    private void showAddThemeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Crea un nuovo tema");

        final EditText input = new EditText(this);
        input.setHint("Nome del tema");
        builder.setView(input);

        builder.setPositiveButton("Crea", (dialog, which) -> {
            String themeName = input.getText().toString();
            if (!themeName.isEmpty()) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null && currentUser.getEmail() != null) {
                    StorageReference userFolderRef = storage.getReference().child(currentUser.getEmail());
                    createThemeFolder(userFolderRef, themeName);
                }
            }
        });

        builder.setNegativeButton("Annulla", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void createThemeFolder(StorageReference userFolderRef, String themeName) {
        StorageReference newThemeFolder = userFolderRef.child(themeName + "/personaggi/.temp");
        newThemeFolder.putBytes(new byte[0]).addOnSuccessListener(taskSnapshot -> addThemeTextView(themeName));
    }
}
