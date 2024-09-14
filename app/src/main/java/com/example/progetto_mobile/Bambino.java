package com.example.progetto_mobile;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class Bambino extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bambino);

        String bambinoIdraw = getIntent().getStringExtra("bambinoId");
        int lastSlashIndex = bambinoIdraw.lastIndexOf('/');
        String bambinoId = bambinoIdraw.substring(lastSlashIndex + 1);

        // Create a Bundle to hold the arguments
        Bundle bundle = new Bundle();
        bundle.putString("bambinoId", bambinoId);

        // Create a new instance of the Fragment and set the arguments
        HomeBambinoFragment homeBambinoFragment = new HomeBambinoFragment();
        homeBambinoFragment.setArguments(bundle);

        // Load the Fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeBambinoFragment)
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

