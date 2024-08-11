package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeGenitoreActivity extends AppCompatActivity {

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
        Button btnCorreggiEsercizi = findViewById(R.id.buttonCorreggiEsercizi);

        Intent intent = getIntent();
        User user = (User) intent.getSerializableExtra("user");
        if (user != null) {
            String text = "Benvenuto/a, " + user.getNome() +" "+ user.getCognome();
            tvBenvenuto.setText(text);
        }

        btnGestisciTemi.setOnClickListener(v ->
                startActivity(new Intent(HomeGenitoreActivity.this, GestisciTemiActivity.class)));

        btnCorreggiEsercizi.setOnClickListener(v ->
                startActivity(new Intent(HomeGenitoreActivity.this, CorreggiEserciziActivity.class)));
    }

}
