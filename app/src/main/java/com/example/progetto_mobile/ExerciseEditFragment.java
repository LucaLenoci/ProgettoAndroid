package com.example.progetto_mobile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;

import java.io.Serializable;

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
    private boolean isEditing;
    private Object exercise;
    private TextInputLayout inputLayout1, inputLayout2, inputLayout3, inputLayout4, inputLayout5;
    private Button confirmButton, cancelButton;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exercise_edit, container, false);

        inputLayout1 = view.findViewById(R.id.textInputLayout1);
        inputLayout2 = view.findViewById(R.id.textInputLayout2);
        inputLayout3 = view.findViewById(R.id.textInputLayout3);
        inputLayout4 = view.findViewById(R.id.textInputLayout4);
        inputLayout5 = view.findViewById(R.id.textInputLayout5);
        cancelButton = view.findViewById(R.id.cancelButton);
        confirmButton = view.findViewById(R.id.confirmButton);

        setupInputFields();
        if (isEditing &&  exercise != null) {
            fillInputFieldsWithExistingData();
        }

        cancelButton.setOnClickListener(v -> onCancelClick());
        confirmButton.setOnClickListener(v -> onConfirmClick());

        return view;
    }

    private void fillInputFieldsWithExistingData() {
        switch (exerciseType) {
            case "tipo1":
                EsercizioTipo1 ex1 = (EsercizioTipo1) exercise;
                inputLayout1.getEditText().setText(ex1.getRisposta_corretta());
                inputLayout2.getEditText().setText(ex1.getSuggerimento());
                inputLayout3.getEditText().setText(ex1.getSuggerimento2());
                inputLayout4.getEditText().setText(ex1.getSuggerimento3());
                break;
            case "tipo2":
                EsercizioTipo2 ex2 = (EsercizioTipo2) exercise;
                inputLayout1.getEditText().setText(ex2.getRisposta_corretta());
                inputLayout2.getEditText().setText(ex2.getRisposta_sbagliata());
                inputLayout3.getEditText().setText(String.valueOf(ex2.getImmagine_corretta()));
                break;
            case "tipo3":
                EsercizioTipo3 ex3 = (EsercizioTipo3) exercise;
                inputLayout1.getEditText().setText(ex3.getRisposta_corretta());
                break;
        }
    }

    private void setupInputFields() {
        switch (exerciseType) {
            case "tipo1":
                inputLayout1.setHint("Risposta corretta");
                inputLayout2.setHint("Suggerimento 1");
                inputLayout3.setHint("Suggerimento 2");
                inputLayout4.setHint("Suggerimento 3");
                inputLayout5.setVisibility(View.GONE);
                break;
            case "tipo2":
                inputLayout1.setHint("Risposta corretta");
                inputLayout2.setHint("Risposta sbagliata");
                inputLayout3.setHint("Immagine corretta (numero)");
                inputLayout4.setVisibility(View.GONE);
                inputLayout5.setVisibility(View.GONE);
                break;
            case "tipo3":
                inputLayout1.setHint("Risposta corretta");
                inputLayout2.setVisibility(View.GONE);
                inputLayout3.setVisibility(View.GONE);
                inputLayout4.setVisibility(View.GONE);
                inputLayout5.setVisibility(View.GONE);
                break;
        }
    }

    private void onCancelClick() {
        ((DashboardBambinoActivity) requireActivity()).closeExerciseEditFragment();
    }

    private void onConfirmClick() {
        Object newExercise = createExerciseObject();
        if (isEditing) {
            ((DashboardBambinoActivity) requireActivity()).updateExercise(exerciseType, newExercise);
        } else {
            ((DashboardBambinoActivity) requireActivity()).addExercise(exerciseType, newExercise);
        }
    }

    private Object createExerciseObject() {
        switch (exerciseType) {
            case "tipo1":
                return new EsercizioTipo1(false, "", inputLayout1.getEditText().getText().toString(),
                        inputLayout2.getEditText().getText().toString(), inputLayout3.getEditText().getText().toString(),
                        inputLayout4.getEditText().getText().toString());
            case "tipo2":
                return new EsercizioTipo2(false, "", inputLayout1.getEditText().getText().toString(),
                        inputLayout2.getEditText().getText().toString(), "", Integer.parseInt(inputLayout3.getEditText().getText().toString()));
            case "tipo3":
                return new EsercizioTipo3(false, inputLayout1.getEditText().getText().toString());
            default:
                throw new IllegalArgumentException("Unknown exercise type: " + exerciseType);
        }
    }

}