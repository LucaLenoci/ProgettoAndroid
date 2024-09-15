package com.example.progetto_mobile;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class LogopedistaActivity extends AppCompatActivity {

    private static final String TAG = "LogopedistaActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logopedista);

        String logopedistaPath = getIntent().getStringExtra("logopedista");

        // Create a Bundle to hold the arguments
        Bundle bundle = new Bundle();
        bundle.putString("logopedista", logopedistaPath);

        // Create a new instance of the Fragment and set the arguments
        HomeLogopedistaFragment homeLogopedistaFragment = HomeLogopedistaFragment.newInstance(logopedistaPath);
        homeLogopedistaFragment.setArguments(bundle);

        // Load the Fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeLogopedistaFragment)
                    .commit();
        }

        Log.d(TAG, "onCreate");
    }

    // Metodo per gestire la sostituzione dei Fragment
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // Aggiungi alla backstack per consentire il "Back" corretto
        transaction.commit();
    }

    public void closeExerciseEditFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof DashboardBambinoFragment) {
            ((DashboardBambinoFragment) currentFragment).closeExerciseEditFragment();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    public void updateExercise(String exerciseType, Object newExercise) {
        DashboardBambinoFragment dashboardFragment = (DashboardBambinoFragment) getSupportFragmentManager().findFragmentByTag("DASHBOARD_FRAGMENT_TAG");
        if (dashboardFragment != null) {
            dashboardFragment.updateExercise(exerciseType, newExercise);
        }
    }

    public void addExercise(String exerciseType, Object newExercise) {
        DashboardBambinoFragment dashboardFragment = (DashboardBambinoFragment) getSupportFragmentManager().findFragmentByTag("DASHBOARD_FRAGMENT_TAG");
        if (dashboardFragment != null) {
            dashboardFragment.addExercise(exerciseType, newExercise);
        }
    }

    public void handleExerciseResult(String exerciseType, Object newExercise, boolean isEditing) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof DashboardBambinoFragment) {
            DashboardBambinoFragment dashboardFragment = (DashboardBambinoFragment) currentFragment;
            if (isEditing) {
                dashboardFragment.updateExercise(exerciseType, newExercise);
            } else {
                dashboardFragment.addExercise(exerciseType, newExercise);
            }
        }
        // Non rimuovere immediatamente il Fragment
        // getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}

