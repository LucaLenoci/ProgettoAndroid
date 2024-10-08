package com.example.progetto_mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ScegliAccountActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "ScegliAccountActivity";
    private LinearLayout linearLayoutAccountGenitore, linearLayoutAccounts;
    private TextView tvNomeGenitore;
    private List<User> bambiniList;

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String PREF_EMAIL = "savedemail";
    private static final String PREF_PASSWORD = "savedpassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scegli_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String genitorePath = getIntent().getStringExtra("genitore");

        tvNomeGenitore = findViewById(R.id.textViewNomeGenitore);
        linearLayoutAccountGenitore = findViewById(R.id.linearLayoutAccountGenitore);
        linearLayoutAccounts = findViewById(R.id.linearLayoutAccounts);

        if (genitorePath != null) {
            FirebaseFirestore.getInstance().document(genitorePath).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + documentSnapshot.getData());
                            String nome = documentSnapshot.getString("nome");
                            tvNomeGenitore.setText(nome);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Errore nel recupero dei dati del genitore", e);
                        Toast.makeText(ScegliAccountActivity.this, "Errore nel recupero dei dati del genitore", Toast.LENGTH_SHORT).show();
                    });

            ChildHelper.getBambiniRefsFromGenitorePath(genitorePath)
                    .addOnSuccessListener(this::processBambiniRefs)
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Errore nel recupero dei riferimenti dei bambini", e);
                        Toast.makeText(ScegliAccountActivity.this, "Errore nel recupero dei dati dei bambini", Toast.LENGTH_SHORT).show();
                    });
        }

        linearLayoutAccountGenitore.setOnClickListener(v ->
                showReauthenticationDialog(genitorePath));

        // Recupera il riferimento al pulsante di logout
        Button buttonLogout = findViewById(R.id.btnLogout);

        // Imposta il listener per il pulsante di logout
        ((View) buttonLogout).setOnClickListener(v -> {
            // Chiama il metodo per il logout
            logout();
        });
    }

    private void showReauthenticationDialog(String genitorePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reinserisci la tua password per accedere");

        // Crea un campo per inserire la password
        final EditText input = new EditText(this);
        input.setHint("Password");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//        input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(4) });
//        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Conferma", (dialog, which) -> {
            String password = input.getText().toString();
            if (!password.isEmpty()) {
                reauthenticateUser(password, genitorePath);
            } else Toast.makeText(ScegliAccountActivity.this, "Password necessaria", Toast.LENGTH_SHORT).show();

        }).setNegativeButton("Annulla", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void reauthenticateUser(String password, String genitorePath) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d(TAG, "Utente non riconosciuto");
            return;
        }

        String email = user.getEmail();
        if (email == null) {
            Log.d(TAG, "Email non trovata");
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Re-autenticazione riuscita.");
                        Intent intent = new Intent(ScegliAccountActivity.this, GenitoreActivity.class);
                        intent.putExtra("genitore", genitorePath);
                        startActivity(intent);
                    } else {
                        Log.e(TAG, "Errore nella re-autenticazione", task.getException());
                        Toast.makeText(ScegliAccountActivity.this, "Re-autenticazione fallita", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void processBambiniRefs(List<DocumentReference> bambiniRefs) {
        Log.d(TAG, "Bambini recuperati: " + bambiniRefs);

        bambiniList = new ArrayList<>();
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

        if (!bambiniRefs.isEmpty()) {
            for (DocumentReference bambinoRef : bambiniRefs) {
                tasks.add(bambinoRef.get());
            }
            getPicAndNameFromFirestore(tasks);
        } else {
            showEmptyChildListMessage();
        }
    }

    private void getPicAndNameFromFirestore(List<Task<DocumentSnapshot>> tasks) {
        Tasks.whenAllComplete(tasks)
                .addOnSuccessListener(task -> {
                    for (Task<DocumentSnapshot> bambinoTask : tasks) {
                        if (bambinoTask.isSuccessful()) {
                            DocumentSnapshot bambinoSnapshot = bambinoTask.getResult();
                            if (bambinoSnapshot.exists()) {
                                String nome = bambinoSnapshot.getString("nome");
                                String path = bambinoSnapshot.getReference().getPath();
                                Log.d(TAG, String.format("Nome: %s, Path: %s", nome, path));

                                User user = new User(nome).putDocRef(path);
                                bambiniList.add(user);
                            }
                        } else {
                            Log.d(TAG, "Errore nel recupero dei dati di un utente", bambinoTask.getException());
                        }
                    }
                    displaySortedUsers();
                });
    }

    private void displaySortedUsers() {
        if (!bambiniList.isEmpty()) {
            Log.d(TAG, "Utenti recuperati: " + bambiniList);
            bambiniList.sort((u1, u2) -> u1.getNome().compareToIgnoreCase(u2.getNome()));

            for (User bambino : bambiniList) {
                addAccountItemList(bambino);
            }
        } else {
            showEmptyChildListMessage();
        }
    }

    private void addAccountItemList(User bambino) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View accountItemView = inflater.inflate(R.layout.layout_user_account_item, linearLayoutAccounts, false);

        TextView tvNomeBambino = accountItemView.findViewById(R.id.textViewNomeBambino);

        ImageView ProfilePic = accountItemView.findViewById(R.id.imageViewUserPicBambino);

        String bambinoIdraw = bambino.getDocRef();
        // Find the last occurrence of '/'
        int lastSlashIndex = bambinoIdraw.lastIndexOf('/');
        // Extract the substring after the last '/'
        String bambinoId = bambinoIdraw.substring(lastSlashIndex + 1);

        loadCurrentAvatar(bambinoId, ProfilePic);

        tvNomeBambino.setText(bambino.getNome());

        accountItemView.setOnClickListener(v -> {
            Intent intent = new Intent(ScegliAccountActivity.this, BambinoActivity.class);
            intent.putExtra("bambinoId", bambino.getDocRef());
            startActivity(intent);
        });

        linearLayoutAccounts.addView(accountItemView);
    }

    private void showEmptyChildListMessage() {
        TextView tvNessunAccount = new TextView(this);
        tvNessunAccount.setText("Nessun account dei bambini trovato");
        tvNessunAccount.setTextSize(18);
        linearLayoutAccounts.addView(tvNessunAccount);
    }

    private void loadCurrentAvatar(String bambinoId, ImageView ProfilePic) {
        db.collection("bambini")
                .document(bambinoId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String avatarFilename = documentSnapshot.getString("avatarCorrente");
                        String temaCorrente =  documentSnapshot.getString("tema");
                        String sessoBambino = documentSnapshot.getString("sesso");
                        String personaggi_da_visualizzare = "";
                        if (sessoBambino.equals("M")){
                            personaggi_da_visualizzare = "personaggi";
                        }else{
                            personaggi_da_visualizzare = "personaggi_femminili";
                        }
                        if (avatarFilename != null) {
                            db.collection("avatars").document(temaCorrente).collection(personaggi_da_visualizzare).document(avatarFilename).get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                String imagePath = document.getString("imageUrl");

                                                if (imagePath != null && !imagePath.isEmpty()) {
                                                    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imagePath);
                                                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                                        Log.d(TAG, "Image URL retrieved successfully for " + avatarFilename);
                                                        Glide.with(ScegliAccountActivity.this).load(uri).into(ProfilePic);
                                                    }).addOnFailureListener(exception -> {
                                                        Log.e(TAG, "Failed to get download URL for " + avatarFilename, exception);
                                                    });
                                                } else {
                                                    Log.e(TAG, "Image path is null or empty for " + avatarFilename);
                                                }
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("LoadAvatar", "Failed to load avatar image", e);
                                    });
                        }
                    }
                });
    }

    private void logout() {
        // Elimina i dati dalle SharedPreferences

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_EMAIL, "");
        editor.putString(PREF_PASSWORD, "");
        editor.apply();

        // Esegui il logout da Firebase Auth
        FirebaseAuth.getInstance().signOut();

        // Reindirizza l'utente alla schermata di login
        Intent intent = new Intent(ScegliAccountActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Chiudi l'activity corrente


    }

}