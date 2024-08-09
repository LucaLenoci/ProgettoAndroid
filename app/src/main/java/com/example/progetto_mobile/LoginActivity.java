package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "EmailPassword";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View accediButton = findViewById(R.id.buttonAccedi);
        EditText emailField = findViewById(R.id.editTextTextUsername);
        EditText passwordField = findViewById(R.id.editTextTextPassword);

        accediButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                signIn(email, password);
            } else {
                Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            }
        });

        // -- BOTTONI PER SIMULARE IL LOGIN --

        View bambinoButton = findViewById(R.id.buttonLoginBambino);
        bambinoButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, HomeBambinoActivity.class));
        });

        View genitoreButton = findViewById(R.id.buttonLoginGenitore);
        genitoreButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, HomeGenitoreActivity.class));
        });

//        View logopedistaButton = findViewById(R.id.buttonLoginLogopedista);
//        logopedistaButton.setOnClickListener(v -> {
//            startActivity(new Intent(LoginActivity.this, HomeLogopedistaActivity.class));
//        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        }
    }

    private void signIn(String email, String password) {
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
        // [END sign_in_with_email]
    }

    private void reload() { }

    private void updateUI(FirebaseUser user) {
        Intent intent;
        if (user != null) {
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            intent = new Intent(LoginActivity.this, DataAccess.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
        }
    }

}
