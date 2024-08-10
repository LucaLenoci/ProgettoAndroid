package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeBambinoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        User user =(User) intent.getSerializableExtra("user");

        setContentView(R.layout.home_bambino);

        TextView nomeField = findViewById(R.id.Nome);
        TextView cognomeField = findViewById(R.id.Cognome);
        TextView etaField = findViewById(R.id.Eta);

        nomeField.setText(user.getNome());
        cognomeField.setText(user.getCognome());
        etaField.setText(String.valueOf(user.getEta()));
    }
}
