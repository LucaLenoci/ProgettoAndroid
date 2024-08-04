package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View accediButton = findViewById(R.id.buttonAccedi);
        accediButton.setOnClickListener(v -> {
            Toast.makeText(this, "Test", Toast.LENGTH_SHORT).show();
//            // avvia l'activity in base all'utente del login
//            if (user.role == bambino)
//                startActivity(new Intent(LoginActivity.this, HomeBambinoActivity.class));
//            else if (user.role == genitore)
//                startActivity(new Intent(LoginActivity.this, HomeGenitoreActivity.class));
//            else if (user.role == logopedista)
//                startActivity(new Intent(LoginActivity.this, HomeLogopedistaActivity.class));
//            else
//                Toast.makeText(this, "Login non andato a buon fine", Toast.LENGTH_SHORT).show();
        });

        // -- BOTTONI PER SIMULARE IL LOGIN --

//        View bambinoButton = findViewById(R.id.buttonLoginBambino);
//        bambinoButton.setOnClickListener(v -> {
//            startActivity(new Intent(LoginActivity.this, HomeBambinoActivity.class));
//        });

        View genitoreButton = findViewById(R.id.buttonLoginGenitore);
        genitoreButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, HomeGenitoreActivity.class));
        });

//        View logopedistaButton = findViewById(R.id.buttonLoginLogopedista);
//        logopedistaButton.setOnClickListener(v -> {
//            startActivity(new Intent(LoginActivity.this, HomeLogopedistaActivity.class));
//        });
    }
}