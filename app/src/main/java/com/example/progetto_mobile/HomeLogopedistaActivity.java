package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeLogopedistaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_logopedista);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String logopedistaPath = getIntent().getStringExtra("logopedista");

        Button btnVediClassificaBambini = findViewById(R.id.buttonClassificaBambini);
        btnVediClassificaBambini.setOnClickListener(v -> {
            if (logopedistaPath == null)
                Toast.makeText(this, "Errore: logopedista non trovato", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeLogopedistaActivity.this, ClassificaBambiniActivity.class);
            intent.putExtra("logopedista", logopedistaPath);
            startActivity(intent);
        });

        Button btnRegistraGenitore = findViewById(R.id.buttonRegistraGenitore);
        btnRegistraGenitore.setOnClickListener(v -> {
            Intent intent = new Intent(HomeLogopedistaActivity.this, RegisterActivity.class);
            intent.putExtra("from", "registraGenitore");
            startActivity(intent);
        });
    }
}