package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordFragment extends Fragment {

    private FirebaseAuth mAuth;
    private static final String TAG = "ResetPassword";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_reset_password, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Get references to UI elements
        EditText emailField = view.findViewById(R.id.editTextResetEmail);
        View resetButton = view.findViewById(R.id.buttonSubmitReset);
        View backButton = view.findViewById(R.id.buttonBack);

        // Set onClickListener for Reset Password button
        resetButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            if (!email.isEmpty()) {
                resetPassword(email);
            } else {
                Toast.makeText(getContext(), "Please enter your email", Toast.LENGTH_SHORT).show();
            }
        });

        // Set onClickListener for Back button
        backButton.setOnClickListener(v -> {
            // Navigate back to the LoginActivity
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getContext(), MainActivity.class));
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            }        });
        return view;
    }

    // Method to send a password reset email
    private void resetPassword(String emailAddress) {
        mAuth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            Toast.makeText(getContext(), "Password reset email sent.", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w(TAG, "sendPasswordResetEmail:failure", task.getException());
                            Toast.makeText(getContext(), "Failed to send reset email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
