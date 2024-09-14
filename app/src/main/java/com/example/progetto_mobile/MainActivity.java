package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LandingFragment landingFragment = new LandingFragment();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_2, landingFragment)
                    .commit();
        }

        // Check for intent extras
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("from")) {
            String from = intent.getStringExtra("from");
            String logopedistaPath = intent.getStringExtra("logopedistaPath");
            if ("registraGenitore".equals(from)) {
                // Show RegisterFragment with arguments
                RegisterFragment registerFragment = new RegisterFragment();
                Bundle args = new Bundle();
                args.putString("from", "registraGenitore");
                args.putString("logopedistaPath", logopedistaPath);
                registerFragment.setArguments(args);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_2, registerFragment)
                        .addToBackStack(null)
                        .commit();
            }
        }
    }
}
