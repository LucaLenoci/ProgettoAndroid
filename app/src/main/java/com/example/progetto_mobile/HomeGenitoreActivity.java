package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

public class HomeGenitoreActivity extends AppCompatActivity {

    private static final String TAG = "HomeGenitoreActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_genitore);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvBenvenuto = findViewById(R.id.textViewBenvenutoGenitore);
        Button btnGestisciTemi = findViewById(R.id.buttonGestisciTemi);

        String genitorePath = getIntent().getStringExtra("genitore");

        if (genitorePath != null) {
            FirebaseFirestore.getInstance().document(genitorePath).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + documentSnapshot.getData());
                            String nome = documentSnapshot.getString("nome");
                            String cognome = documentSnapshot.getString("cognome");
                            tvBenvenuto.setText(String.format("Benvenuto/a, %s %s", nome, cognome));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Errore nel recupero dei dati del genitore", e);
                        Toast.makeText(HomeGenitoreActivity.this, "Errore nel recupero dei dati del genitore", Toast.LENGTH_SHORT).show();
                    });
        }

        BambiniListFragment bambiniListFragment = BambiniListFragment.newInstance(genitorePath);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerBambini, bambiniListFragment)
                .commit();

        btnGestisciTemi.setOnClickListener(v ->
                startActivity(new Intent(HomeGenitoreActivity.this, GestisciTemiActivity.class)));
    }
}