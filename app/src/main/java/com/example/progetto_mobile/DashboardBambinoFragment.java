package com.example.progetto_mobile;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardBambinoFragment extends Fragment {

    private static final String TAG = "DashboardBambinoFragment";
    private TabLayout tabLayout;
    private LinearLayout contentLayout;
    private Child child;
    private boolean isFromHomeLogopedista = false;
    private FirebaseFirestore db;

    public static DashboardBambinoFragment newInstance(Child child, boolean isFromHomeLogopedista) {
        DashboardBambinoFragment fragment = new DashboardBambinoFragment();
        Bundle args = new Bundle();
        args.putSerializable("child", child);
        args.putBoolean("from", isFromHomeLogopedista);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dashboard_bambino, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tabLayout = view.findViewById(R.id.tabLayout);
        contentLayout = view.findViewById(R.id.contentLayout);
        db = FirebaseFirestore.getInstance();

        child = (Child) getArguments().getSerializable("child");
        Boolean from = getArguments().getBoolean("from");
        Log.d(TAG, "From: " + from);
        if (from != null && from.equals(true))
            isFromHomeLogopedista = true;

        if (child != null) {
            setupTabs();
        }

        return view;
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Denominazione immagine"));
        tabLayout.addTab(tabLayout.newTab().setText("Riconoscimento coppie minime"));
        tabLayout.addTab(tabLayout.newTab().setText("Ripetizione sequenza di parole"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "Tab selected: " + tab.getPosition()); // Debugging log
                try {
                    if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        closeExerciseEditFragment();
                    }
                    updateContent(tab.getPosition());
                } catch (Exception e) {
                    Log.e(TAG, "Error switching tabs", e); // Catch potential exceptions
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        if (tabLayout.getTabCount() > 0) {
            updateContent(0); // Initial content load
        }
    }

    private void updateContent(int position) {
        Log.d(TAG, "Updating content for tab: " + position); // Debugging log
        try {
            contentLayout.removeAllViews(); // Reset the view
            String exerciseType = "tipo" + (position + 1);
            Log.d(TAG, "Exercise type: " + exerciseType); // Debugging log
            displayExercises(exerciseType);
        } catch (Exception e) {
            Log.e(TAG, "Error updating content", e); // Catch potential exceptions
        }
    }

    private void displayExercises(String exerciseType) {
        Log.d(TAG, "Displaying exercises for: " + exerciseType); // Debugging log
        try {
            contentLayout.removeAllViews();
            Log.d(TAG, "Content layout cleared."); // Log after removing views from contentLayout

            // Check if it's a placeholder
            if (isPlaceholder(exerciseType)) {
                Log.d(TAG, "Exercise is a placeholder."); // Log if it's a placeholder
                if (isFromHomeLogopedista) {
                    Log.d(TAG, "Showing add exercise button for Logopedista."); // Log for showing the add button
                    showAddExerciseButton(exerciseType);
                } else {
                    Log.d(TAG, "Showing no exercises message for non-Logopedista."); // Log for showing the "no exercises" message
                    showNoExercisesMessage();
                }
                return;
            }

            // Get exercise reference
            String exerciseRef = getExerciseRef(exerciseType);
            Log.d(TAG, "Exercise reference: " + exerciseRef); // Debugging log for the reference
            if (exerciseRef == null || exerciseRef.isEmpty()) {
                Log.d(TAG, "No exercise reference found."); // Log when no reference is found
                if (isFromHomeLogopedista) {
                    Log.d(TAG, "Showing add exercise button for Logopedista."); // Log for showing the add button
                    showAddExerciseButton(exerciseType);
                } else {
                    Log.d(TAG, "Showing no exercises message for non-Logopedista."); // Log for showing the "no exercises" message
                    showNoExercisesMessage();
                }
            } else {
                Log.d(TAG, "Exercise reference found: " + exerciseRef); // Log when the reference is found

                // Get the exercise from the child object
                Object exercise = getExerciseFromChild(exerciseType);
                if (exercise != null) {
                    Log.d(TAG, "Exercise already exists in child object."); // Log when exercise is already present in the child
                    displayExercise(exercise);
                    if (isFromHomeLogopedista) {
                        Log.d(TAG, "Showing edit exercise button."); // Log for showing the edit button
                        showEditExerciseButton(exerciseType);
                    }
                } else {
                    Log.d(TAG, "Fetching exercise from Firestore."); // Log before fetching the exercise from Firestore
                    fetchAndDisplayExercise(exerciseType, exerciseRef);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying exercises", e); // Catch potential exceptions
        }
    }



    private Object getExerciseFromChild(String exerciseType) {
        switch (exerciseType) {
            case "tipo1":
                return child.getEserciziTipo1();
            case "tipo2":
                return child.getEserciziTipo2();
            case "tipo3":
                return child.getEserciziTipo3();
            default:
                return null;
        }
    }

    private boolean isPlaceholder(String exerciseType) {
        String exerciseRef = getExerciseRef(exerciseType);
        if (exerciseRef == null || exerciseRef.isEmpty()) {
            return true;
        }
        return exerciseRef.startsWith("esercizi/placeholder/");
    }

    private String getExerciseRef(String exerciseType) {
        switch (exerciseType) {
            case "tipo1":
                return child.getEsercizioTipo1Ref();
            case "tipo2":
                return child.getEsercizioTipo2Ref();
            case "tipo3":
                return child.getEsercizioTipo3Ref();
            default:
                return null;
        }
    }

    private void fetchAndDisplayExercise(String exerciseType, String exerciseRef) {
        db.document(exerciseRef).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Object exercise = documentSnapshot.toObject(getExerciseClass(exerciseType));
                        displayExercise(exercise);
                    } else {
                        if (isFromHomeLogopedista) {
                            showAddExerciseButton(exerciseType);
                        } else {
                            showNoExercisesMessage();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching exercise", e);
                    Toast.makeText(getContext(), "Errore durante il recupero dell'esercizio", Toast.LENGTH_SHORT).show();
                });
    }

    private Class<?> getExerciseClass(String exerciseType) {
        switch (exerciseType) {
            case "tipo1":
                return EsercizioTipo1.class;
            case "tipo2":
                return EsercizioTipo2.class;
            case "tipo3":
                return EsercizioTipo3.class;
            default:
                throw new IllegalArgumentException("Unknown exercise type: " + exerciseType);
        }
    }

    private void showAddExerciseButton(String exerciseType) {
        Button addButton = new Button(getContext());
        addButton.setText("Aggiungi esercizio");
        addButton.setOnClickListener(v -> openExerciseEditFragment(exerciseType, false, null));
        contentLayout.addView(addButton);
    }

    private void showEditExerciseButton(String exerciseType) {
        Button editButton = new Button(getContext());
        editButton.setText("Modifica esercizio");
        editButton.setOnClickListener(v -> {
            Object exercise = getExerciseFromChild(exerciseType);
            openExerciseEditFragment(exerciseType, true, exercise);
        });
        contentLayout.addView(editButton);
    }

    private void openExerciseEditFragment(String exerciseType, boolean isEditing, Object exercise) {
        ExerciseEditFragment fragment = ExerciseEditFragment.newInstance(exerciseType, isEditing, exercise);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
        contentLayout.removeAllViews();
    }

    public void closeExerciseEditFragment() {
       getActivity().getSupportFragmentManager().popBackStack();
        displayExercises(getCurrentExerciseType());
    }

    public void addExercise(String exerciseType, Object exercise) {
        addExerciseToFirestoreAndAssignItToChild(exerciseType, exercise);
        closeExerciseEditFragment();
    }

    public void updateExercise(String exerciseType, Object exercise) {
        updateExerciseInFirestore(exerciseType, exercise);
        closeExerciseEditFragment();
    }

    private String getCurrentExerciseType() {
        int position = tabLayout.getSelectedTabPosition();
        return "tipo" + (position + 1);
    }

    private void showNoExercisesMessage() {
        TextView messageView = new TextView(getContext());
        messageView.setText("Nessun esercizio disponibile");
        contentLayout.addView(messageView);
    }

    private void addExerciseToFirestoreAndAssignItToChild(String exerciseType, Object exercise) {
        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        DocumentReference exerciseDocRef = db.collection("esercizi")
                .document(child.getDocId())
                .collection(exerciseType)
                .document(date);
        DocumentReference childDocRef = db.collection("bambini").document(child.getDocId());

        db.runTransaction((transaction) -> {
            transaction.set(exerciseDocRef, exercise);
            transaction.update(childDocRef, "esercizio" + exerciseType.substring(0, 1).toUpperCase() + exerciseType.substring(1), exerciseDocRef);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Esercizio aggiunto con successo", Toast.LENGTH_SHORT).show();
            updateChildExerciseRef(exerciseType, exerciseDocRef.getPath());
            updateLocalChild(exerciseType, exerciseDocRef, exercise);
            displayExercises(exerciseType);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error adding exercise", e);
            Toast.makeText(getContext(), "Errore nell'aggiunta dell'esercizio", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateLocalChild(String exerciseType, DocumentReference exerciseDocRef, Object exercise) {
        switch (exerciseType) {
            case "tipo1":
                child.setEserciziTipo1((EsercizioTipo1) exercise);
                child.putEsercizioTipo1Ref(exerciseDocRef.getPath());
                break;
            case "tipo2":
                child.setEserciziTipo2((EsercizioTipo2) exercise);
                child.putEsercizioTipo2Ref(exerciseDocRef.getPath());
                break;
            case "tipo3":
                child.setEserciziTipo3((EsercizioTipo3) exercise);
                child.putEsercizioTipo3Ref(exerciseDocRef.getPath());
                break;
        }
    }

    private void updateExerciseInFirestore(String exerciseType, Object exercise) {
        String exerciseRef = getExerciseRef(exerciseType);
        if (exerciseRef != null && !exerciseRef.isEmpty()) {
            DocumentReference exerciseDocRef = db.document(exerciseRef);
            exerciseDocRef.set(exercise)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Esercizio aggiornato con successo", Toast.LENGTH_SHORT).show();
                        updateLocalChild(exerciseType, exerciseDocRef, exercise);
                        displayExercises(exerciseType);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating exercise", e);
                        Toast.makeText(getContext(), "Errore nell'aggiornamento dell'esercizio", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateChildExerciseRef(String exerciseType, String newRef) {
        switch (exerciseType) {
            case "tipo1":
                child.putEsercizioTipo1Ref(newRef);
                break;
            case "tipo2":
                child.putEsercizioTipo2Ref(newRef);
                break;
            case "tipo3":
                child.putEsercizioTipo3Ref(newRef);
                break;
        }
    }

    private void displayExercise(Object exercise) {
        Log.d(TAG, "Displaying exercise: " + exercise.toString()); // Log the exercise object being displayed

        // Inflate the layout for the exercise
        View exerciseView = LayoutInflater.from(getContext()).inflate(R.layout.layout_item_esercizio, contentLayout, false);
        Log.d(TAG, "Exercise layout inflated."); // Log after inflating the layout

        LinearLayout detailsContainer = exerciseView.findViewById(R.id.esercizioDetailsContainer);
        LinearLayout barraAudio = exerciseView.findViewById(R.id.layoutBarraAudio);
        Button playPauseButton = exerciseView.findViewById(R.id.playPauseButton);
        SeekBar audioSeekBar = exerciseView.findViewById(R.id.audioSeekBar);

        // Add exercise details to the container
        View detailsView = getExerciseDetails(exercise);
        detailsContainer.addView(detailsView);
        Log.d(TAG, "Exercise details added to the container."); // Log after adding exercise details

        String audioUrl = null;
        if (exercise instanceof EsercizioTipo1) {
            EsercizioTipo1 es = (EsercizioTipo1) exercise;
            audioUrl = es.getAudio_url();
            Log.d(TAG, "Exercise type is EsercizioTipo1, audio URL: " + audioUrl); // Log audio URL for EsercizioTipo1
        } else if (exercise instanceof EsercizioTipo3) {
            EsercizioTipo3 es = (EsercizioTipo3) exercise;
            audioUrl = es.getAudio_url();
            Log.d(TAG, "Exercise type is EsercizioTipo3, audio URL: " + audioUrl); // Log audio URL for EsercizioTipo3
        }

        if (audioUrl != null && !audioUrl.isEmpty()) {
            barraAudio.setVisibility(View.VISIBLE);
            Log.d(TAG, "Audio URL is valid, initializing MediaPlayer."); // Log when audio is valid and MediaPlayer is initialized

            MediaPlayer mediaPlayer = new MediaPlayer();
            Handler handler = new Handler(); // Handler for updating the SeekBar

            try {
                mediaPlayer.setDataSource(audioUrl);
                Log.d(TAG, "MediaPlayer data source set: " + audioUrl); // Log the audio URL being set in MediaPlayer

                mediaPlayer.prepareAsync();
                Log.d(TAG, "MediaPlayer is preparing asynchronously."); // Log when MediaPlayer starts preparing

                // When MediaPlayer is ready, set up the SeekBar and play/pause button
                mediaPlayer.setOnPreparedListener(mp -> {
                    Log.d(TAG, "MediaPlayer is prepared, setting up play/pause button and SeekBar."); // Log when MediaPlayer is prepared
                    playPauseButton.setText("Play");
                    audioSeekBar.setMax(mp.getDuration()); // Set the max duration of the SeekBar
                    Log.d(TAG, "Audio duration set in SeekBar: " + mp.getDuration()); // Log the audio duration

                    playPauseButton.setOnClickListener(v -> {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            playPauseButton.setText("Play");
                            Log.d(TAG, "Audio paused."); // Log when audio is paused
                        } else {
                            mediaPlayer.start();
                            playPauseButton.setText("Pause");
                            Log.d(TAG, "Audio playing."); // Log when audio starts playing
                            updateSeekBar(mediaPlayer, audioSeekBar, handler); // Update SeekBar while playing
                        }
                    });
                });

                // Release the MediaPlayer when playback is complete
                mediaPlayer.setOnCompletionListener(mp -> {
                    Log.d(TAG, "Audio playback completed, resetting MediaPlayer."); // Log when playback is completed
                    playPauseButton.setText("Play");
                    audioSeekBar.setProgress(0);
                    mediaPlayer.seekTo(0); // Reset to the start
                });

            } catch (Exception e) {
                Log.e(TAG, "Error in audio playback", e); // Log any errors during audio playback
                Toast.makeText(getContext(), "Error in audio playback", Toast.LENGTH_SHORT).show();
            }

            // Listener for SeekBar interaction
            audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        Log.d(TAG, "SeekBar progress changed by user: " + progress); // Log when user changes SeekBar position
                        mediaPlayer.seekTo(progress); // Allow user to skip forward/backward
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    Log.d(TAG, "User started tracking SeekBar."); // Log when user starts tracking the SeekBar
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    Log.d(TAG, "User stopped tracking SeekBar."); // Log when user stops tracking the SeekBar
                }
            });
        } else {
            Log.d(TAG, "No valid audio URL provided, audio section hidden."); // Log when there is no valid audio URL
            barraAudio.setVisibility(View.GONE);
        }

        contentLayout.addView(exerciseView);
        Log.d(TAG, "Exercise view added to content layout."); // Log when the exercise view is added to the content layout
    }

    private void updateSeekBar(MediaPlayer mediaPlayer, SeekBar audioSeekBar, Handler handler) {
        audioSeekBar.setProgress(mediaPlayer.getCurrentPosition());
        if (mediaPlayer.isPlaying()) {
            // Aggiorna la SeekBar ogni 100ms
            handler.postDelayed(() -> updateSeekBar(mediaPlayer, audioSeekBar, handler), 20);
        }
    }

    // todo: rivedi quali campi far vedere
    private View getExerciseDetails(Object exercise) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_exercise_details, null);
        TextView tvExerciseStatus = view.findViewById(R.id.tvExerciseStatus);
        TextView tvAttempts = view.findViewById(R.id.tvAttempts);
        TextView tvCorrectAnswer = view.findViewById(R.id.tvCorrectAnswer);
        TextView tvAdditionalInfo = view.findViewById(R.id.tvAdditionalInfo);

        if (exercise instanceof EsercizioTipo1) {
            EsercizioTipo1 es = (EsercizioTipo1) exercise;

            if (!es.isEsercizio_corretto()) {
                tvExerciseStatus.setText(getString(R.string.esercizio_in_corso));
                tvAttempts.setVisibility(View.GONE);
                tvCorrectAnswer.setText(getString(R.string.parola_da_ripetere, es.getRisposta_corretta()));
                tvAdditionalInfo.setVisibility(View.GONE);
                return view;
            }

            tvExerciseStatus.setVisibility(View.GONE);
            tvAttempts.setText(getResources().getQuantityString(R.plurals.tentativi, es.getTentativi(), es.getTentativi()));
            tvCorrectAnswer.setText(getString(R.string.parola_da_ripetere, es.getRisposta_corretta()));
            tvAdditionalInfo.setText(getResources().getQuantityString(R.plurals.suggerimenti_usati, es.getSuggerimentiUsati(), es.getSuggerimentiUsati()));

        } else if (exercise instanceof EsercizioTipo2) {
            EsercizioTipo2 es = (EsercizioTipo2) exercise;

            if (!es.isEsercizio_corretto()) {
                tvExerciseStatus.setText(getString(R.string.esercizio_in_corso));
                tvAttempts.setVisibility(View.GONE);
                tvCorrectAnswer.setText(getString(R.string.immagine_da_scegliere, es.getRisposta_corretta()));
                tvAdditionalInfo.setText(getString(R.string.immagine_da_evitare, es.getRisposta_sbagliata()));
                return view;
            }

            tvExerciseStatus.setVisibility(View.GONE);
            tvAttempts.setText(getResources().getQuantityString(R.plurals.tentativi, es.getTentativi(), es.getTentativi()));
            tvCorrectAnswer.setText(getString(R.string.immagine_da_scegliere, es.getRisposta_corretta()));
            tvAdditionalInfo.setText(getString(R.string.immagine_da_evitare, es.getRisposta_sbagliata()));

        } else if (exercise instanceof EsercizioTipo3) {
            EsercizioTipo3 es = (EsercizioTipo3) exercise;

            if (!es.isEsercizio_corretto()) {
                tvExerciseStatus.setText(getString(R.string.esercizio_in_corso));
                tvAttempts.setVisibility(View.GONE);
                tvCorrectAnswer.setText(getString(R.string.parole_da_ripetere, es.getRisposta_corretta()));
                tvAdditionalInfo.setVisibility(View.GONE);
                return view;
            }

            tvExerciseStatus.setVisibility(View.GONE);
            tvAttempts.setText(getResources().getQuantityString(R.plurals.tentativi, es.getTentativi(), es.getTentativi()));
            tvCorrectAnswer.setText(getString(R.string.parole_da_ripetere, es.getRisposta_corretta()));
            tvAdditionalInfo.setVisibility(View.GONE);

        } else {
            tvExerciseStatus.setText("Tipo di esercizio sconosciuto");
            tvAttempts.setVisibility(View.GONE);
            tvCorrectAnswer.setVisibility(View.GONE);
            tvAdditionalInfo.setVisibility(View.GONE);
        }

        return view;
    }

}
