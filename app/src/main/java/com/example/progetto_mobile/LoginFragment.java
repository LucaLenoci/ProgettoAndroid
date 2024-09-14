package com.example.progetto_mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private static final String TAG = "EmailPassword";
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String PREF_EMAIL = "savedemail";
    private static final String PREF_PASSWORD = "savedpassword";

    private EditText emailField;
    private EditText passwordField;
    private CheckBox rememberMeCheckbox;
    private View accediButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View view = inflater.inflate(R.layout.activity_login, container, false);

// Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // UI References
        emailField = view.findViewById(R.id.editTextTextEmail);
        passwordField = view.findViewById(R.id.editTextTextPassword);
        rememberMeCheckbox = view.findViewById(R.id.checkBoxRememberMe);
        accediButton = view.findViewById(R.id.buttonAccedi);

        // Load saved email if it exists


        accediButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                signIn(email, password);
            } else {
                Toast.makeText(getContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            }
        });

        loadSavedEmail();

        // Example buttons to simulate login for different users
        View bambinoButton = view.findViewById(R.id.buttonLoginBambino);


        View genitoreButton = view.findViewById(R.id.buttonLoginGenitore);
        genitoreButton.setOnClickListener(v -> startActivity(new Intent(getContext(), GenitoreActivity.class)));

        View accediButton = view.findViewById(R.id.buttonResetPassword);
        accediButton.setOnClickListener(v -> {
            Fragment fragment;
            fragment = new ResetPasswordFragment();
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

    @Override
    public void onStart() {
        super.onStart();
        // Check if the user is already logged in and update the UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            reload();
        }
    }

    // Method to handle user sign-in
    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (rememberMeCheckbox.isChecked()) {
                                saveLogin(email, password);
                            } else {
                                clearSavedLogin();
                            }
                            updateUI(user);
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void reload() {
        // Optionally reload the UI
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(getContext(), "Login Successful", Toast.LENGTH_SHORT).show();
            // Assuming you are in an Activity or Fragment
            DataAccess dataAccess = new DataAccess(getContext()); // Pass context if needed

// Retrieve the email from the field
            String email = emailField.getText().toString().trim();

// Call the method to get the collection
            dataAccess.getCollection(email);
        } else {
            Toast.makeText(getContext(), "Login Failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Save login to SharedPreferences
    private void saveLogin(String email, String password) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_EMAIL, email);
        editor.putString(PREF_PASSWORD, password);
        editor.apply();
    }

    // Load saved email from SharedPreferences
    private void loadSavedEmail() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString(PREF_EMAIL, null);
        String savedPassword = sharedPreferences.getString(PREF_PASSWORD, null);
        if (savedEmail != null) {
            emailField.setText(savedEmail);
            passwordField.setText(savedPassword);
            rememberMeCheckbox.setChecked(true);
            accediButton.performClick();
        }
    }

    // Clear saved login from SharedPreferences
    private void clearSavedLogin() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREF_EMAIL);
        editor.apply();
    }
}

