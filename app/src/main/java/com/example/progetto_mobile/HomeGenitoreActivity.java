package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

// TODO: aggiungere il setOnClickListener sulle dashboard
public class HomeGenitoreActivity extends AppCompatActivity {

    private LinearLayout linearLayoutFigli;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_genitore);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvBenvenuto = findViewById(R.id.textViewBenvenutoGenitore);
        Button btnGestisciTemi = findViewById(R.id.buttonGestisciTemi);
//        Button btnCorreggiEsercizi = findViewById(R.id.buttonCorreggiEsercizi);
        linearLayoutFigli = findViewById(R.id.linearLayoutFigli);

        Intent intent = getIntent();
        User user = (User) intent.getSerializableExtra("user");
        if (user != null) {
            String text = "Benvenuto/a, " + user.getNome() + " " + user.getCognome();
            tvBenvenuto.setText(text);
            getFigliFromFirestore(user.getEmail());
        }

        btnGestisciTemi.setOnClickListener(v ->
                startActivity(new Intent(HomeGenitoreActivity.this, GestisciTemiActivity.class)));

//        btnCorreggiEsercizi.setOnClickListener(v ->
//                startActivity(new Intent(HomeGenitoreActivity.this, CorreggiEserciziActivity.class)));
    }

    @SuppressWarnings("unchecked")
    private void getFigliFromFirestore(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

                        List<DocumentReference> figliRefs = (List<DocumentReference>) documentSnapshot.get("figli");

                        if (figliRefs == null || figliRefs.isEmpty()) {
                            showEmptyChildrenMessage();
                        } else {
                            Log.d("HomeGenitoreActivity", "Numero di figli: " + figliRefs.size());
                            for (DocumentReference figlioRef : figliRefs) {
                                figlioRef.get().addOnSuccessListener(figlioSnapshot -> {
                                    if (figlioSnapshot.exists()) {
                                        Child child = figlioSnapshot.toObject(Child.class);
                                        if (child != null) {
                                            Log.d("HomeGenitoreActivity", "Figlio: " + child.getNome());
                                            Log.d("HomeGenitoreActivity", "Esercizi tipo1: " + child.getEserciziTipo1());
                                            Log.d("HomeGenitoreActivity", "Esercizi tipo2: " + child.getEserciziTipo2());
                                            Log.d("HomeGenitoreActivity", "Esercizi tipo3: " + child.getEserciziTipo3());

                                            addChildDashboard(child);
                                        }
                                    }
                                });
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Errore nel recupero dei dati", Toast.LENGTH_SHORT).show());
    }

    private void showEmptyChildrenMessage() {
        TextView tvNessunFiglio = new TextView(this);
        tvNessunFiglio.setText("Non ci sono figli registrati.");
        tvNessunFiglio.setTextSize(18);
        linearLayoutFigli.addView(tvNessunFiglio);
    }

    private void addChildDashboard(Child child) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View childDashboard = inflater.inflate(R.layout.layout_child_dashboard, linearLayoutFigli, false);

        ImageView imageViewChild = childDashboard.findViewById(R.id.imageViewChild);
        TextView textViewChildName = childDashboard.findViewById(R.id.textViewChildName);
        ProgressBar progressBarChild = childDashboard.findViewById(R.id.progressBarChild);
        LinearLayout linearLayoutExercises = childDashboard.findViewById(R.id.linearLayoutExercises);

        textViewChildName.setText(child.getNome());
        progressBarChild.setProgress(child.getProgresso());

        if (child.getAllEsercizi() != null) {
            addExerciseInfo(linearLayoutExercises, child.getAllEsercizi());
        } else {
            Log.e("HomeGenitoreActivity", "getAllEsercizi() returned null for child: " + child.getNome());
        }

        linearLayoutFigli.addView(childDashboard);
    }

    private void addExerciseInfo(LinearLayout container, List<List<DocumentReference>> allEsercizi) {
        String[] tipi = {"Tipo 1", "Tipo 2", "Tipo 3"};
        for (int i = 0; i < allEsercizi.size(); i++) {
            List<DocumentReference> esercizi = allEsercizi.get(i);
            if (esercizi != null && !esercizi.isEmpty()) {
                for (DocumentReference exerciseRef : esercizi) {
                    int finalI = i;
                    exerciseRef.get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            boolean esercizioCorretto = Boolean.TRUE.equals(documentSnapshot.getBoolean("esercizio_corretto"));
                            addExerciseView(container, tipi[finalI], esercizioCorretto);
                        }
                    });
                }
            }
        }
    }

    private void addExerciseView(LinearLayout container, String tipo, boolean esercizioCorretto) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View exerciseView = inflater.inflate(R.layout.layout_exercise_info, container, false);

        TextView textViewExerciseName = exerciseView.findViewById(R.id.textViewExerciseName);
        TextView textViewExerciseStatus = exerciseView.findViewById(R.id.textViewExerciseStatus);

        textViewExerciseName.setText(String.format("Esercizio %s", tipo));
        textViewExerciseStatus.setText(esercizioCorretto ? "Completato" : "In corso");

        container.addView(exerciseView);
    }
}