package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public class LandingFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_main, container, false);

        View accediButton = view.findViewById(R.id.buttonAccedi);
        accediButton.setOnClickListener(v -> {
            Fragment fragment;
            fragment = new LoginFragment();
            if (fragment != null) {

                if (getParentFragmentManager() != null) {
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container_2, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        View registratiButton = view.findViewById(R.id.buttonRegistrati);
        registratiButton.setOnClickListener(v -> {
            // Start the registration activity
            Fragment fragment;
            fragment = new RegisterFragment();
            Bundle args = new Bundle();
            args.putString("from", "registraLogopedista");
            fragment.setArguments(args);

            if (fragment != null) {

                if (getParentFragmentManager() != null) {
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container_2, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        return view;
    }
}


