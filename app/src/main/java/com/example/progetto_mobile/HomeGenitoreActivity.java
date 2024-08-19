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

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

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
                                        String nome = figlioSnapshot.getString("nome");
                                        int progresso = figlioSnapshot.getLong("progresso").intValue();

                                        List<DocumentReference> eserciziTipo1Refs = (List<DocumentReference>) figlioSnapshot.get("eserciziTipo1");
                                        List<DocumentReference> eserciziTipo2Refs = (List<DocumentReference>) figlioSnapshot.get("eserciziTipo2");
                                        List<DocumentReference> eserciziTipo3Refs = (List<DocumentReference>) figlioSnapshot.get("eserciziTipo3");

                                        Log.d("HomeGenitoreActivity", "Nome figlio: " + nome);
                                        Log.d("HomeGenitoreActivity", "Progresso figlio: " + progresso);
                                        Log.d("HomeGenitoreActivity", "Esercizi tipo 1: " + eserciziTipo1Refs.size());
                                        Log.d("HomeGenitoreActivity", "Esercizi tipo 2: " + eserciziTipo2Refs.size());
                                        Log.d("HomeGenitoreActivity", "Esercizi tipo 3: " + eserciziTipo3Refs.size());

                                        getEserciziDetails(eserciziTipo1Refs, eserciziTipo2Refs, eserciziTipo3Refs, (eserciziTipo1, eserciziTipo2, eserciziTipo3) -> {
                                            Child child = new Child(nome, progresso, eserciziTipo1, eserciziTipo2, eserciziTipo3);
                                            addChildDashboard(child);
                                        });
                                    }
                                });
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Errore nel recupero dei dati", Toast.LENGTH_SHORT).show());
    }

    private void getEserciziDetails(List<DocumentReference> tipo1Refs, List<DocumentReference> tipo2Refs, List<DocumentReference> tipo3Refs, EserciziCallback callback) {
        List<EsercizioTipo1> eserciziTipo1 = new ArrayList<>();
        List<EsercizioTipo2> eserciziTipo2 = new ArrayList<>();
        List<EsercizioTipo3> eserciziTipo3 = new ArrayList<>();

        // Utilizziamo Tasks.whenAllComplete per gestire tutte le richieste asincrone
        List<Task<?>> allTasks = new ArrayList<>();

        // Aggiungi task per EsercizioTipo1
        for (DocumentReference ref : tipo1Refs) {
            allTasks.add(ref.get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    eserciziTipo1.add(doc.toObject(EsercizioTipo1.class));
                }
            }));
        }

        // Aggiungi task per EsercizioTipo2
        for (DocumentReference ref : tipo2Refs) {
            allTasks.add(ref.get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    eserciziTipo2.add(doc.toObject(EsercizioTipo2.class));
                }
            }));
        }

        // Aggiungi task per EsercizioTipo3
        for (DocumentReference ref : tipo3Refs) {
            allTasks.add(ref.get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    eserciziTipo3.add(doc.toObject(EsercizioTipo3.class));
                }
            }));
        }

        // Attendi il completamento di tutti i task
        Tasks.whenAllComplete(allTasks)
                .addOnSuccessListener(tasks -> {
                    // Tutti i task sono completati, chiama il callback
                    callback.onComplete(eserciziTipo1, eserciziTipo2, eserciziTipo3);
                })
                .addOnFailureListener(e -> {
                    // Gestisci eventuali errori
                    Log.e("getEserciziDetails", "Error retrieving esercizi", e);
                });
    }

    interface EserciziCallback {
        void onComplete(List<EsercizioTipo1> tipo1, List<EsercizioTipo2> tipo2, List<EsercizioTipo3> tipo3);
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

        childDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeGenitoreActivity.this, DashboardBambinoActivity.class);
            intent.putExtra("child", child);
            startActivity(intent);
        });
        linearLayoutFigli.addView(childDashboard);
    }

    private void addExerciseInfo(LinearLayout container, List<List<?>> allEsercizi) {
        String[] tipi = {"Denominazione immagine", "Riconoscimento coppie minime", "Ripetizione sequenza di parole"};
        for (int i = 0; i < allEsercizi.size(); i++) {
            List<?> esercizi = allEsercizi.get(i);
            if (esercizi != null && !esercizi.isEmpty()) {
                for (Object esercizio : esercizi) {
                    boolean esercizioCorretto = false;
                    if (esercizio instanceof EsercizioTipo1) {
                        esercizioCorretto = ((EsercizioTipo1) esercizio).isEsercizio_corretto();
                    } else if (esercizio instanceof EsercizioTipo2) {
                        esercizioCorretto = ((EsercizioTipo2) esercizio).isEsercizio_corretto();
                    } else if (esercizio instanceof EsercizioTipo3) {
                        esercizioCorretto = ((EsercizioTipo3) esercizio).isEsercizio_corretto();
                    }
                    addExerciseView(container, tipi[i], esercizioCorretto);
                }
            }
        }
    }

    private void addExerciseView(LinearLayout container, String tipo, boolean esercizioCorretto) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View exerciseView = inflater.inflate(R.layout.layout_exercise_info, container, false);

        TextView textViewExerciseName = exerciseView.findViewById(R.id.textViewExerciseName);
        TextView textViewExerciseStatus = exerciseView.findViewById(R.id.textViewExerciseStatus);

        textViewExerciseName.setText(tipo);
        textViewExerciseStatus.setText(esercizioCorretto ? "Completato" : "In corso");

        container.addView(exerciseView);
    }
}