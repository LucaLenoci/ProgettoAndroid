package com.example.progetto_mobile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DashboardGenitoreActivity extends AppCompatActivity {

    private static final String TAG = "DashboardGenitoreActivity";
    private String genitorePath = "", logopedistaPath = "";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard_genitore);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        Genitore genitore = (Genitore) getIntent().getSerializableExtra("genitore");
        Log.d(TAG, "Genitore preso: " + genitore);
        if (genitore != null) {
            genitorePath = genitore.getGenitoreRef();
            showBambiniList();
        }

        logopedistaPath = getIntent().getStringExtra("logopedista");

        Button btnRegistraBambino = findViewById(R.id.buttonRegistraBambino);
        if (genitore != null && logopedistaPath != null) {
            btnRegistraBambino.setOnClickListener(v -> showRegistraBambinoDialog());
        }
    }

    private void showBambiniList() {
        BambiniListFragment bambiniListFragment = BambiniListFragment.newInstance(genitorePath);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerBambini, bambiniListFragment)
                .commit();
    }

    private void showRegistraBambinoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Registra Bambino");

        View view = getLayoutInflater().inflate(R.layout.layout_dialog_registra_bambino, null);

        EditText etNome = view.findViewById(R.id.editTextNome);
        EditText etCognome = view.findViewById(R.id.editTextCognome);
        EditText etEta = view.findViewById(R.id.editTextEta);
        RadioGroup radioGroupSesso = view.findViewById(R.id.radioGroupSesso);

        builder.setView(view);

        builder.setPositiveButton("Registra", (dialog, which) -> {
                    String nome = etNome.getText().toString();
                    String cognome = etCognome.getText().toString();
                    String eta = etEta.getText().toString();
                    String sesso = "";

                    int selectedId = radioGroupSesso.getCheckedRadioButtonId();
                    if (selectedId == R.id.radioButtonMaschio) sesso = "M";
                    else if (selectedId == R.id.radioButtonFemmina) sesso = "F";

                    db = FirebaseFirestore.getInstance();
                    if (!nome.isEmpty() && !cognome.isEmpty() && !eta.isEmpty() && !sesso.isEmpty()) {
                        Map<String, Object> child = new HashMap<>();
                        child.put("nome", nome);
                        child.put("cognome", cognome);
                        child.put("eta", Integer.parseInt(eta));
                        child.put("sesso", sesso);
                        child.put("tipologia", 0);
                        child.put("avatarCorrente", "");
                        child.put("progresso", 0);
                        child.put("coins", 0);
                        child.put("esercizioTipo1", db.document("/esercizi/placeholder/tipo1/16-08-2024")); //placeholder
                        child.put("esercizioTipo2", db.document("/esercizi/placeholder/tipo2/16-08-2024")); //placeholder
                        child.put("esercizioTipo3", db.document("/esercizi/placeholder/tipo3/16-08-2024")); //placeholder
                        ChildHelper.addBambinoToFirestore(child, genitorePath, logopedistaPath)
                                .addOnSuccessListener(task -> {
                                    Toast.makeText(DashboardGenitoreActivity.this, "Bambino registrato con successo!", Toast.LENGTH_SHORT).show();
                                    showBambiniList();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(DashboardGenitoreActivity.this, "Errore nella registrazione del bambino", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Errore aggiungendo il bambino: ", e);
                                });
                    } else {
                        Toast.makeText(DashboardGenitoreActivity.this, "Inserisci tutti i dati", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annulla", (dialog, which) -> dialog.cancel())
                .show();
    }
}