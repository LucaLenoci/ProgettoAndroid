package com.example.progetto_mobile;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BambiniListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BambiniListFragment extends Fragment {

    private static final String ARG_GENITORE_PATH = "genitorePath";

    private static final String TAG = "BambiniListFragment";
    private LinearLayout linearLayoutBambini;
    private FirebaseFirestore db;
    private String genitorePath;

    public BambiniListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param genitorePath il path del genitore da cui prendere i bambini
     * @return A new instance of fragment BambiniListFragment.
     */
    public static BambiniListFragment newInstance(String genitorePath) {
        BambiniListFragment fragment = new BambiniListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GENITORE_PATH, genitorePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            genitorePath = getArguments().getString(ARG_GENITORE_PATH);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bambini_list, container, false);

        linearLayoutBambini = view.findViewById(R.id.linearLayoutBambini);

        if (genitorePath != null) {
            getBambiniFromFirestore(genitorePath);
        }

        return view;
    }

    private void getBambiniFromFirestore(String genitorePath) {
        db.document(genitorePath)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<DocumentReference> bambiniRefs = (List<DocumentReference>) documentSnapshot.get("bambiniRef");

                        if (bambiniRefs == null || bambiniRefs.isEmpty()) {
                            showEmptyChildrenMessage();
                        } else {
                            Log.d(TAG, "Numero di bambini: " + bambiniRefs.size());
                            for (DocumentReference bambinoRef : bambiniRefs) {
                                bambinoRef.get().addOnSuccessListener(this::processChildData);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Errore nel recupero dei dati", e));
    }

    private void showEmptyChildrenMessage() {
        TextView tvNessunBambino = new TextView(getContext());
        tvNessunBambino.setText("Non ci sono bambini registrati.");
        tvNessunBambino.setTextSize(18);
        linearLayoutBambini.addView(tvNessunBambino);
    }

    private void processChildData(DocumentSnapshot bambinoSnapshot) {
        if (bambinoSnapshot.exists()) {
            String nome = bambinoSnapshot.getString("nome");
            int progresso = bambinoSnapshot.getLong("progresso").intValue();
            int coins = bambinoSnapshot.getLong("coins").intValue();

            List<DocumentReference> eserciziTipo1Refs = (List<DocumentReference>) bambinoSnapshot.get("eserciziTipo1");
            List<DocumentReference> eserciziTipo2Refs = (List<DocumentReference>) bambinoSnapshot.get("eserciziTipo2");
            List<DocumentReference> eserciziTipo3Refs = (List<DocumentReference>) bambinoSnapshot.get("eserciziTipo3");

            Log.d("HomeGenitoreActivity", "Nome bambino: " + nome);
            Log.d("HomeGenitoreActivity", "Progresso bambino: " + progresso);
            Log.d("HomeGenitoreActivity", "Esercizi tipo 1: " + eserciziTipo1Refs.size());
            Log.d("HomeGenitoreActivity", "Esercizi tipo 2: " + eserciziTipo2Refs.size());
            Log.d("HomeGenitoreActivity", "Esercizi tipo 3: " + eserciziTipo3Refs.size());

            getEserciziDetails(eserciziTipo1Refs, eserciziTipo2Refs, eserciziTipo3Refs, (eserciziTipo1, eserciziTipo2, eserciziTipo3) -> {
                Child child = new Child(
                        nome,
                        progresso,
                        coins,
                        eserciziTipo1,
                        eserciziTipo2,
                        eserciziTipo3);
                addChildDashboard(child);
            });
        }
    }

    private void getEserciziDetails(List<DocumentReference> tipo1Refs, List<DocumentReference> tipo2Refs, List<DocumentReference> tipo3Refs, BambiniListFragment.EserciziCallback callback) {
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

    private void addChildDashboard(Child child) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View childDashboard = inflater.inflate(R.layout.layout_child_dashboard, linearLayoutBambini, false);

        ImageView imageViewChild = childDashboard.findViewById(R.id.imageViewChild);
        TextView textViewChildName = childDashboard.findViewById(R.id.textViewChildName);
        TextView textViewChildCoins = childDashboard.findViewById(R.id.textViewChildCoins);
        ProgressBar progressBarChild = childDashboard.findViewById(R.id.progressBarChild);
        LinearLayout linearLayoutExercises = childDashboard.findViewById(R.id.linearLayoutExercises);

        textViewChildName.setText(child.getNome());
        textViewChildCoins.setText(String.format("%s: %d", "Coins", child.getCoins()));
        progressBarChild.setProgress(child.getProgresso());

        if (child.getAllEsercizi() != null) {
            addExerciseInfo(linearLayoutExercises, child.getAllEsercizi());
        } else {
            Log.e(TAG, "getAllEsercizi() returned null for child: " + child.getNome());
        }

        childDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), DashboardBambinoActivity.class);
            intent.putExtra("child", child);
            startActivity(intent);
        });

        linearLayoutBambini.addView(childDashboard);
    }

    private void addExerciseInfo(LinearLayout container, List<List<?>> allEsercizi) {
        String[] tipi = {"Denominazione immagine", "Riconoscimento coppie minime", "Ripetizione sequenza di parole"};
        for (int i = 0; i < allEsercizi.size(); i++) {
            List<?> esercizi = allEsercizi.get(i);
            if (esercizi != null && !esercizi.isEmpty()) {
                for (Object esercizio : esercizi) {
                    boolean esercizioCorretto = isEsercizioCorretto(esercizio);
                    addExerciseView(container, tipi[i], esercizioCorretto);
                }
            }
        }
    }

    private static boolean isEsercizioCorretto(Object esercizio) {
        boolean esercizioCorretto = false;
        if (esercizio instanceof EsercizioTipo1) {
            esercizioCorretto = ((EsercizioTipo1) esercizio).isEsercizio_corretto();
        } else if (esercizio instanceof EsercizioTipo2) {
            esercizioCorretto = ((EsercizioTipo2) esercizio).isEsercizio_corretto();
        } else if (esercizio instanceof EsercizioTipo3) {
            esercizioCorretto = ((EsercizioTipo3) esercizio).isEsercizio_corretto();
        }
        return esercizioCorretto;
    }

    private void addExerciseView(LinearLayout container, String tipo, boolean esercizioCorretto) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View exerciseView = inflater.inflate(R.layout.layout_exercise_info, container, false);

        TextView textViewExerciseName = exerciseView.findViewById(R.id.textViewExerciseName);
        TextView textViewExerciseStatus = exerciseView.findViewById(R.id.textViewExerciseStatus);

        textViewExerciseName.setText(tipo);
        textViewExerciseStatus.setText(esercizioCorretto ? "Completato" : "In corso");

        container.addView(exerciseView);
    }

}