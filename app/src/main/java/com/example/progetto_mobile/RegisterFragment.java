package com.example.progetto_mobile;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    private String from;
    private int tipologia;
    private static final String TAG = "FirestoreExample";
    private EditText etEmail, etPassword, etNome, etCognome, etEta;
    private FirebaseAuth auth;
    private FirebaseUser fbUser;
    private FirebaseFirestore db;
    private CollectionReference collectionRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View view = inflater.inflate(R.layout.activity_register, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvLabel = view.findViewById(R.id.textViewRegisterLabel);
        etNome = view.findViewById(R.id.editTextNome);
        etCognome = view.findViewById(R.id.editTextCognome);
        etEta = view.findViewById(R.id.editTextEta);
        etEmail = view.findViewById(R.id.editTextTextEmail);
        etPassword = view.findViewById(R.id.editTextTextPassword2);
        Button btnRegister = view.findViewById(R.id.buttonRegistrati2);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Handle arguments
        if (getArguments() != null) {
            from = getArguments().getString("from");
        }


        if (from != null && from.equals("registraLogopedista")){
            tvLabel.setText("Registrati come un nuovo logopedista");
            collectionRef = db.collection("logopedisti");
            tipologia = 2;
        }
        else if (from != null && from.equals("registraGenitore")){
            tvLabel.setText("Registra un genitore");
            collectionRef = db.collection("genitori");
            tipologia = 1;
        }

        Log.d("RegisterIntent", "Tipologia: " + from + tipologia);
        Log.d("RegisterIntent", "Collection: " + collectionRef.getPath());

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = etNome.getText().toString().trim();
                String cognome = etCognome.getText().toString().trim();
                String eta = etEta.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (nome.isEmpty() || cognome.isEmpty() || eta.isEmpty() || email.isEmpty() || password.isEmpty())
                    Toast.makeText(getContext(), "Empty credentials", Toast.LENGTH_SHORT).show();
                else if (password.length() < 6)
                    Toast.makeText(getContext(), "Password too short", Toast.LENGTH_SHORT).show();
                else registerUser(nome, cognome, eta, email, password);
            }
        });

        return view;
    }

    private void registerUser(String nome, String cognome, String eta, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
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
                            Toast.makeText(getContext(), "Error while registering user", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addUserToFirestore(String nome, String cognome, String eta, String email) {
        User user = null;
        if (tipologia == 1)
            user = new Genitore(nome, cognome, Integer.parseInt(eta), email, tipologia,
                    Collections.emptyList());
        else if (tipologia == 2)
            user = new Logopedista(nome, cognome, Integer.parseInt(eta), email, tipologia,
                    Collections.emptyList(), Collections.emptyList());

        // Aggiungi l'user a Firestore
        collectionRef
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot/Reference added with ID: " + documentReference.getId());
                        Log.d(TAG, "DocumentReference path: " + documentReference.getPath());
                        Toast.makeText(getContext(), "User registered successfully", Toast.LENGTH_SHORT).show();
                        addToGeneralUsersCollection(email, documentReference.getId());
                        if (from.equals("registraLogopedista")) {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(getContext(), MainActivity.class));
                            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                            if (fragmentManager.getBackStackEntryCount() > 0) {
                                fragmentManager.popBackStack();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        Toast.makeText(getContext(), "Error saving user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // TODO: togliere la funzione duplicata 'signIn'
    private void signIn(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
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
    }

    private void addToGeneralUsersCollection(String email, String documentId) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("infoRef", db.document(collectionRef.getPath().concat("/").concat(documentId)));
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentReference added with ID: " + documentReference.getId());
                        Log.d(TAG, "DocumentReference path: " + documentReference.getPath());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }
}