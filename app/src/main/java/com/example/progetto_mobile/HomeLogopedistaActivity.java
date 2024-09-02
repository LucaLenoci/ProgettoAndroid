package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import java.util.stream.Collectors;

public class HomeLogopedistaActivity extends AppCompatActivity {

    private static final String TAG = "HomeLogopedistaActivity";
    private LinearLayout linearLayoutGenitori;
    private List<String> genitoriPaths;
    private List<Genitore> genitoriList;
    private String logopedistaPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_logopedista);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        linearLayoutGenitori = findViewById(R.id.linearLayoutGenitori);
        genitoriPaths = new ArrayList<>();
        genitoriList = new ArrayList<>();

        logopedistaPath = getIntent().getStringExtra("logopedista");
        if (logopedistaPath != null) {
            getGenitoriFromLogopedistaPath(logopedistaPath);
        }

        Button btnVediClassificaBambini = findViewById(R.id.buttonClassificaBambini);
        btnVediClassificaBambini.setOnClickListener(v -> {
            if (logopedistaPath == null)
                Toast.makeText(this, "Errore: logopedista non trovato", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeLogopedistaActivity.this, ClassificaBambiniActivity.class);
            intent.putExtra("logopedista", logopedistaPath);
            startActivity(intent);
        });

        Button btnRegistraGenitore = findViewById(R.id.buttonRegistraGenitore);
        btnRegistraGenitore.setOnClickListener(v -> {
            Intent intent = new Intent(HomeLogopedistaActivity.this, RegisterActivity.class);
            intent.putExtra("from", "registraGenitore");
            startActivity(intent);
        });
    }

    private void getGenitoriFromLogopedistaPath(String logopedistaPath) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.document(logopedistaPath)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<DocumentReference> genitoriRefs = (List<DocumentReference>) documentSnapshot.get("genitoriRef");
                    Log.d(TAG, "Dati presi: " + genitoriRefs);

                    if (genitoriRefs == null || genitoriRefs.isEmpty()) {
                        showEmptyParentsMessage();
                    } else {
                        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                        for (DocumentReference genitoreRef : genitoriRefs) {
                            Log.d(TAG, "Genitore ref: " + genitoreRef.getPath());
                            genitoriPaths.add(genitoreRef.getPath());
                            tasks.add(genitoreRef.get());
                        }

                        Tasks.whenAllComplete(tasks)
                                .addOnSuccessListener(task -> {
                                    int genitoreCounter = 0;
                                    for (Task<DocumentSnapshot> genitoreTask : tasks) {
                                        if (genitoreTask.isSuccessful()) {
                                            DocumentSnapshot genitoreSnapshot = genitoreTask.getResult();
                                            if (genitoreSnapshot.exists()) {
                                                List<DocumentReference> bambiniRefs = (List<DocumentReference>) genitoreSnapshot.get("bambiniRef");
                                                if (bambiniRefs == null) {
                                                    bambiniRefs = new ArrayList<>();
                                                }
                                                String nome = genitoreSnapshot.getString("nome");
                                                String cognome = genitoreSnapshot.getString("cognome");
                                                List<String> bambiniRefStrings = bambiniRefs.stream()
                                                        .map(DocumentReference::getPath)
                                                        .collect(Collectors.toList());

                                                Genitore genitore = new Genitore(nome, cognome, bambiniRefStrings, genitoriPaths.get(genitoreCounter));
                                                genitoriList.add(genitore);
                                            }
                                        } else {
                                            Log.d(TAG, "Errore nel recupero dei dati di un genitore", genitoreTask.getException());
                                        }
                                        genitoreCounter++;
                                    }
                                    displaySortedParents();
                                });
                    }
                })
                .addOnFailureListener(e -> Log.d(TAG, "Errore nel prendere i genitori da <" + logopedistaPath + ">: " + e));
    }

    private void displaySortedParents() {
        if (!genitoriList.isEmpty()) {
            Log.d(TAG, "Lista genitori: " + genitoriList);
            genitoriList.sort((parent1, parent2) -> CharSequence.compare(parent1.getCognome(), parent2.getCognome()));
            Log.d(TAG, "Lista genitori ordinata: " + genitoriList);
            for (Genitore genitore : genitoriList)
                addParentItemList(genitore);
        } else showEmptyParentsMessage();
    }

    private void addParentItemList(Genitore genitore) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View parentItem = inflater.inflate(R.layout.layout_parent_item_list, linearLayoutGenitori, false);

        TextView textViewParentName = parentItem.findViewById(R.id.textViewParentName);
        TextView textViewParentChildCount = parentItem.findViewById(R.id.textViewParentChildCount);

        textViewParentName.setText(String.format("%s %s", genitore.getNome(), genitore.getCognome()));
        textViewParentChildCount.setText(String.format("%s:\n%d", "Bambini", genitore.getBambiniRef().size()));

        parentItem.setOnClickListener(v -> {
            Log.d(TAG, "Genitore cliccato: " + genitore);
            Intent intent = new Intent(HomeLogopedistaActivity.this, DashboardGenitoreActivity.class);
            intent.putExtra("genitore", genitore);
            intent.putExtra("from", "homeLogopedista");
            intent.putExtra("logopedista", logopedistaPath);
            startActivity(intent);
        });

        linearLayoutGenitori.addView(parentItem);
    }

    private void showEmptyParentsMessage() {
        TextView tvNessunGenitore = new TextView(this);
        tvNessunGenitore.setText("Nessun genitore trovato.");
        tvNessunGenitore.setTextSize(18);
        linearLayoutGenitori.addView(tvNessunGenitore);
    }
}