package com.example.progetto_mobile;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DashboardGenitoreActivity extends AppCompatActivity {

    private static final String TAG = "DashboardGenitoreActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard_genitore);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Genitore genitore = (Genitore) getIntent().getSerializableExtra("genitore");
        Log.d(TAG, "onCreate: " + genitore);
        if (genitore != null) {
            String genitorePath = genitore.getGenitoreRef();
            BambiniListFragment bambiniListFragment = BambiniListFragment.newInstance(genitorePath);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerBambini, bambiniListFragment)
                    .commit();
        }
    }
}