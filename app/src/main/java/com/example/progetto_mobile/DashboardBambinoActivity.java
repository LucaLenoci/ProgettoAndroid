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
            String listaEserciziTipo1 = "";
            String listaEserciziTipo2 = "";
            String listaEserciziTipo3 = "";

            for (int i = 0; i < child.getEserciziTipo1().size(); i++) {
                listaEserciziTipo1 = listaEserciziTipo1.concat("\nEs "+(i+1)+":\n").concat(child.getEserciziTipo1().get(i).toString());
                listaEserciziTipo1 = i == child.getEserciziTipo1().size() - 1
                        ? listaEserciziTipo1
                        : listaEserciziTipo1.concat("\n");
            }
            for (int i = 0; i < child.getEserciziTipo2().size(); i++) {
                listaEserciziTipo2 = listaEserciziTipo2.concat("\nEs "+(i+1)+":\n").concat(child.getEserciziTipo2().get(i).toString());
                listaEserciziTipo2 = i == child.getEserciziTipo2().size() - 1
                        ? listaEserciziTipo2
                        : listaEserciziTipo2.concat("\n");
            }
            for (int i = 0; i < child.getEserciziTipo3().size(); i++) {
                listaEserciziTipo3 = listaEserciziTipo3.concat("\nEs "+(i+1)+":\n").concat(child.getEserciziTipo3().get(i).toString());
                listaEserciziTipo3 = i == child.getEserciziTipo3().size() - 1
                        ? listaEserciziTipo3
                        : listaEserciziTipo3.concat("\n");
            }

            String text = String.format(Locale.ITALY,
                    "Nome: %s,\n" +
                            "Progresso: %d,\n" +
                            "Esercizi tipo 1: %s,\n" +
                            "Esercizi tipo 2: %s,\n" +
                            "Esercizi tipo 3: %s\n",
                    child.getNome(), child.getProgresso(), listaEserciziTipo1,
                    listaEserciziTipo2, listaEserciziTipo3);
            textView.setText(text);
        }
    }
}