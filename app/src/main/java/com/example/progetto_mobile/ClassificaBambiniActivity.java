package com.example.progetto_mobile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class ClassificaBambiniActivity extends AppCompatActivity {

    private static final String TAG = "ClassificaBambiniActivity";
    private LinearLayout linearLayoutBambini;
    private int childCount = 0;
    private List<Child> childList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_classifica_bambini);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        linearLayoutBambini = findViewById(R.id.linearLayoutBambini);
        childList = new ArrayList<>();

        String logopedistaPath = getIntent().getStringExtra("logopedista");
        getBambiniFromFirestore(logopedistaPath);
    }

    private void getBambiniFromFirestore(String logopedistaPath) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.document(logopedistaPath)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Dati presi: " + documentSnapshot);
                        List<DocumentReference> bambiniRefs = (List<DocumentReference>) documentSnapshot.get("bambiniRef");

                        if (bambiniRefs == null || bambiniRefs.isEmpty()) {
                            showEmptyChildrenMessage();
                        } else {
                            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                            for (DocumentReference bambinoRef : bambiniRefs) {
                                tasks.add(bambinoRef.get());
                            }

                            Tasks.whenAllComplete(tasks)
                                    .addOnCompleteListener(task -> {
                                        for (Task<DocumentSnapshot> bambinoTask : tasks) {
                                            if (bambinoTask.isSuccessful()) {
                                                DocumentSnapshot bambinoSnapshot = bambinoTask.getResult();
                                                if (bambinoSnapshot.exists()) {
                                                    String nome = bambinoSnapshot.getString("nome");
                                                    String cognome = bambinoSnapshot.getString("cognome");
                                                    int coins = bambinoSnapshot.getLong("coins").intValue();

                                                    Child child = new Child(nome, cognome, coins);
                                                    childList.add(child);
                                                }
                                            } else {
                                                Log.d(TAG, "Errore nel recupero dei dati di un bambino", bambinoTask.getException());
                                            }
                                        }
                                        displaySortedChildren();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> Log.d(TAG, "Errore nel recupero dei dati di: " + logopedistaPath, e));
    }

    private void displaySortedChildren() {
        if (!childList.isEmpty()) {
            childList.sort((child1, child2) -> Integer.compare(child2.getCoins(), child1.getCoins()));
            for (Child child : childList)
                addChildItemList(child);
        } else showEmptyChildrenMessage();
    }

    private void showEmptyChildrenMessage() {
        TextView tvNessunBambino = new TextView(this);
        tvNessunBambino.setText("Nessun bambino trovato.");
        tvNessunBambino.setTextSize(18);
        linearLayoutBambini.addView(tvNessunBambino);
    }

    private void addChildItemList(Child child) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View childItem = inflater.inflate(R.layout.layout_child_item_list, linearLayoutBambini, false);

        TextView textViewChildPosition = childItem.findViewById(R.id.textViewPosition);
        TextView textViewChildName = childItem.findViewById(R.id.textViewChildName);
        TextView textViewChildCoins = childItem.findViewById(R.id.textViewChildCoins);

        textViewChildPosition.setText(String.format("#%d", ++childCount));
        textViewChildName.setText(String.format("%s %s", child.getNome(), child.getCognome()));
        textViewChildCoins.setText(String.format("%s: %d", "Coins", child.getCoins()));

        linearLayoutBambini.addView(childItem);
    }
}