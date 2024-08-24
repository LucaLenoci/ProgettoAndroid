package com.example.progetto_mobile;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BambinoClassificaActivity extends AppCompatActivity {

    private static final String TAG = "BambinoClassificaActivity";
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private BambinoRankingAdapter adapter;
    private List<Child> bambinoList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classifica);

        Log.d(TAG, "onCreate: Initializing BambinoClassificaActivity");

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerViewRanking);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bambinoList = new ArrayList<>();
        adapter = new BambinoRankingAdapter(bambinoList);
        recyclerView.setAdapter(adapter);

        Log.d(TAG, "onCreate: Fetching bambini data");
        fetchBambiniAndDisplayRanking();
    }

    private void fetchBambiniAndDisplayRanking() {
        db.collection("bambini")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "fetchBambiniAndDisplayRanking: Successfully fetched bambini data");

                        bambinoList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("nome");
                            Long coins = document.getLong("coins");

                            Log.d(TAG, "fetchBambiniAndDisplayRanking: Bambino - Name: " + name + ", Coins: " + coins);

                            if (name != null && coins != null) {
                                bambinoList.add(new Child(name, coins.intValue()));
                            } else {
                                Log.e(TAG, "fetchBambiniAndDisplayRanking: Null value found for name or coins in document " + document.getId());
                            }
                        }

                        // Sort the list by coins in descending order
                        Collections.sort(bambinoList, (b1, b2) -> Integer.compare(b2.getCoins(), b1.getCoins()));
                        Log.d(TAG, "fetchBambiniAndDisplayRanking: Bambini list sorted by coins");

                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "fetchBambiniAndDisplayRanking: Error fetching bambini data", task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "fetchBambiniAndDisplayRanking: Firestore operation failed", e));
    }
}
