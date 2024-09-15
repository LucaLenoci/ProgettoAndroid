package com.example.progetto_mobile;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExerciseEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExerciseEditFragment extends Fragment {

    private static final String TAG = "ExerciseEditFragment";
    private static final String ARG_EXERCISE_TYPE = "exercise_type";
    private static final String ARG_IS_EDITING = "is_editing";
    private static final String ARG_EXERCISE = "exercise";

    private String exerciseType;
    private boolean isEditing, isCorrectImageDialog;
    private Object exercise;
    private TextInputLayout inputLayout1, inputLayout2, inputLayout3;
    private Button buttonCorrectAnswer;
    private Button buttonWrongAnswer;
    private String selectedCorrectImageName;
    private String selectedWrongImageName;
    private StorageReference storageRef;
    private List<StorageReference> imagesList;
    private ActivityResultLauncher<Intent> pickImagefromGalleryLauncher;

    public ExerciseEditFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param exerciseType The exercise type.
     * @param isEditing    Whether the fragment is in editing mode.
     * @param exercise     The exercise object to be edited (if in editing mode).
     * @return A new instance of fragment ExerciseEditFragment.
     */
    public static ExerciseEditFragment newInstance(String exerciseType, boolean isEditing, Object exercise) {
        ExerciseEditFragment fragment = new ExerciseEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EXERCISE_TYPE, exerciseType);
        args.putBoolean(ARG_IS_EDITING, isEditing);
        args.putSerializable(ARG_EXERCISE, (Serializable) exercise);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exerciseType = getArguments().getString(ARG_EXERCISE_TYPE);
            isEditing = getArguments().getBoolean(ARG_IS_EDITING);
            exercise = getArguments().getSerializable(ARG_EXERCISE);
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("immagini");
        imagesList = new ArrayList<>();
        loadImagesFromFirebase();

        pickImagefromGalleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadImageToFirebaseStorage(imageUri)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(requireContext(), "Immagine caricata con successo!", Toast.LENGTH_SHORT).show();
                                        loadImagesFromFirebase()
                                                .addOnSuccessListener(updatedList -> showImageSelectionDialog(isCorrectImageDialog))
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Errore durante l'aggiornamento della lista di immagini", e);
                                                    Toast.makeText(requireContext(), "Errore durante l'aggiornamento della lista di immagini", Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Errore durante il caricamento dell'immagine", e);
                                        Toast.makeText(requireContext(), "Errore durante il caricamento dell'immagine", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                });
    }

    private Task<List<StorageReference>> loadImagesFromFirebase() {
        return storageRef.listAll()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    imagesList.clear();
                    for (StorageReference imageRef : task.getResult().getItems()) {
                        Log.d(TAG, "image ref: " + imageRef.getPath());
                        imagesList.add(imageRef);
                    }
                    Log.d(TAG, "immagini trovate: " + imagesList.size());
                    return imagesList;
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise_edit, container, false);

        buttonCorrectAnswer = view.findViewById(R.id.buttonCorrectAnswer);
        buttonWrongAnswer = view.findViewById(R.id.buttonWrongAnswer);
        inputLayout1 = view.findViewById(R.id.textInputLayoutSuggerimento1);
        inputLayout2 = view.findViewById(R.id.textInputLayoutSuggerimento2);
        inputLayout3 = view.findViewById(R.id.textInputLayoutSuggerimento3);
        Button cancelButton = view.findViewById(R.id.cancelButton);
        Button confirmButton = view.findViewById(R.id.confirmButton);

        setupInputFields();
        if (isEditing &&  exercise != null) {
            fillInputFieldsWithExistingData();
        }

        buttonCorrectAnswer.setOnClickListener(v -> showImageSelectionDialog(true));
        buttonWrongAnswer.setOnClickListener(v -> showImageSelectionDialog(false));

        cancelButton.setOnClickListener(v -> onCancelClick());
        confirmButton.setOnClickListener(v -> onConfirmClick());

        return view;
    }

    private void showImageSelectionDialog(boolean isCorrectAnswer) {
        isCorrectImageDialog = isCorrectAnswer;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(isCorrectAnswer ? "Seleziona l'immagine corretta" : "Seleziona l'immagine sbagliata");

        View view = getLayoutInflater().inflate(R.layout.dialog_image_selection, null);
        LinearLayout linearLayoutImages = view.findViewById(R.id.linearLayoutImages);
        Button btnAggiungiImmagine = view.findViewById(R.id.buttonAddImage);

        AlertDialog dialog = builder.setView(view).create();

        for (StorageReference imageRef : imagesList) {
            View itemView = getLayoutInflater().inflate(R.layout.layout_button_drawable_bottom_item, null);
            Button imageButton = itemView.findViewById(R.id.buttonImageName);

            // Rimuovi l'estensione dal nome del file
            String fileName = imageRef.getName().replaceFirst("[.][^.]+$", "");
            imageButton.setText(fileName);

            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(requireContext())
                        .asDrawable()
                        .load(uri)
                        .override(300, 300)
                        .into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                imageButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, resource);
                                imageButton.setCompoundDrawablePadding(16);

                                imageButton.setOnClickListener(v -> {
                                    if (isCorrectAnswer) {
                                        selectedCorrectImageName = fileName;
                                        buttonCorrectAnswer.setCompoundDrawablesWithIntrinsicBounds(null, null, null, resource);
                                    } else {
                                        selectedWrongImageName = fileName;
                                        buttonWrongAnswer.setCompoundDrawablesWithIntrinsicBounds(null, null, null, resource);
                                    }
                                    dialog.dismiss();
                                });
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {}

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);

                                Drawable defaultDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.error_outline_24);
                                imageButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, defaultDrawable);
                                imageButton.setCompoundDrawablePadding(16);
                            }
                        });
            }).addOnFailureListener(e -> Log.e(TAG, "Error getting image URL", e));

            linearLayoutImages.addView(itemView);
        }

        btnAggiungiImmagine.setOnClickListener(v -> {
            selectImageFromGallery(pickImagefromGalleryLauncher);
            dialog.dismiss();
        });

        dialog.show();
    }

    private Task<Void> uploadImageToFirebaseStorage(Uri imageUri) {
        return FirebaseStorage.getInstance()
                .getReference("immagini")
                .child(imageUri.getLastPathSegment())
                .putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Invece di restituire null, restituiamo un Task completato
                    return Tasks.forResult(null);
                });
    }

    private void selectImageFromGallery(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcher.launch(intent);
    }

    private void setupInputFields() {
        switch (exerciseType) {
            case "tipo1":
                inputLayout1.setHint("Suggerimento 1");
                inputLayout2.setHint("Suggerimento 2");
                inputLayout3.setHint("Suggerimento 3");
                buttonWrongAnswer.setVisibility(View.GONE);
                break;
            case "tipo2":
                inputLayout1.setVisibility(View.GONE);
                inputLayout2.setVisibility(View.GONE);
                inputLayout3.setVisibility(View.GONE);
                break;
            case "tipo3":
                inputLayout1.setHint("Parole separate da spazi");
                inputLayout2.setVisibility(View.GONE);
                inputLayout3.setVisibility(View.GONE);
                buttonCorrectAnswer.setVisibility(View.GONE);
                buttonWrongAnswer.setVisibility(View.GONE);
                break;
        }
    }

    private void fillInputFieldsWithExistingData() {
        switch (exerciseType) {
            case "tipo1":
                EsercizioTipo1 ex1 = (EsercizioTipo1) exercise;
                loadImageAndSetButtonDrawable(findImageReferenceByName(ex1.getRisposta_corretta()), buttonCorrectAnswer, R.drawable.aggiungi_foto_24);
                inputLayout1.getEditText().setText(ex1.getSuggerimento());
                inputLayout2.getEditText().setText(ex1.getSuggerimento2());
                inputLayout3.getEditText().setText(ex1.getSuggerimento3());
                break;
            case "tipo2":
                EsercizioTipo2 ex2 = (EsercizioTipo2) exercise;
                loadImageAndSetButtonDrawable(findImageReferenceByName(ex2.getRisposta_corretta()), buttonCorrectAnswer, R.drawable.aggiungi_foto_24);
                loadImageAndSetButtonDrawable(findImageReferenceByName(ex2.getRisposta_sbagliata()), buttonWrongAnswer, R.drawable.aggiungi_foto_24);
                inputLayout1.getEditText().setText(String.valueOf(ex2.getImmagine_corretta()));
                break;
            case "tipo3":
                EsercizioTipo3 ex3 = (EsercizioTipo3) exercise;
                inputLayout1.getEditText().setText(ex3.getRisposta_corretta());
                break;
        }
    }

    private StorageReference findImageReferenceByName(String imageName) {
        for (StorageReference imageRef : imagesList) {
            // Rimuove l'estensione dal nome dell'immagine per fare il confronto
            String fileName = imageRef.getName().replaceFirst("[.][^.]+$", "");
            if (fileName.equals(imageName)) {
                return imageRef;
            }
        }
        return null;
    }

    private void loadImageAndSetButtonDrawable(StorageReference imageName, Button button, int placeholder) {
        // placeholder nel frattempo che Glide scarichi l'immagine
        button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, placeholder);
        if (imageName == null) return;

        imageName.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(requireContext())
                    .asDrawable()
                    .load(uri)
                    .override(300, 300)  // Puoi regolare la dimensione
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            button.setCompoundDrawablesWithIntrinsicBounds(null, null, null, resource);
                            button.setCompoundDrawablePadding(16);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {}

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);

                            Drawable defaultDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.error_outline_24);
                            button.setCompoundDrawablesWithIntrinsicBounds(null, null, null, defaultDrawable);
                            button.setCompoundDrawablePadding(16);
                        }
                    });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error getting image URL", e);
            Drawable defaultDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.error_outline_24);
            button.setCompoundDrawablesWithIntrinsicBounds(null, null, null, defaultDrawable);
            button.setCompoundDrawablePadding(16);
        });
    }

    private void onCancelClick() {
        if (getActivity() instanceof LogopedistaActivity) {
            ((LogopedistaActivity) getActivity()).closeExerciseEditFragment();
        }
    }

    private void onConfirmClick() {
        Object newExercise = createExerciseObject();
        if (getActivity() instanceof LogopedistaActivity) {
            ((LogopedistaActivity) getActivity()).handleExerciseResult(exerciseType, newExercise, isEditing);
        }
    }

    private Object createExerciseObject() {
        switch (exerciseType) {
            case "tipo1":
                return new EsercizioTipo1("", false, selectedCorrectImageName,
                        Arrays.asList(false, false, false), inputLayout1.getEditText().getText().toString(), inputLayout2.getEditText().getText().toString(),
                        inputLayout3.getEditText().getText().toString(), 0, 0, "", "tipo1");
            case "tipo2":
                return new EsercizioTipo2("", false, selectedCorrectImageName,
                        selectedWrongImageName, new Random().nextInt(2) + 1, 0, "tipo2");
            case "tipo3":
                return new EsercizioTipo3("", false,
                        inputLayout1.getEditText().getText().toString(), "", 0, "tipo3");
            default:
                throw new IllegalArgumentException("Unknown exercise type: " + exerciseType);
        }
    }

}