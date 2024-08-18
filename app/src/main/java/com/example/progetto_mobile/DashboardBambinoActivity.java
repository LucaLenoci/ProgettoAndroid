package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class DashboardBambinoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard_bambino);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView textView = findViewById(R.id.textView10);

        Intent intent = getIntent();
        Child child = (Child) intent.getSerializableExtra("child");
        if (child != null) {
            String text = String.format(Locale.ITALY,
                    "Nome: %s,\n" +
                            "Progresso: %d,\n" +
                            "Esercizi tipo 1: %s,\n" +
                            "Esercizi tipo 2: %s,\n" +
                            "Esercizi tipo 3: %s\n",
                    child.getNome(), child.getProgresso(), child.getEserciziTipo1().toString(),
                    child.getEserciziTipo2().toString(), child.getEserciziTipo3().toString());
            textView.setText(text);
        }
    }
}