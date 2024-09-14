package com.example.progetto_mobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HomeLogopedistaFragment extends Fragment {

    private static final String TAG = "HomeLogopedistaFragment";
    private static final String ARG_LOGOPEDISTA_PATH = "logopedista";
    private LinearLayout linearLayoutGenitori;
    private List<String> genitoriPaths;
    private List<Genitore> genitoriList;
    private String logopedistaPath = "";

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String PREF_EMAIL = "savedemail";
    private static final String PREF_PASSWORD = "savedpassword";

    public static HomeLogopedistaFragment newInstance(String logopedistaPath) {
        HomeLogopedistaFragment fragment = new HomeLogopedistaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOGOPEDISTA_PATH, logopedistaPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home_logopedista, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        linearLayoutGenitori = view.findViewById(R.id.linearLayoutGenitori);
        genitoriPaths = new ArrayList<>();
        genitoriList = new ArrayList<>();

        if (getArguments() != null) {
            logopedistaPath = getArguments().getString("logopedista");
        }

        if (logopedistaPath != null) {
            getGenitoriFromLogopedistaPath(logopedistaPath);
        }

        Button btnVediClassificaBambini = view.findViewById(R.id.buttonClassificaBambini);
        btnVediClassificaBambini.setOnClickListener(v -> {
            if (logopedistaPath == null)
                Toast.makeText(getContext(), "Errore: logopedista non trovato", Toast.LENGTH_SHORT).show();

            Fragment fragment;
            fragment = new ClassificaBambiniFragment();
            Bundle args = new Bundle();
            args.putString("logopedista", logopedistaPath);
            fragment.setArguments(args);
            if (fragment != null) {

                if (getParentFragmentManager() != null) {
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        Button btnRegistraGenitore = view.findViewById(R.id.buttonRegistraGenitore);
        btnRegistraGenitore.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.putExtra("from", "registraGenitore");
            startActivity(intent);

        });

        // Recupera il riferimento al pulsante di logout
        Button buttonLogout = view.findViewById(R.id.btnLogout);

        // Imposta il listener per il pulsante di logout
        ((View) buttonLogout).setOnClickListener(v -> {
            // Chiama il metodo per il logout
            logout();
        });

        return view;
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
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View parentItem = inflater.inflate(R.layout.layout_parent_item_list, linearLayoutGenitori, false);

        TextView textViewParentName = parentItem.findViewById(R.id.textViewParentName);
        TextView textViewParentChildCount = parentItem.findViewById(R.id.textViewParentChildCount);

        textViewParentName.setText(String.format("%s %s", genitore.getNome(), genitore.getCognome()));
        textViewParentChildCount.setText(String.format("%s:\n%d", "Bambini", genitore.getBambiniRef().size()));

        parentItem.setOnClickListener(v -> {
            Fragment fragment;
            fragment = new DashboardGenitoreFragment();
            Bundle args = new Bundle();
            args.putSerializable("genitore", genitore);
            args.putString("logopedista", logopedistaPath);
            args.putString("from", "homeLogopedista");
            fragment.setArguments(args);

            if (fragment != null) {

                if (getParentFragmentManager() != null) {
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        linearLayoutGenitori.addView(parentItem);
    }

    private void showEmptyParentsMessage() {
        TextView tvNessunGenitore = new TextView(getContext());
        tvNessunGenitore.setText("Nessun genitore trovato.");
        tvNessunGenitore.setTextSize(18);
        linearLayoutGenitori.addView(tvNessunGenitore);
    }

    private void logout() {
        // Elimina i dati dalle SharedPreferences

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_EMAIL, "");
        editor.putString(PREF_PASSWORD, "");
        editor.apply();

        // Esegui il logout da Firebase Auth
        FirebaseAuth.getInstance().signOut();

        // Reindirizza l'utente alla schermata di login
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        }


    }
}