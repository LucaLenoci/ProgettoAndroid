package com.example.progetto_mobile;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class GenitoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.genitore);

        String genitorePath = getIntent().getStringExtra("genitore");

        // Create a Bundle to hold the arguments
        Bundle bundle = new Bundle();
        bundle.putString("genitore", genitorePath);

        // Create a new instance of the Fragment and set the arguments
        HomeGenitoreFragment homeGenitoreFragment = new HomeGenitoreFragment();
        homeGenitoreFragment.setArguments(bundle);

        // Load the Fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeGenitoreFragment)
                    .commit();
        }
    }

    // Metodo per gestire la sostituzione dei Fragment
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // Aggiungi alla backstack per consentire il "Back" corretto
        transaction.commit();
    }
}

