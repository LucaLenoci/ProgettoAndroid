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

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardBambinoActivity extends AppCompatActivity {

    private static final String TAG = "DashboardBambinoActivity";
    private TabLayout tabLayout;
    private LinearLayout contentLayout;
    private Child child;
    private boolean isFromHomeLogopedista = false;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard_bambino);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tabLayout = findViewById(R.id.tabLayout);
        contentLayout = findViewById(R.id.contentLayout);
        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        child = (Child) intent.getSerializableExtra("child");
        String from = intent.getStringExtra("from");
        Log.d(TAG, "From: " + from);
        if (from != null && from.equals("homeLogopedista"))
            isFromHomeLogopedista = true;

        if (child != null) {
            setupTabs();
        }
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Denominazione immagine"));
        tabLayout.addTab(tabLayout.newTab().setText("Riconoscimento coppie minime"));
        tabLayout.addTab(tabLayout.newTab().setText("Ripetizione sequenza di parole"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    closeExerciseEditFragment();
                }
                updateContent(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        if (tabLayout.getTabCount() > 0) {
            updateContent(0);
        }
    }

    private void updateContent(int position) {
        contentLayout.removeAllViews();
        String exerciseType = "tipo" + (position + 1);
        displayExercises(exerciseType);
    }

    private void displayExercises(String exerciseType) {
        contentLayout.removeAllViews();

        if (isPlaceholder(exerciseType)) {
            if (isFromHomeLogopedista) {
                showAddExerciseButton(exerciseType);
            } else {
                showNoExercisesMessage();
            }
            return;
        }

        String exerciseRef = getExerciseRef(exerciseType);
        if (exerciseRef == null || exerciseRef.isEmpty()) {
            if (isFromHomeLogopedista) {
                showAddExerciseButton(exerciseType);
            } else {
                showNoExercisesMessage();
            }
        } else {
            Object exercise = getExerciseFromChild(exerciseType);
            if (exercise != null) {
                displayExercise(exercise, exerciseType);
                if (isFromHomeLogopedista) {
                    showEditExerciseButton(exerciseType);
                }
            } else {
                fetchAndDisplayExercise(exerciseType, exerciseRef);
            }
        }
    }

    private Object getExerciseFromChild(String exerciseType) {
        switch (exerciseType) {
            case "tipo1":
                return child.getEserciziTipo1();
            case "tipo2":
                return child.getEserciziTipo2();
            case "tipo3":
                return child.getEserciziTipo3();
            default:
                return null;
        }
    }

    private boolean isPlaceholder(String exerciseType) {
        String exerciseRef = getExerciseRef(exerciseType);
        if (exerciseRef == null || exerciseRef.isEmpty()) {
            return true;
        }
        return exerciseRef.startsWith("esercizi/placeholder/");
    }

    private String getExerciseRef(String exerciseType) {
        switch (exerciseType) {
            case "tipo1":
                return child.getEsercizioTipo1Ref();
            case "tipo2":
                return child.getEsercizioTipo2Ref();
            case "tipo3":
                return child.getEsercizioTipo3Ref();
            default:
                return null;
        }
    }

    private void fetchAndDisplayExercise(String exerciseType, String exerciseRef) {
        db.document(exerciseRef).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Object exercise = documentSnapshot.toObject(getExerciseClass(exerciseType));
                        displayExercise(exercise, exerciseType);
                    } else {
                        if (isFromHomeLogopedista) {
                            showAddExerciseButton(exerciseType);
                        } else {
                            showNoExercisesMessage();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching exercise", e);
                    Toast.makeText(this, "Error fetching exercise", Toast.LENGTH_SHORT).show();
                });
    }

    private Class<?> getExerciseClass(String exerciseType) {
        switch (exerciseType) {
            case "tipo1":
                return EsercizioTipo1.class;
            case "tipo2":
                return EsercizioTipo2.class;
            case "tipo3":
                return EsercizioTipo3.class;
            default:
                throw new IllegalArgumentException("Unknown exercise type: " + exerciseType);
        }
    }

    private void showAddExerciseButton(String exerciseType) {
        Button addButton = new Button(this);
        addButton.setText("Aggiungi esercizio");
        addButton.setOnClickListener(v -> openExerciseEditFragment(exerciseType, false, null));
        contentLayout.addView(addButton);
    }

    private void showEditExerciseButton(String exerciseType) {
        Button editButton = new Button(this);
        editButton.setText("Modifica esercizio");
        editButton.setOnClickListener(v -> {
            Object exercise = getExerciseFromChild(exerciseType);
            openExerciseEditFragment(exerciseType, true, exercise);
        });
        contentLayout.addView(editButton);
    }

    private void openExerciseEditFragment(String exerciseType, boolean isEditing, Object exercise) {
        ExerciseEditFragment fragment = ExerciseEditFragment.newInstance(exerciseType, isEditing, exercise);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
        contentLayout.removeAllViews();
    }

    public void closeExerciseEditFragment() {
        getSupportFragmentManager().popBackStack();
        displayExercises(getCurrentExerciseType());
    }

    public void addExercise(String exerciseType, Object exercise) {
        addExerciseToFirestoreAndAssignItToChild(exerciseType, exercise);
        closeExerciseEditFragment();
    }

    public void updateExercise(String exerciseType, Object exercise) {
        updateExerciseInFirestore(exerciseType, exercise);
        closeExerciseEditFragment();
    }

    private String getCurrentExerciseType() {
        int position = tabLayout.getSelectedTabPosition();
        return "tipo" + (position + 1);
    }

    private void showNoExercisesMessage() {
        TextView messageView = new TextView(this);
        messageView.setText("Nessun esercizio disponibile");
        contentLayout.addView(messageView);
    }

    private void addExerciseToFirestoreAndAssignItToChild(String exerciseType, Object exercise) {
        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        DocumentReference exerciseDocRef = db.collection("esercizi")
                .document(child.getDocId())
                .collection(exerciseType)
                .document(date);
        DocumentReference childDocRef = db.collection("bambini").document(child.getDocId());

        db.runTransaction((transaction) -> {
            transaction.set(exerciseDocRef, exercise);
            transaction.update(childDocRef, "esercizio" + exerciseType.substring(0, 1).toUpperCase() + exerciseType.substring(1), exerciseDocRef);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Esercizio aggiunto con successo", Toast.LENGTH_SHORT).show();
            updateChildExerciseRef(exerciseType, exerciseDocRef.getPath());
            updateLocalChild(exerciseType, exerciseDocRef, exercise);
            displayExercises(exerciseType);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error adding exercise", e);
            Toast.makeText(this, "Errore nell'aggiunta dell'esercizio", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateLocalChild(String exerciseType, DocumentReference exerciseDocRef, Object exercise) {
        switch (exerciseType) {
            case "tipo1":
                child.setEserciziTipo1((EsercizioTipo1) exercise);
                child.putEsercizioTipo1Ref(exerciseDocRef.getPath());
                break;
            case "tipo2":
                child.setEserciziTipo2((EsercizioTipo2) exercise);
                child.putEsercizioTipo2Ref(exerciseDocRef.getPath());
                break;
            case "tipo3":
                child.setEserciziTipo3((EsercizioTipo3) exercise);
                child.putEsercizioTipo3Ref(exerciseDocRef.getPath());
                break;
        }
    }

    private void updateExerciseInFirestore(String exerciseType, Object exercise) {
        String exerciseRef = getExerciseRef(exerciseType);
        if (exerciseRef != null && !exerciseRef.isEmpty()) {
            DocumentReference exerciseDocRef = db.document(exerciseRef);
            exerciseDocRef.set(exercise)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Esercizio aggiornato con successo", Toast.LENGTH_SHORT).show();
                        updateLocalChild(exerciseType, exerciseDocRef, exercise);
                        displayExercises(exerciseType);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating exercise", e);
                        Toast.makeText(this, "Errore nell'aggiornamento dell'esercizio", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateChildExerciseRef(String exerciseType, String newRef) {
        switch (exerciseType) {
            case "tipo1":
                child.putEsercizioTipo1Ref(newRef);
                break;
            case "tipo2":
                child.putEsercizioTipo2Ref(newRef);
                break;
            case "tipo3":
                child.putEsercizioTipo3Ref(newRef);
                break;
        }
    }

    private void displayExercise(Object exercise, String exerciseType) {
        View exerciseView = LayoutInflater.from(this).inflate(R.layout.layout_item_esercizio, contentLayout, false);
        TextView titleTextView = exerciseView.findViewById(R.id.esercizioTitleTextView);
        TextView detailsTextView = exerciseView.findViewById(R.id.esercizioDetailsTextView);

        titleTextView.setText(exerciseType);
        detailsTextView.setText(getExerciseDetails(exercise));

        contentLayout.addView(exerciseView);
    }

    // todo: rivedi quali campi far vedere
    private String getExerciseDetails(Object exercise) {
        if (exercise instanceof EsercizioTipo1) {
            EsercizioTipo1 es = (EsercizioTipo1) exercise;
            return String.format("Corretto: %s\nRisposta: %s\nRisposta corretta: %s\nSuggerimento: %s",
                    es.isEsercizio_corretto(), es.getRisposta(), es.getRisposta_corretta(), es.getSuggerimento());
        } else if (exercise instanceof EsercizioTipo2) {
            EsercizioTipo2 es = (EsercizioTipo2) exercise;
            return String.format("Corretto: %s\nRisposta corretta: %s\nRisposta sbagliata: %s\nImmagine corretta: %d",
                    es.isEsercizio_corretto(), es.getRisposta_corretta(), es.getRisposta_sbagliata(), es.getImmagine_corretta());
        } else if (exercise instanceof EsercizioTipo3) {
            EsercizioTipo3 es = (EsercizioTipo3) exercise;
            return String.format("Corretto: %s\nRisposta corretta: %s",
                    es.isEsercizio_corretto(), es.getRisposta_corretta());
        }
        return "Tipo di esercizio sconosciuto";
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            closeExerciseEditFragment();
        } else {
            super.onBackPressed();
        }
    }
}