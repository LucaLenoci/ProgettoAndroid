package com.example.progetto_mobile;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AvatarActivity extends AppCompatActivity {

    private static final String TAG = "AvatarActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ImageView avatar1, avatar2, avatar3, avatar4;
    private TextView textAvatar1, textAvatar2, textAvatar3, textAvatar4;
    private Button buttonAvatar1, buttonAvatar2, buttonAvatar3, buttonAvatar4;
    String bambinoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compra_seleziona_avatar);
        bambinoId = getIntent().getStringExtra("bambinoId");


        // Initialize views
        avatar1 = findViewById(R.id.avatar1);
        avatar2 = findViewById(R.id.avatar2);
        avatar3 = findViewById(R.id.avatar3);
        avatar4 = findViewById(R.id.avatar4);

        textAvatar1 = findViewById(R.id.textavatar1);
        textAvatar2 = findViewById(R.id.textavatar2);
        textAvatar3 = findViewById(R.id.textavatar3);
        textAvatar4 = findViewById(R.id.textavatar4);

        buttonAvatar1 = findViewById(R.id.buttonavatar1);
        buttonAvatar2 = findViewById(R.id.buttonavatar2);
        buttonAvatar3 = findViewById(R.id.buttonavatar3);
        buttonAvatar4 = findViewById(R.id.buttonavatar4);

        // Load data from Firebase
        loadAvatarData("1", avatar1, textAvatar1, buttonAvatar1);
        loadAvatarData("2", avatar2, textAvatar2, buttonAvatar2);
        loadAvatarData("3", avatar3, textAvatar3, buttonAvatar3);
        loadAvatarData("4", avatar4, textAvatar4, buttonAvatar4);
        fetchBambinoCoins(bambinoId);
    }

    private void loadAvatarData(String avatarKey, ImageView imageView, TextView textView, Button button) {


        db.collection("avatars").document(avatarKey).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String imagePath = document.getString("imageUrl");
                            String name = document.getString("name");
                            Long coinsCostLong = document.getLong("coinsCost");
                            Integer coinsCost = (coinsCostLong != null) ? coinsCostLong.intValue() : null;

                            Log.d(TAG, "Loading data for " + avatarKey);
                            Log.d(TAG, "Image path: " + imagePath);
                            Log.d(TAG, "Name: " + name);
                            Log.d(TAG, "Coins cost: " + coinsCost);

                            if (imagePath != null && !imagePath.isEmpty()) {
                                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imagePath);
                                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    Log.d(TAG, "Image URL retrieved successfully for " + avatarKey);
                                    Glide.with(AvatarActivity.this).load(uri).into(imageView);
                                }).addOnFailureListener(exception -> {
                                    Log.e(TAG, "Failed to get download URL for " + avatarKey, exception);
                                });
                            } else {
                                Log.e(TAG, "Image path is null or empty for " + avatarKey);
                            }

                            if (name != null) {
                                textView.setText(name);
                            } else {
                                textView.setText("Name not available");
                                Log.e(TAG, "Name is null for " + avatarKey);
                            }

                            if (coinsCost != null) {
                                button.setText(String.valueOf(coinsCost));
                            } else {
                                button.setText("0");
                                Log.e(TAG, "Coins cost is null for " + avatarKey);
                            }
                        } else {
                            Log.d(TAG, "No such document for " + avatarKey);
                        }
                    } else {
                        Log.e(TAG, "Firestore get failed for " + avatarKey, task.getException());
                    }
                });
    }

    private void fetchBambinoCoins(String bambinoId) {
        // Reference to the Firestore collection and document
        db.collection("bambini").document(bambinoId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Long coins = document.getLong("coins");
                            if (coins != null) {
                                // Update TextView with the number of coins
                                TextView coinsTextView = findViewById(R.id.moneteBambino);
                                coinsTextView.setText(String.valueOf(coins));
                                coinsTextView.setTextSize(24.0F);
                            } else {
                                // Handle the case where coins field is null
                                Log.e(TAG, "Coins field is null for " + bambinoId);
                                TextView coinsTextView = findViewById(R.id.moneteBambino);
                                coinsTextView.setText("0");
                            }
                        } else {
                            // Handle the case where the document does not exist
                            Log.d(TAG, "No such document for " + bambinoId);
                            TextView coinsTextView = findViewById(R.id.moneteBambino);
                            coinsTextView.setText("0");
                        }
                    } else {
                        // Handle the case where the task fails
                        Log.e(TAG, "Firestore get failed for " + bambinoId, task.getException());
                        TextView coinsTextView = findViewById(R.id.moneteBambino);
                        coinsTextView.setText("0");
                    }
                });
    }


}
