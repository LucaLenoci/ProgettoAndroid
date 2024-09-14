package com.example.progetto_mobile;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class HomeBambinoFragment extends Fragment {

    private static final String TAG = "HomeBambinoFragment";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String selectedDate;
    private TextView tvNome, tvCoins;
    private ProgressBar progressBar;
    private ImageView ProfilePic;
    private String bambinoId;
    private int currentStreak = 0;
    private TextView numerostreak;
    private Button classifica;
    CalendarView calendarView;

    public static HomeBambinoFragment newInstance(String bambinoId) {
        HomeBambinoFragment fragment = new HomeBambinoFragment();
        Bundle args = new Bundle();
        args.putString("bambinoId", bambinoId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_bambino, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentStreak = 0;

        ProfilePic = view.findViewById(R.id.ProfilePic);
        numerostreak = view.findViewById(R.id.textView3);

        if (getArguments() != null) {
            bambinoId = getArguments().getString("bambinoId");
        }

        tvNome = view.findViewById(R.id.Nome);
        tvCoins = view.findViewById(R.id.Coins);
        progressBar = view.findViewById(R.id.progressBar);

        calendarView = view.findViewById(R.id.calendarView);
        // Ottieni la data corrente
        Calendar calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();

        // Imposta la data minima e massima per la selezione
        calendarView.setMinDate(currentTime);
        calendarView.setMaxDate(currentTime);
        Button esercizioButton = view.findViewById(R.id.button);

        // Set default selected date to the current date
        selectedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        // Listener to capture the selected date
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            // Month is 0-based, so add 1
            int correctedMonth = month + 1;
            selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d", dayOfMonth, correctedMonth, year);
        });

        getChildFromFirestore();
        fetchTema();
        loadCurrentAvatar(bambinoId);

        esercizioButton.setOnClickListener(v -> checkAndProceedToExercises());
        classifica = view.findViewById(R.id.button5);
        classifica.setOnClickListener(v -> {
            getLogopedistaPathFromBambino(bambinoId)
                    .addOnSuccessListener(logopedistaPath -> {
                        Fragment fragment = new ClassificaBambiniFragment();

                        // Create a new Bundle and add data to it
                        Bundle bundle = new Bundle();
                        bundle.putString("logopedista", logopedistaPath);

                        // Set the arguments for the fragment
                        fragment.setArguments(bundle);

                        // Begin the fragment transaction and replace the container with the new fragment
                        if (getParentFragmentManager() != null) {
                            getParentFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    })
                    .addOnFailureListener(e -> Log.e("ClassificaClick", "Errore nel recuperare il path del logopedista", e));
        });


        ProfilePic.setOnClickListener(v -> {
            Fragment fragment;
            fragment = new AvatarFragment();
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
        });

    }

    // -- IL TIPO DI RITORNO 'TASK' TI PERMETTE DI FARE '.addOnSuccessListener' E '.addOnFailureListener'
    private Task<String> getLogopedistaPathFromBambino(String bambinoId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        return db.collection("bambini")
                .document(bambinoId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        DocumentReference logopedistaRef = documentSnapshot.getDocumentReference("logopedistaRef");

                        if (logopedistaRef != null) {
                            return logopedistaRef.getPath();
                        } else {
                            throw new Exception("logopedistaRef non trovato.");
                        }
                    } else {
                        throw new Exception("Documento bambino non trovato o errore nella query.");
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        currentStreak = 0;

        getChildFromFirestore();
        loadCurrentAvatar(bambinoId);
        fetchTema();
        calculateStreak();
    }

    private void checkAndProceedToExercises() {
        checkIfExerciseExists(selectedDate, new HomeBambinoFragment.FirestoreCallback() {
            @Override
            public void onCallback(boolean hasExercises) {
                if (hasExercises) {
                    HomeEserciziBambinoFragment fragment = new HomeEserciziBambinoFragment();

// Pass data to the fragment using arguments
                    Bundle args = new Bundle();
                    args.putString("selectedDate", selectedDate);
                    args.putString("bambinoId", bambinoId);
                    fragment.setArguments(args);

// Start the fragment
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, fragment); // Use the ID of your container
                    transaction.addToBackStack(null); // Optional: add to back stack if you want to be able to navigate back
                    transaction.commit();
                } else {
                    showNoExercisesPopup();
                }
            }
        });
    }

    private void checkIfExerciseExists(String date, HomeBambinoFragment.FirestoreCallback callback) {
        // Reference to the specific document based on the date
        db.collection("esercizi")
                .document(bambinoId)
                .collection("tipo1")
                .document(date)
                .get()
                .addOnCompleteListener(task -> {
                    boolean exists = task.isSuccessful() && task.getResult().exists();

                    if (callback != null) {
                        // Invoke the callback with the result
                        db.collection("esercizi")
                                .document(bambinoId)
                                .collection("tipo2")
                                .document(date)
                                .get()
                                .addOnCompleteListener(task_2 -> {
                                    boolean exists_2 = task_2.isSuccessful() && task_2.getResult().exists();

                                    if (callback != null) {
                                        // Invoke the callback with the result
                                        db.collection("esercizi")
                                                .document(bambinoId)
                                                .collection("tipo3")
                                                .document(date)
                                                .get()
                                                .addOnCompleteListener(task_3 -> {
                                                    boolean exists_3 = task_3.isSuccessful() && task_3.getResult().exists();

                                                    if (callback != null) {
                                                        // Invoke the callback with the result
                                                        callback.onCallback(exists);
                                                    }

                                                    if (!exists_3) {
                                                        Log.d(TAG, "No exercise found for the date: " + date);
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error checking exercise for date: " + date, e);

                                                    if (callback != null) {
                                                        // Notify the callback about the failure
                                                        callback.onCallback(false);
                                                    }
                                                });
                                    }

                                    if (!exists_2) {
                                        Log.d(TAG, "No exercise found for the date: " + date);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error checking exercise for date: " + date, e);

                                    if (callback != null) {
                                        // Notify the callback about the failure
                                        callback.onCallback(false);
                                    }
                                });
                    }

                    if (!exists) {
                        Log.d(TAG, "No exercise found for the date: " + date);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking exercise for date: " + date, e);

                    if (callback != null) {
                        // Notify the callback about the failure
                        callback.onCallback(false);
                    }
                });
    }


    private void showNoExercisesPopup() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Nessun esercizio")
                .setMessage("Non ci sono esercizi per la data selezionata.")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void getChildFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the specific document with ID "1" in the "bambini" collection

        // Fetch the document
        db.collection("bambini").document(bambinoId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Document exists, retrieve data
                        Map<String, Object> data = documentSnapshot.getData();

                        if (data != null) {
                            Log.d("FirestoreData", "Document data: " + data.toString());

                            String nomeString = (String) data.get("nome");
                            Long coinsLong = (Long) data.get("coins");

                            if (coinsLong != null) {
                                String coins = coinsLong.toString();
                                Log.d("FirestoreData", coins);
                                tvCoins.setText("Monete Attuali: "+coins);
                            } else {
                                Log.d("FirestoreData", "Coins not found");
                            }

                            if (nomeString != null) {
                                tvNome.setText("Nome: "+nomeString);
                            } else {
                                Log.d("FirestoreData", "Nome not found");
                            }
                        } else {
                            Log.d("FirestoreData", "No data found in the document");
                        }
                    } else {
                        Log.d("FirestoreData", "No such document");
                        Toast.makeText(requireContext(), "Documento non trovato", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error fetching document", e);
                    Toast.makeText(requireContext(), "Errore di lettura documento", Toast.LENGTH_SHORT).show();
                });
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
                                                        Glide.with(requireContext()).load(uri).into(ProfilePic);
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

    private interface FirestoreCallback {
        void onCallback(boolean hasExercises);
    }

    private void fetchTema() {
        db.collection("bambini").document(bambinoId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String temaCorrente =  document.getString("tema");
                            updateRoundRectColors(temaCorrente);
                            updateConstraintLayoutBackground(temaCorrente);
                        } else {
                            Log.e(TAG, "No such document for " + bambinoId);
                        }
                    } else {
                        Log.e(TAG, "Firestore get failed for " + bambinoId, task.getException());
                    }
                });
    }

    public void updateRoundRectColors(String theme){
        ImageView imageView = requireView().findViewById(R.id.imageView7); // Your ImageView containing round_rect

        int startColor = 0;
        int centerColor = 0;
        int endColor = 0;

        // Set colors based on the theme
        switch (theme) {
            case "supereroi":
            case "cartoni_animati":
                startColor = ContextCompat.getColor(requireContext(), R.color.supereroi1);
                centerColor = ContextCompat.getColor(requireContext(), R.color.supereroi2);
                endColor = ContextCompat.getColor(requireContext(), R.color.supereroi3);
                break;
            case "favole":
            case "videogiochi":
                startColor = ContextCompat.getColor(requireContext(), R.color.videogiochi1);
                centerColor = ContextCompat.getColor(requireContext(), R.color.videogiochi2);
                endColor = ContextCompat.getColor(requireContext(), R.color.videogiochi3);
                break;
        }

        // Update the drawable with the new colors
        GradientDrawable gradientDrawable = (GradientDrawable) imageView.getBackground();
        gradientDrawable.setColors(new int[]{startColor, centerColor, endColor});
        imageView.setBackground(gradientDrawable);
    }

    public void updateConstraintLayoutBackground(String theme) {
        ConstraintLayout constraintLayout = requireView().findViewById(R.id.main);// Your ConstraintLayout

        int backgroundColor = 0;

        // Set background color based on the theme
        switch (theme) {
            case "supereroi":
            case "cartoni_animati":
                backgroundColor = ContextCompat.getColor(requireContext(), R.color.supereroibackground); // Replace with actual color resource
                break;
            case "favole":
            case "videogiochi":
                backgroundColor = ContextCompat.getColor(requireContext(), R.color.videogiochibackground); // Replace with actual color resource
                break;
        }

        // Apply the background color to the ConstraintLayout
        constraintLayout.setBackgroundColor(backgroundColor);
    }

    private void calculateStreak() {
        currentStreak=0;
        Date currentDate = new Date();
        calculateDayStreak(currentDate);
        currentStreak=0;
    }

    private void calculateDayStreak(Date date) {
        String formattedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date);

        // Check if exercises exist for all types for the given date
        checkAllExerciseTypesForDate(formattedDate, new HomeBambinoFragment.FirestoreCallback() {
            @Override
            public void onCallback(boolean allExercisesCompleted) {
                if (allExercisesCompleted) {
                    // If all exercises for this day are completed, increase the streak and check the previous day
                    currentStreak++;

                    // Move to the previous day
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendar.add(Calendar.DAY_OF_MONTH, -1);

                    // Recursively check the previous day
                    calculateDayStreak(calendar.getTime());
                } else {
                    // Not all exercises are completed for this day, streak ends
                    Toast.makeText(requireContext(), "Serie attuale: " + currentStreak + " giorni", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Current Streak: " + currentStreak + " days");
                    numerostreak.setText(String.valueOf(currentStreak));

                    // Calcola il progresso della barra
                    int progress = (int) ((currentStreak / 3.0) * 100); // Calcolo del progresso in base alla streak
                    progressBar.setProgress(progress);
                }
            }
        });
    }


    private void checkAllExerciseTypesForDate(String date, HomeBambinoFragment.FirestoreCallback callback) {
        // Define the types of exercises to check
        String[] exerciseTypes = {"tipo1", "tipo2", "tipo3"};
        int totalTypes = exerciseTypes.length;
        final int[] countCorrect = {0}; // Count of exercise types that are correct
        final int[] countChecked = {0}; // Count of total exercise types checked

        for (String type : exerciseTypes) {
            db.collection("esercizi")
                    .document(bambinoId)
                    .collection(type)
                    .document(date)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Boolean isCorrect = document.getBoolean("esercizio_corretto");
                                if (Boolean.TRUE.equals(isCorrect)) {
                                    countCorrect[0]++;
                                }
                                countChecked[0]++;
                            } else {
                                countChecked[0]++;
                            }

                            // Check if all exercise types have been processed
                            if (countChecked[0] == totalTypes) {
                                // All types have been checked, determine if the streak should continue
                                if (countCorrect[0] == totalTypes) {
                                    callback.onCallback(true); // All exercises are correct
                                } else {
                                    callback.onCallback(false); // Not all exercises are correct
                                }
                            }
                        } else {
                            Log.e(TAG, "Error fetching document for type: " + type + " on date: " + date, task.getException());
                            // If there's an error fetching the document, we assume it affects the streak
                            countChecked[0]++;
                            if (countChecked[0] == totalTypes) {
                                callback.onCallback(false); // Assume failure if any document fetch fails
                            }
                        }
                    });
        }
    }

}
