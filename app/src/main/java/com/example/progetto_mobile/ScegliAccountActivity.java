package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ScegliAccountActivity extends AppCompatActivity {

    private static final String TAG = "ScegliAccountActivity";
    private LinearLayout linearLayoutAccountGenitore, linearLayoutAccounts;
    private TextView tvNomeGenitore;
    private List<User> bambiniList;

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

        linearLayoutAccountGenitore.setOnClickListener(v -> {
            Intent intent = new Intent(ScegliAccountActivity.this, HomeGenitoreActivity.class);
            intent.putExtra("genitore", genitorePath);
            startActivity(intent);
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

                                User user = new User(nome).setDocRef(path);
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

        tvNomeBambino.setText(bambino.getNome());

        accountItemView.setOnClickListener(v -> {
            Intent intent = new Intent(ScegliAccountActivity.this, HomeBambinoActivity.class);
            intent.putExtra("bambino", bambino.getDocRef());
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
}