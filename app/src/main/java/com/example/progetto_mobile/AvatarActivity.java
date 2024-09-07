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

import java.util.ArrayList;
import java.util.List;

public class AvatarActivity extends AppCompatActivity {

    private static final String TAG = "AvatarActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ImageView avatar1, avatar2, avatar3, avatar4, coinsimageavatar1, coinsimageavatar2, coinsimageavatar3, coinsimageavatar4;
    private TextView textAvatar1, textAvatar2, textAvatar3, textAvatar4;
    private Button buttonAvatar1, buttonAvatar2, buttonAvatar3, buttonAvatar4;
    private String bambinoId;
    private String avatarCorrente;
    private String temaCorrente;
    String sessoBambino;
    String personaggi_da_visualizzare;
    private int bambinoCoins = 0;  // Store the bambino's current coin balance

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

        coinsimageavatar1 = findViewById(R.id.coinavatar1);
        coinsimageavatar2 = findViewById(R.id.coinavatar2);
        coinsimageavatar3 = findViewById(R.id.coinavatar3);
        coinsimageavatar4 = findViewById(R.id.coinavatar4);

        // Fetch and display bambino's current coin balance, then load avatar data
        fetchBambinoCoinsAndLoadAvatars();
    }

    private void fetchBambinoCoinsAndLoadAvatars() {
        db.collection("bambini").document(bambinoId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Long coins = document.getLong("coins");
                            TextView coinsTextView = findViewById(R.id.moneteBambino);
                            if (coins != null) {
                                bambinoCoins = coins.intValue();
                                coinsTextView.setText(String.valueOf(bambinoCoins));
                                coinsTextView.setTextSize(24.0F);
                            } else {
                                Log.e(TAG, "Coins field is null for " + bambinoId);
                                coinsTextView.setText("0");
                            }
                        } else {
                            Log.d(TAG, "No such document for " + bambinoId);
                            TextView coinsTextView = findViewById(R.id.moneteBambino);
                            coinsTextView.setText("0");
                        }
                        // After fetching coins, load the avatars
                        fetchAvatarCorrenteAndLoadData();
                    } else {
                        Log.e(TAG, "Firestore get failed for " + bambinoId, task.getException());
                        TextView coinsTextView = findViewById(R.id.moneteBambino);
                        coinsTextView.setText("0");
                    }
                });
    }

    private void fetchAvatarCorrenteAndLoadData() {
        db.collection("bambini").document(bambinoId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            avatarCorrente = document.getString("avatarCorrente");
                            temaCorrente = document.getString("tema");
                            sessoBambino = document.getString("sesso");
                            personaggi_da_visualizzare = sessoBambino.equals("M") ? "personaggi" : "personaggi_femminili";

                            // Get owned avatars
                            List<String> ownedAvatars = (List<String>) document.get("ownedAvatars");
                            if (ownedAvatars == null) {
                                ownedAvatars = new ArrayList<>();  // Initialize if null
                            }

                            // Load avatar data and check ownership and selection
                            loadAvatarData("1", avatar1, textAvatar1, buttonAvatar1, coinsimageavatar1, ownedAvatars);
                            loadAvatarData("2", avatar2, textAvatar2, buttonAvatar2, coinsimageavatar2, ownedAvatars);
                            loadAvatarData("3", avatar3, textAvatar3, buttonAvatar3, coinsimageavatar3, ownedAvatars);
                            loadAvatarData("4", avatar4, textAvatar4, buttonAvatar4, coinsimageavatar4, ownedAvatars);
                        } else {
                            Log.e(TAG, "No such document for " + bambinoId);
                        }
                    } else {
                        Log.e(TAG, "Firestore get failed for " + bambinoId, task.getException());
                    }
                });
    }

    private void loadAvatarData(String avatarKey, ImageView imageView, TextView textView, Button button, ImageView coinimageView, List<String> ownedAvatars) {
        db.collection("avatars").document(temaCorrente).collection(personaggi_da_visualizzare).document(avatarKey).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String imagePath = document.getString("imageUrl");
                            String name = document.getString("name");
                            Long coinsCostLong = document.getLong("coinCost");
                            Integer coinsCost = (coinsCostLong != null) ? coinsCostLong.intValue() : 0;

                            // Set the avatar image and name
                            if (imagePath != null && !imagePath.isEmpty()) {
                                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imagePath);
                                imageRef.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(AvatarActivity.this).load(uri).into(imageView));
                            }

                            textView.setText(name != null ? name : "Name not available");

                            // Check if the avatar is already owned
                            if (ownedAvatars.contains(avatarKey)) {
                                coinimageView.setVisibility(TextView.INVISIBLE);  // Hide coin image for owned avatars

                                if (avatarCorrente != null && avatarCorrente.equals(avatarKey)) {
                                    button.setText("Selected");
                                    button.setClickable(false);  // Disable button if this is the current avatar
                                } else {
                                    button.setText("Select");
                                    button.setClickable(true);
                                    button.setOnClickListener(v -> selectAvatar(avatarKey));  // Set click listener to select the avatar
                                }
                            } else {
                                // Show the price and buying option if the avatar is not owned
                                coinimageView.setVisibility(TextView.VISIBLE);
                                button.setText(String.valueOf(coinsCost));

                                if (coinsCost <= bambinoCoins) {
                                    button.setClickable(true);
                                    button.setOnClickListener(v -> buyAvatar(avatarKey, coinsCost));  // Set click listener to buy the avatar
                                } else {
                                    button.setClickable(false);  // Disable if not enough coins
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "Firestore get failed for " + avatarKey, task.getException());
                    }
                });
    }


    private void buyAvatar(String avatarKey, int coinsCost) {
        db.collection("bambini").document(bambinoId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Get current coins and owned avatars
                            Long currentCoinsLong = document.getLong("coins");
                            List<String> ownedAvatars = (List<String>) document.get("ownedAvatars");
                            if (ownedAvatars == null) {
                                ownedAvatars = new ArrayList<>();
                            }

                            int currentCoins = currentCoinsLong != null ? currentCoinsLong.intValue() : 0;

                            if (currentCoins >= coinsCost) {
                                // Update Firestore: Subtract coins and add the avatar to ownedAvatars
                                int newCoins = currentCoins - coinsCost;
                                ownedAvatars.add(avatarKey);

                                db.collection("bambini").document(bambinoId)
                                        .update("coins", newCoins, "ownedAvatars", ownedAvatars)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Avatar purchased successfully: " + avatarKey);
                                            fetchBambinoCoinsAndLoadAvatars();  // Refresh UI
                                        })
                                        .addOnFailureListener(e -> Log.e(TAG, "Error updating avatar purchase", e));
                            } else {
                                Log.e(TAG, "Not enough coins to purchase avatar");
                            }
                        }
                    } else {
                        Log.e(TAG, "Failed to retrieve bambino data", task.getException());
                    }
                });
    }


    private void selectAvatar(String avatarKey) {
        db.collection("bambini").document(bambinoId)
                .update("avatarCorrente", avatarKey)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Avatar successfully updated to " + avatarKey);
                    fetchAvatarCorrenteAndLoadData();  // Refresh UI to show the selected avatar
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error updating avatar", e));
    }

}
