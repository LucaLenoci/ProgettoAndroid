package com.example.progetto_mobile;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class HomeEserciziBambinoFragment extends Fragment {
    private static final String TAG = "HomeEserciziBambinoActivity";
    private ImageView movableObject;
    private float centerX, centerY;
    private List<Button> buttons;  // List to hold all the buttons
    private boolean isNavigating = false;  // Flag to prevent multiple intents

    private String selectedDate;
    private String bambinoId;
    private FirebaseFirestore db;

    private ImageView spine1, spine2, spine3;  // Aggiungi riferimenti alle immagini PNG
    private float initialX, initialY;  // Posizione iniziale dell'oggetto movibile


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.esercizi_giornalieri_bambino, container, false);

        selectedDate = getArguments().getString("selectedDate");
        bambinoId = getArguments().getString("bambinoId");

        db = FirebaseFirestore.getInstance();

        fetchTema();
        JoystickView joystick = view.findViewById(R.id.joystick);
        movableObject = view.findViewById(R.id.movable_object);

        // Usa ViewTreeObserver per ottenere la posizione iniziale una volta che il layout è pronto
        movableObject.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                initialX = movableObject.getX();
                initialY = movableObject.getY();

                // Rimuovi l'ascoltatore per evitare di ripetere l'inizializzazione
                movableObject.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        // Inizializza le immagini PNG
        spine1 = view.findViewById(R.id.spine1);
        spine2 = view.findViewById(R.id.spine2);
        spine3 = view.findViewById(R.id.spine3);

        // Initialize the list of buttons and add your buttons to the list
        buttons = new ArrayList<>();
        buttons.add(view.findViewById(R.id.button1));  // Add your buttons by their IDs
        buttons.add(view.findViewById(R.id.button2));
        buttons.add(view.findViewById(R.id.button3));
        buttons.add(view.findViewById(R.id.button4));

        // Set onClickListeners for each button to navigate to a different view
        for (int i = 0; i < buttons.size(); i++) {
            final int buttonNumber = i + 1; // Button numbers are 1-based
            buttons.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isNavigating) {  // Only navigate if not already navigating
                        isNavigating = true;
                        navigateToView(buttonNumber, selectedDate, bambinoId);
                    }
                }
            });
        }

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) movableObject.getLayoutParams();
        centerX = params.leftMargin;
        centerY = params.topMargin;

        joystick.setJoystickListener(new JoystickView.JoystickListener() {
            @Override
            public void onJoystickMoved(float x, float y) {
                moveObject(x, y);
            }
        });

        // Check the status of each exercise and disable/green the button if completed
        checkExerciseStatus();

        // Load the current avatar for the child and set it as the image for the movable object
        loadCurrentAvatar(bambinoId);
        return view;  // Return the inflated view
    }

    private void moveObject(float x, float y) {
        float deltaX = x * 10; // Adjust the multiplier to control speed
        float deltaY = y * 10; // Adjust the multiplier to control speed

        // Calculate the new position
        float newX = movableObject.getX() + deltaX;
        float newY = movableObject.getY() + deltaY;

        // Get the parent layout dimensions
        ConstraintLayout parentLayout = (ConstraintLayout) movableObject.getParent();
        int parentWidth = parentLayout.getWidth();
        int parentHeight = parentLayout.getHeight();

        // Ensure the object stays within the bounds of the parent layout
        newX = Math.max(0, Math.min(newX, parentWidth - movableObject.getWidth()));
        newY = Math.max(0, Math.min(newY, parentHeight - movableObject.getHeight()));

        // Move the object
        movableObject.setX(newX);
        movableObject.setY(newY);

        // Check for collisions with each button
        for (Button button : buttons) {
            if (isColliding(movableObject, button)) {
                button.performClick();  // Trigger button click if colliding
                break;  // Stop checking other buttons after a collision is detected
            }
        }

        // Contolla collisione con le spine
        if (isColliding(movableObject, spine1) || isColliding(movableObject, spine2) || isColliding(movableObject, spine3)) {
            resetMovableObjectPosition();  // Torna alla posizione iniziale se c'è una collisione
        }
    }

    private void resetMovableObjectPosition() {
        movableObject.setX(initialX);
        movableObject.setY(initialY);
    }

    private boolean isColliding(View object1, View object2) {
        // Get the locations of both objects on the screen
        int[] location1 = new int[2];
        int[] location2 = new int[2];

        object1.getLocationOnScreen(location1);
        object2.getLocationOnScreen(location2);

        // Get the actual width and height of both objects
        int object1Width = object1.getWidth();
        int object1Height = object1.getHeight();
        int object2Width = object2.getWidth();
        int object2Height = object2.getHeight();

        // Calculate the top-left and bottom-right corners of the bounding boxes
        int x1 = location1[0] + object1Width / 4; // Add an offset to shrink the bounding box
        int y1 = location1[1] + object1Height / 4;
        int object1Right = x1 + (object1Width / 2); // Shrink width by half
        int object1Bottom = y1 + (object1Height / 2); // Shrink height by half

        int x2 = location2[0] + object2Width / 4; // Add an offset to shrink the bounding box
        int y2 = location2[1] + object2Height / 4;
        int object2Right = x2 + (object2Width / 2); // Shrink width by half
        int object2Bottom = y2 + (object2Height / 2); // Shrink height by half

        // Check if the bounding boxes overlap
        return x1 < object2Right &&
                object1Right > x2 &&
                y1 < object2Bottom &&
                object1Bottom > y2;
    }

    private void checkExerciseStatus() {
        // Check the status of each exercise and disable/green the button if completed
        for (int i = 0; i < buttons.size(); i++) {
            final int buttonNumber = i + 1;
            String exerciseDocId = getExerciseDocumentId(buttonNumber);

            db.collection("esercizi")
                    .document(bambinoId)
                    .collection("tipo"+buttonNumber)
                    .document(selectedDate)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists() && document.getBoolean("esercizio_corretto") != null && document.getBoolean("esercizio_corretto")) {
                                // If the exercise is correct, disable the button and set its background color to green
                                Button button = buttons.get(buttonNumber - 1);
                                button.setEnabled(false);
                                button.setTextColor(Color.GREEN);
                            }
                        }
                    });
        }
    }

    private String getExerciseDocumentId(int buttonNumber) {
        // Map the button number to the corresponding exercise document ID
        switch (buttonNumber) {
            case 1:
                return "esercizio1";  // Replace with your actual document IDs
            case 2:
                return "esercizio2";
            case 3:
                return "esercizio3";
            case 4:
                return "esercizio4";
            default:
                return null;
        }
    }

    private void navigateToView(int buttonNumber, String selectedDate, String bambinoId) {
        Fragment fragment = null;

        switch (buttonNumber) {
            case 1:
                fragment = new DenominazioneImmaginiFragment();
                break;
            case 2:
                fragment = new DenominazioneImmaginiFragment();
                break;
            case 3:
                fragment = new DenominazioneImmaginiFragment();
                break;
            case 4:
                fragment = new DenominazioneImmaginiFragment();
                break;
        }

        if (fragment != null) {
            Bundle bundle = new Bundle();
            bundle.putString("bambinoId", bambinoId);
            bundle.putString("selectedDate", selectedDate);
            fragment.setArguments(bundle);

            if (getParentFragmentManager() != null) {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        }

        // Delay resetting isNavigating flag to prevent multiple clicks
        new Handler(Looper.getMainLooper()).postDelayed(() -> isNavigating = false, 2000);
    }

    private void loadCurrentAvatar(String bambinoId) {
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
                                                        Glide.with(getContext()).load(uri).into(movableObject);
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

    private void fetchTema() {
        db.collection("bambini").document(bambinoId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String temaCorrente =  document.getString("tema");
                            updateConstraintLayoutBackground(temaCorrente);
                        } else {
                            Log.e(TAG, "No such document for " + bambinoId);
                        }
                    } else {
                        Log.e(TAG, "Firestore get failed for " + bambinoId, task.getException());
                    }
                });
    }

    public void updateConstraintLayoutBackground(String theme) {
        ConstraintLayout constraintLayout = getView().findViewById(R.id.relativeLayout);// Your ConstraintLayout

        int backgroundColor = 0;

        // Set background color based on the theme
        switch (theme) {
            case "supereroi":
            case "cartoni_animati":
                backgroundColor = ContextCompat.getColor(getContext(), R.color.supereroibackground); // Replace with actual color resource
                break;
            case "favole":
            case "videogiochi":
                backgroundColor = ContextCompat.getColor(getContext(), R.color.videogiochibackground); // Replace with actual color resource
                break;
        }

        // Apply the background color to the ConstraintLayout
        constraintLayout.setBackgroundColor(backgroundColor);
    }

}
