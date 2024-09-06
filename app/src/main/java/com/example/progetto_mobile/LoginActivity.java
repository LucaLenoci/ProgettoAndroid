package com.example.progetto_mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "EmailPassword";
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String PREF_EMAIL = "savedemail";
    private static final String PREF_PASSWORD = "savedpassword";

    private EditText emailField;
    private EditText passwordField;
    private CheckBox rememberMeCheckbox;
    View accediButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // UI References
        emailField = findViewById(R.id.editTextTextEmail);
        passwordField = findViewById(R.id.editTextTextPassword);
        rememberMeCheckbox = findViewById(R.id.checkBoxRememberMe);
        accediButton = findViewById(R.id.buttonAccedi);

        // Load saved email if it exists


        accediButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                signIn(email, password);
            } else {
                Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            }
        });

        loadSavedEmail();

        // Example buttons to simulate login for different users
        View bambinoButton = findViewById(R.id.buttonLoginBambino);
        bambinoButton.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, HomeBambinoActivity.class)));

        View genitoreButton = findViewById(R.id.buttonLoginGenitore);
        genitoreButton.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, HomeGenitoreActivity.class)));
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
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
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
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, DataAccess.class);
            intent.putExtra("key", emailField.getText().toString().trim());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Save login to SharedPreferences
    private void saveLogin(String email, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_EMAIL, email);
        editor.putString(PREF_PASSWORD, password);
        editor.apply();
    }

    // Load saved email from SharedPreferences
    private void loadSavedEmail() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString(PREF_EMAIL, null);
        String savedPassword = sharedPreferences.getString(PREF_PASSWORD, null);
        if (savedEmail != null) {
            emailField.setText(savedEmail);
            passwordField.setText(savedPassword);
            accediButton.performClick();
        }
    }

    // Clear saved login from SharedPreferences
    private void clearSavedLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREF_EMAIL);
        editor.apply();
    }
}
