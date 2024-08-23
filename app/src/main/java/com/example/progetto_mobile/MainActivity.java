package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View accediButton = findViewById(R.id.buttonAccedi);
        accediButton.setOnClickListener(v -> {
            // Start the login activity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

        View registratiButton = findViewById(R.id.buttonRegistrati);
        registratiButton.setOnClickListener(v -> {
            // Start the registration activity
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            intent.putExtra("from", "registraLogopedista");
            startActivity(intent);
        });
    }
}
