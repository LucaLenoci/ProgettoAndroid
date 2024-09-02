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
    private boolean isFromHomeLogopedista = false;

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

        String from = getActivity().getIntent().getStringExtra("from");
        if (from != null && from.equals("homeLogopedista")) {
            Log.d(TAG, "from: " + from);
            isFromHomeLogopedista = true;
        }

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

            DocumentReference esercizioTipo1Ref = bambinoSnapshot.getDocumentReference("esercizioTipo1");
            DocumentReference esercizioTipo2Ref = bambinoSnapshot.getDocumentReference("esercizioTipo2");
            DocumentReference esercizioTipo3Ref = bambinoSnapshot.getDocumentReference("esercizioTipo3");

            Log.d("HomeGenitoreActivity", "Nome bambino: " + nome);
            Log.d("HomeGenitoreActivity", "Progresso bambino: " + progresso);

            // Otteniamo i dettagli di ogni esercizio
            getEserciziDetails(esercizioTipo1Ref, esercizioTipo2Ref, esercizioTipo3Ref, (eserciziTipo1, eserciziTipo2, eserciziTipo3) -> {
                Child child = new Child(
                        nome,
                        progresso,
                        coins,
                        eserciziTipo1,
                        eserciziTipo2,
                        eserciziTipo3);
                child.putDocId(bambinoSnapshot.getId());
                if (esercizioTipo1Ref != null) child.putEsercizioTipo1Ref(esercizioTipo1Ref.getPath());
                if (esercizioTipo2Ref != null) child.putEsercizioTipo2Ref(esercizioTipo2Ref.getPath());
                if (esercizioTipo3Ref != null) child.putEsercizioTipo3Ref(esercizioTipo3Ref.getPath());
                addChildDashboard(child);
            });
        }
    }

    private void getEserciziDetails(DocumentReference tipo1Ref, DocumentReference tipo2Ref, DocumentReference tipo3Ref, BambiniListFragment.EserciziCallback callback) {
        Task<EsercizioTipo1> esercizioTipo1Task = tipo1Ref != null
                ? tipo1Ref.get().continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().toObject(EsercizioTipo1.class);
                    }
                    return null;
                })
                : Tasks.forResult(null);

        Task<EsercizioTipo2> esercizioTipo2Task = tipo2Ref != null
                ? tipo2Ref.get().continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().toObject(EsercizioTipo2.class);
                    }
                    return null;
                })
                : Tasks.forResult(null);

        Task<EsercizioTipo3> esercizioTipo3Task = tipo3Ref != null
                ? tipo3Ref.get().continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().toObject(EsercizioTipo3.class);
                    }
                    return null;
                })
                : Tasks.forResult(null);

        // Attendi il completamento di tutti i task
        Tasks.whenAllComplete(esercizioTipo1Task, esercizioTipo2Task, esercizioTipo3Task)
                .addOnSuccessListener(tasks -> {
                    // Tutti i task sono completati, chiama il callback
                    callback.onComplete(esercizioTipo1Task.getResult(), esercizioTipo2Task.getResult(), esercizioTipo3Task.getResult());
                })
                .addOnFailureListener(e -> {
                    // Gestisci eventuali errori
                    Log.e("getEserciziDetails", "Error retrieving esercizi", e);
                });
    }

    interface EserciziCallback {
        void onComplete(EsercizioTipo1 tipo1, EsercizioTipo2 tipo2, EsercizioTipo3 tipo3);
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

            if (isFromHomeLogopedista) {
                intent.putExtra("from", "homeLogopedista");
            }

            startActivity(intent);
        });

        linearLayoutBambini.addView(childDashboard);
    }

    private void addExerciseInfo(LinearLayout container, List<?> allEsercizi) {
        String[] tipi = {"Denominazione immagine", "Riconoscimento coppie minime", "Ripetizione sequenza di parole"};
        for (int i = 0; i < allEsercizi.size(); i++) {
            Object esercizio = allEsercizi.get(i);
            if (esercizio != null) {
                if (isPlaceholder(esercizio)) {
                    // todo: aggiungi view 'non ci sono esercizi'
                } else {
                    boolean esercizioCorretto = isEsercizioCorretto(esercizio);
                    addExerciseView(container, tipi[i], esercizioCorretto);
                }
            }
        }
    }

    private static boolean isPlaceholder(Object esercizio) {
        String isPlaceholder = "";
        if (esercizio instanceof EsercizioTipo1) {
            isPlaceholder = ((EsercizioTipo1) esercizio).getPlaceholder();
        } else if (esercizio instanceof EsercizioTipo2) {
            isPlaceholder = ((EsercizioTipo2) esercizio).getPlaceholder();
        } else if (esercizio instanceof EsercizioTipo3) {
            isPlaceholder = ((EsercizioTipo3) esercizio).getPlaceholder();
        }
        if (isPlaceholder == null) return false;
        return isPlaceholder.equals("placeholder");
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