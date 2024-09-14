package com.example.progetto_mobile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

// todo: aggiustare il cropping. quando ci sono troppi bambini, vengono tagliati
public class ClassificaBambiniActivity extends AppCompatActivity {

    private static final String TAG = "ClassificaBambiniActivity";
    private LinearLayout linearLayoutBambini;
    private int childCount = 0;
    private List<Child> childList;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_classifica_bambini);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        linearLayoutBambini = findViewById(R.id.linearLayoutBambini);
        childList = new ArrayList<>();

        String logopedistaPath = getIntent().getStringExtra("logopedista");
        getBambiniFromFirestore(logopedistaPath);
    }

    private void getBambiniFromFirestore(String logopedistaPath) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.document(logopedistaPath)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Dati presi: " + documentSnapshot);
                        List<DocumentReference> bambiniRefs = (List<DocumentReference>) documentSnapshot.get("bambiniRef");

                        if (bambiniRefs == null || bambiniRefs.isEmpty()) {
                            showEmptyChildrenMessage();
                        } else {
                            List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                            for (DocumentReference bambinoRef : bambiniRefs) {
                                tasks.add(bambinoRef.get());
                            }

                            Tasks.whenAllComplete(tasks)
                                    .addOnCompleteListener(task -> {
                                        for (Task<DocumentSnapshot> bambinoTask : tasks) {
                                            if (bambinoTask.isSuccessful()) {
                                                DocumentSnapshot bambinoSnapshot = bambinoTask.getResult();
                                                if (bambinoSnapshot.exists()) {
                                                    String nome = bambinoSnapshot.getString("nome");
                                                    String cognome = bambinoSnapshot.getString("cognome");
                                                    int coins = bambinoSnapshot.getLong("coins").intValue();
                                                    String id = bambinoSnapshot.getId();

                                                    Child child = new Child(id, nome, cognome, coins);
                                                    childList.add(child);
                                                }
                                            } else {
                                                Log.d(TAG, "Errore nel recupero dei dati di un bambino", bambinoTask.getException());
                                            }
                                        }
                                        displaySortedChildren();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> Log.d(TAG, "Errore nel recupero dei dati di: " + logopedistaPath, e));
    }

    private void displaySortedChildren() {
        if (!childList.isEmpty()) {
            // todo: comparare in base all'esperienza
            childList.sort((child1, child2) -> Integer.compare(child2.getCoins(), child1.getCoins()));
            for (Child child : childList)
                addChildItemList(child);
        } else showEmptyChildrenMessage();
    }

    private void showEmptyChildrenMessage() {
        TextView tvNessunBambino = new TextView(this);
        tvNessunBambino.setText("Nessun bambino trovato.");
        tvNessunBambino.setTextSize(18);
        linearLayoutBambini.addView(tvNessunBambino);
    }

    private void addChildItemList(Child child) {
        // Crea il LayoutInflater per aggiungere il nuovo item alla lista
        Log.d("addChildItemList", "Inizio creazione item per bambino: " + child.getNome() + " " + child.getCognome());

        LayoutInflater inflater = LayoutInflater.from(this);
        View childItem = inflater.inflate(R.layout.layout_child_item_list, linearLayoutBambini, false);

        // Trova i componenti nel layout
        TextView textViewChildPosition = childItem.findViewById(R.id.textViewPosition);
        TextView textViewChildName = childItem.findViewById(R.id.textViewChildName);
        TextView textViewChildCoins = childItem.findViewById(R.id.textViewChildCoins);
        ImageView profilePic = childItem.findViewById(R.id.imageViewChild);

        // Imposta i valori per i TextView
        textViewChildPosition.setText(String.format("#%d", ++childCount));
        Log.d("addChildItemList", "Posizione bambino settata: " + childCount);

        textViewChildName.setText(String.format("%s %s", child.getNome(), child.getCognome()));
        Log.d("addChildItemList", "Nome bambino settato: " + child.getNome() + " " + child.getCognome());

        textViewChildCoins.setText(String.format("%s: %d", "Coins", child.getCoins()));
        Log.d("addChildItemList", "Monete bambino settate: " + child.getCoins());

        // Imposta l'immagine del profilo
        String bambinoIdraw = child.getDocId();
        loadCurrentAvatar(bambinoIdraw, profilePic);
        Log.d("addChildItemList", "Avatar caricato per bambino con ID: " + bambinoIdraw);

        // Aggiungi la vista al layout
        linearLayoutBambini.addView(childItem);
        Log.d("addChildItemList", "Item bambino aggiunto alla lista.");
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
                                                        Glide.with(ClassificaBambiniActivity.this).load(uri).into(ProfilePic);
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