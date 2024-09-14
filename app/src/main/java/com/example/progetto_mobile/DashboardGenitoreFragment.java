package com.example.progetto_mobile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardGenitoreFragment extends Fragment {

    private static final String TAG = "DashboardGenitoreFragment";
    private String genitorePath = "", logopedistaPath = "";
    private FirebaseFirestore db;

    public DashboardGenitoreFragment() {
        // Required empty public constructor
    }

    public static DashboardGenitoreFragment newInstance(Genitore genitore, String logopedistaPath) {
        DashboardGenitoreFragment fragment = new DashboardGenitoreFragment();
        Bundle args = new Bundle();
        args.putSerializable("genitore", genitore);
        args.putString("logopedista", logopedistaPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_dashboard_genitore, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        Genitore genitore = (Genitore) getArguments().getSerializable("genitore");

        Log.d(TAG, "Genitore preso: " + genitore);
        if (genitore != null) {
            genitorePath = genitore.getGenitoreRef();
            showBambiniList();
        }

        if (getArguments() != null) {
            logopedistaPath = getArguments().getString("logopedista");
        }

        Button btnRegistraBambino = view.findViewById(R.id.buttonRegistraBambino);
        if (genitore != null && logopedistaPath != null) {
            btnRegistraBambino.setOnClickListener(v -> showRegistraBambinoDialog());
        }

        return view;
    }

    private void showBambiniList() {
        Log.d(TAG, "showBambiniList");
//        Fragment fragment = requireActivity().getSupportFragmentManager().findFragmentByTag("listaBambiniFragment");
        final FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        final Fragment content = fragmentManager.findFragmentById(R.id.fragmentContainerBambini);
        if (content == null || !(content instanceof BambiniListFragment)) {
            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            final BambiniListFragment myFragment = BambiniListFragment.newInstance(genitorePath);
            fragmentTransaction.replace(R.id.fragmentContainerBambini, myFragment, "MyFragment");
            fragmentTransaction.commitAllowingStateLoss();
        Log.d(TAG, "Fragment: " + myFragment);
        }
//        if (fragment == null) {
//            fragment = BambiniListFragment.newInstance(genitorePath);
//            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
//            transaction.add(R.id.fragmentContainerBambini, fragment, "listaBambiniFragment");
//            transaction.commit();
//            //pop dello stack
//            requireActivity().getSupportFragmentManager().popBackStack();
//        } else {
//            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
//            transaction.replace(R.id.fragmentContainerBambini, fragment, "listaBambiniFragment");
//            transaction.commit();
//        }
//        BambiniListFragment bambiniListFragment = BambiniListFragment.newInstance(genitorePath);
//        requireActivity().getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragmentContainerBambini, bambiniListFragment)
//                .commit();
    }

    private void showRegistraBambinoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Registra Bambino");

        View view = getLayoutInflater().inflate(R.layout.layout_dialog_registra_bambino, null);

        EditText etNome = view.findViewById(R.id.editTextNome);
        EditText etCognome = view.findViewById(R.id.editTextCognome);
        EditText etEta = view.findViewById(R.id.editTextEta);
        RadioGroup radioGroupSesso = view.findViewById(R.id.radioGroupSesso);

        builder.setView(view);

        builder.setPositiveButton("Registra", (dialog, which) -> {
                    String nome = etNome.getText().toString();
                    String cognome = etCognome.getText().toString();
                    String eta = etEta.getText().toString();
                    String sesso = "";

                    int selectedId = radioGroupSesso.getCheckedRadioButtonId();
                    if (selectedId == R.id.radioButtonMaschio) sesso = "M";
                    else if (selectedId == R.id.radioButtonFemmina) sesso = "F";

                    db = FirebaseFirestore.getInstance();
                    if (!nome.isEmpty() && !cognome.isEmpty() && !eta.isEmpty() && !sesso.isEmpty()) {
                        List<String> ownedAvatars = new ArrayList<>();
                        ownedAvatars.add("1");
                        Map<String, Object> child = new HashMap<>();
                        child.put("nome", nome);
                        child.put("cognome", cognome);
                        child.put("eta", Integer.parseInt(eta));
                        child.put("sesso", sesso);
                        child.put("tipologia", 0);
                        child.put("avatarCorrente", "1");
                        child.put("tema", "supereroi");
                        child.put("ownedAvatars", ownedAvatars);
                        child.put("progresso", 0);
                        child.put("coins", 0);
                        child.put("logopedistaRef", db.document(logopedistaPath));
                        child.put("esercizioTipo1", db.document("/esercizi/placeholder/tipo1/16-08-2024")); //placeholder
                        child.put("esercizioTipo2", db.document("/esercizi/placeholder/tipo2/16-08-2024")); //placeholder
                        child.put("esercizioTipo3", db.document("/esercizi/placeholder/tipo3/16-08-2024")); //placeholder
                        ChildHelper.addBambinoToFirestore(child, genitorePath, logopedistaPath)
                                .addOnSuccessListener(task -> {
                                    Toast.makeText(getContext(), "Bambino registrato con successo!", Toast.LENGTH_SHORT).show();
                                    showBambiniList();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Errore nella registrazione del bambino", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Errore aggiungendo il bambino: ", e);
                                });
                    } else {
                        Toast.makeText(getContext(), "Inserisci tutti i dati", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annulla", (dialog, which) -> dialog.cancel())
                .show();
    }
}