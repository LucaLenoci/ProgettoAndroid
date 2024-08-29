package com.example.progetto_mobile;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ChildHelper {

    private static final String TAG = "ChildHelper";

    /**
     * Recupera la lista delle reference dei bambini associati al genitore specificato dal path
     *
     * @param genitorePath il path di Firestore del genitore
     * @return un Task che, quando completato, conterrà la lista di DocumentReference dei bambini
     */
    public static Task<List<DocumentReference>> getBambiniRefsFromGenitorePath(String genitorePath) {
        return FirebaseFirestore.getInstance()
                .document(genitorePath)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        List<DocumentReference> bambiniRefs = (List<DocumentReference>) task.getResult().get("bambiniRef");
                        if (bambiniRefs != null) {
                            Log.d(TAG, "Numero di bambini trovati: " + bambiniRefs.size());
                            return bambiniRefs;
                        }
                    }
                    return new ArrayList<>();
                });
    }
}