package com.example.progetto_mobile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class HomeGenitoreFragment extends Fragment {

    private static final String TAG = "HomeGenitoreFragment";
    private static final String ARG_GENITORE_PATH = "genitore_path";
    private String genitorePath;


    // Factory method to create a new instance of the fragment with arguments
    public static HomeGenitoreFragment newInstance(String genitorePath) {
        HomeGenitoreFragment fragment = new HomeGenitoreFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GENITORE_PATH, genitorePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_home_genitore, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvBenvenuto = view.findViewById(R.id.textViewBenvenutoGenitore);

        if (getArguments() != null) {
            genitorePath = getArguments().getString("genitore");
        }

        if (genitorePath != null) {
            FirebaseFirestore.getInstance().document(genitorePath).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + documentSnapshot.getData());
                            String nome = documentSnapshot.getString("nome");
                            String cognome = documentSnapshot.getString("cognome");
                            tvBenvenuto.setText(String.format("Benvenuto/a, %s %s", nome, cognome));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Errore nel recupero dei dati del genitore", e);
                        Toast.makeText(getContext(), "Errore nel recupero dei dati del genitore", Toast.LENGTH_SHORT).show();
                    });
        }

        BambiniListFragment bambiniListFragment = BambiniListFragment.newInstance(genitorePath);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerBambini, bambiniListFragment)
                .commit();
        return view;
    }
}
