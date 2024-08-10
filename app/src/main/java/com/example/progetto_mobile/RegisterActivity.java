package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "FirestoreExample";
    private EditText etEmail, etPassword, etNome, etCognome, etEta;
    private FirebaseAuth auth;
    private FirebaseUser fbUser;
    private FirebaseFirestore db;
    private CollectionReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etNome = findViewById(R.id.editTextNome);
        etCognome = findViewById(R.id.editTextCognome);
        etEta = findViewById(R.id.editTextEta);
        etEmail = findViewById(R.id.editTextTextEmail);
        etPassword = findViewById(R.id.editTextTextPassword2);
        Button btnRegister = findViewById(R.id.buttonRegistrati2);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = etNome.getText().toString().trim();
                String cognome = etCognome.getText().toString().trim();
                String eta = etEta.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (nome.isEmpty() || cognome.isEmpty() || eta.isEmpty() || email.isEmpty() || password.isEmpty())
                    Toast.makeText(RegisterActivity.this, "Empty credentials", Toast.LENGTH_SHORT).show();
                else if (password.length() < 6)
                    Toast.makeText(RegisterActivity.this, "Password too short", Toast.LENGTH_SHORT).show();
                else registerUser(nome, cognome, eta, email, password);
            }
        });
    }

    private void registerUser(String nome, String cognome, String eta, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in per avere i permessi da Firestore
                            signIn(email, password);
                            fbUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (fbUser != null) {
                                Log.d(TAG, "User signed in");
                                addUserToFirestore(nome, cognome, eta, email);
                            } else {
                                Log.d(TAG, "Error signing in");
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error while registering user", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addUserToFirestore(String nome, String cognome, String eta, String email) {
        // Recupera l'ultimo ID disponibile
        usersRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int newId = 1; // Nel caso non ci siano utenti

                            // Trova l'ultimo ID
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                int currentId = Integer.parseInt(document.getId());
                                if (currentId >= newId)
                                    newId = currentId + 1;
                            }

                            // Crea un nuovo User
                            Map<String, Object> user = new HashMap<>();
                            user.put("nome", nome);
                            user.put("cognome", cognome);
                            user.put("eta", Integer.parseInt(eta));
                            user.put("email", email);
                            user.put("tipologia", 1);
//                            User user = new User(cognome, Integer.parseInt(eta), nome, 1, email);

                            // Aggiungi l'user a Firestore
                            usersRef.document(String.valueOf(newId))
                                    .set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
//                                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                            Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                                            FirebaseAuth.getInstance().signOut();
                                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error adding document", e);
                                            Toast.makeText(RegisterActivity.this, "Error saving user data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error retrieving users", Toast.LENGTH_SHORT).show();
                        }
                    }
        });
    }

    // TODO: togliere la funzione duplicata 'signIn'
    private void signIn(String email, String password) {
        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                        }
                    }
                });
        // [END sign_in_with_email]
    }
}