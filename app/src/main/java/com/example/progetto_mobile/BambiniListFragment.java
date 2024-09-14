package com.example.progetto_mobile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private boolean isFromHomeLogopedista = false;
    private Button btnScegliData;
    private String selectedDate;
    private int mYear;
    private int mMonth;
    private int mDay;

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
        btnScegliData = view.findViewById(R.id.btnScegliData);

        // Inizializza la data corrente
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        setDateOnButton();

        String from = getActivity().getIntent().getStringExtra("from");
        if (from != null && from.equals("homeLogopedista")) {
            Log.d(TAG, "from: " + from);
            isFromHomeLogopedista = true;
        }

        // se non si vuole far selezionare la data al genitore spostare nell'if sopra
        btnScegliData.setOnClickListener(v -> showDatePickerDialog());

        // se non si vuole far selezionare la data al genitore
        /*
        if (!isFromHomeLogopedista) {
            tvScegliData.setVisibility(View.GONE);
            btnScegliData.setClickable(false);
        }
         */

        if (genitorePath != null) {
            getBambiniFromFirestore(genitorePath);
        }

        return view;
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;

                    setDateOnButton();

                    linearLayoutBambini.removeAllViews();

                    if (genitorePath != null) {
                        getBambiniFromFirestore(genitorePath);
                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }

    private void setDateOnButton() {
        selectedDate = String.format("%02d-%02d-%04d", mDay, mMonth + 1, mYear);
        btnScegliData.setText(selectedDate);
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

            DocumentReference esercizioTipo1Ref = db.document("esercizi/" + bambinoSnapshot.getId() + "/tipo1/" + selectedDate);
            DocumentReference esercizioTipo2Ref = db.document("esercizi/" + bambinoSnapshot.getId() + "/tipo2/" + selectedDate);
            DocumentReference esercizioTipo3Ref = db.document("esercizi/" + bambinoSnapshot.getId() + "/tipo3/" + selectedDate);

            Log.d("HomeGenitoreActivity", "Nome bambino: " + nome);
            Log.d("HomeGenitoreActivity", "Progresso bambino: " + progresso);

            getEserciziDetails(esercizioTipo1Ref, esercizioTipo2Ref, esercizioTipo3Ref, (esercizioTipo1, esercizioTipo2, esercizioTipo3) -> {
                Child child = new Child(
                        nome,
                        progresso,
                        coins,
                        esercizioTipo1,
                        esercizioTipo2,
                        esercizioTipo3);
                child.putDocId(bambinoSnapshot.getId());
                if (esercizioTipo1.getPlaceholder() == null) child.putEsercizioTipo1Ref(esercizioTipo1Ref.getPath());
                if (esercizioTipo2.getPlaceholder() == null) child.putEsercizioTipo2Ref(esercizioTipo2Ref.getPath());
                if (esercizioTipo3.getPlaceholder() == null) child.putEsercizioTipo3Ref(esercizioTipo3Ref.getPath());
                addChildDashboard(child);
            });
        }
    }

    private void getEserciziDetails(DocumentReference tipo1Ref, DocumentReference tipo2Ref, DocumentReference tipo3Ref, BambiniListFragment.EserciziCallback callback) {
        DocumentReference placeholderTipo1Ref = db.document("/esercizi/placeholder/tipo1/16-08-2024");
        DocumentReference placeholderTipo2Ref = db.document("/esercizi/placeholder/tipo2/16-08-2024");
        DocumentReference placeholderTipo3Ref = db.document("/esercizi/placeholder/tipo3/16-08-2024");

        Task<EsercizioTipo1> esercizioTipo1Task = tipo1Ref != null
                ? tipo1Ref.get().continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        EsercizioTipo1 esercizioTipo1 = task.getResult().toObject(EsercizioTipo1.class);
                        return esercizioTipo1 != null
                                ? Tasks.forResult(esercizioTipo1)
                                : placeholderTipo1Ref.get().continueWith(placeholderTask ->
                                placeholderTask.getResult().toObject(EsercizioTipo1.class));
                    }
                    return placeholderTipo1Ref.get().continueWith(placeholderTask ->
                            placeholderTask.getResult().toObject(EsercizioTipo1.class));
                })
                : placeholderTipo1Ref.get().continueWith(task -> task.getResult().toObject(EsercizioTipo1.class));

        Task<EsercizioTipo2> esercizioTipo2Task = tipo2Ref != null
                ? tipo2Ref.get().continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        EsercizioTipo2 esercizioTipo2 = task.getResult().toObject(EsercizioTipo2.class);
                        return esercizioTipo2 != null
                                ? Tasks.forResult(esercizioTipo2)
                                : placeholderTipo2Ref.get().continueWith(placeholderTask ->
                                placeholderTask.getResult().toObject(EsercizioTipo2.class));
                    }
                    return placeholderTipo2Ref.get().continueWith(placeholderTask ->
                            placeholderTask.getResult().toObject(EsercizioTipo2.class));
                })
                : placeholderTipo2Ref.get().continueWith(task ->task.getResult().toObject(EsercizioTipo2.class));

        Task<EsercizioTipo3> esercizioTipo3Task = tipo3Ref != null
                ? tipo3Ref.get().continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        EsercizioTipo3 esercizioTipo3 = task.getResult().toObject(EsercizioTipo3.class);
                        return esercizioTipo3 != null
                                ? Tasks.forResult(esercizioTipo3)
                                : placeholderTipo3Ref.get().continueWith(placeholderTask ->
                                placeholderTask.getResult().toObject(EsercizioTipo3.class));
                    }
                    return placeholderTipo3Ref.get().continueWith(placeholderTask ->
                            placeholderTask.getResult().toObject(EsercizioTipo3.class));
                })
                : placeholderTipo3Ref.get().continueWith(task -> task.getResult().toObject(EsercizioTipo3.class));

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
        loadCurrentAvatar( child.getDocId(), imageViewChild);
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

        if (!isFromHomeLogopedista) {
            childDashboard.setOnLongClickListener(v -> showEditChildThemeDialog(child));
        }

        linearLayoutBambini.addView(childDashboard);
    }

    private boolean showEditChildThemeDialog(Child child) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Modifica tema avatar");

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_dialog_edit_theme, null);
        builder.setView(dialogView);

        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroupThemes);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBarThemes);

        progressBar.setVisibility(View.VISIBLE);
        radioGroup.setVisibility(View.GONE);

        fetchAvatarsAndPopulateRadioButtons(radioGroup, progressBar, child);

        builder.setPositiveButton("Conferma", (dialog, which) -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selectedRadioButton = dialogView.findViewById(selectedId);
                String newTheme = selectedRadioButton.getText().toString();
                updateChildTheme(child, newTheme);
            }
        });
        builder.setNegativeButton("Annulla", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        return true;
    }

    private void fetchAvatarsAndPopulateRadioButtons(RadioGroup radioGroup, ProgressBar progressBar, Child child) {
        db.collection("avatars")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> avatarIds = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        avatarIds.add(documentSnapshot.getId());
                    }
                    populateRadioButtons(radioGroup, avatarIds, child);
                    progressBar.setVisibility(View.GONE);
                    radioGroup.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching avatars", e);
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void populateRadioButtons(RadioGroup radioGroup, List<String> avatarIds, Child child) {
        for (String avatarId : avatarIds) {
            RadioButton radioButton = new RadioButton(requireContext());
            radioButton.setText(avatarId);
            radioButton.setId(View.generateViewId());
            radioGroup.addView(radioButton);
        }

        db.document("bambini/" + child.getDocId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String currentTheme = documentSnapshot.getString("tema");
                    if (currentTheme != null) {
                        for (int i = 0; i < radioGroup.getChildCount(); i++) {
                            RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
                            if (radioButton.getText().toString().equals(currentTheme)) {
                                radioButton.setChecked(true);
                                break;
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching child theme", e));
    }

    private void updateChildTheme(Child child, String newTheme) {
        db.document("bambini/" + child.getDocId())
                .update("tema", newTheme)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Theme updated successfully");
                    linearLayoutBambini.removeAllViews();
                    getBambiniFromFirestore(genitorePath);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error updating theme", e));
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
                                                        Glide.with(getContext()).load(uri).into(ProfilePic);
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
}