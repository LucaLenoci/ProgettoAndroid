package com.example.progetto_mobile;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChildHelper {

    private static final String TAG = "ChildHelper";

    private static Task<List<DocumentReference>> getBambiniRefsFromParentPath(String path) {
        return FirebaseFirestore.getInstance()
                .document(path)
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

    /**
     * Recupera la lista delle reference dei bambini associati al genitore specificato dal path
     *
     * @param genitorePath il path di Firestore del genitore
     * @return un Task che, quando completato, conterrà la lista di DocumentReference dei bambini
     */
    public static Task<List<DocumentReference>> getBambiniRefsFromGenitorePath(String genitorePath) {
        return getBambiniRefsFromParentPath(genitorePath);
    }

    /**
     * Recupera la lista delle reference dei bambini associati al logopedista specificato dal path
     *
     * @param logopedistaPath il path di Firestore del genitore
     * @return un Task che, quando completato, conterrà la lista di DocumentReference dei bambini
     */
    public static Task<List<DocumentReference>> getBambiniRefsFromLogopedistaPath(String logopedistaPath) {
        return getBambiniRefsFromParentPath(logopedistaPath);
    }

    /**
     * Aggiunge un bambino al database Firestore
     *
     * @param child         la mappa contenente i dati del bambino
     * @param genitorePath  il path di Firestore del genitore
     * @param logopedistaPath il path di Firestore del logopedista
     * @return un Task che, quando completato, conterrà il DocumentReference del bambino appena aggiunto
     */
    public static Task<Task<DocumentReference>> addBambinoToFirestore(Map<String, Object> child, String genitorePath, String logopedistaPath) {
        return FirebaseFirestore.getInstance()
                .collection("bambini")
                .add(child)
                .continueWith(addTask -> {
                    if (addTask.isSuccessful()) {
                        DocumentReference bambinoRef = addTask.getResult();
                        Task<DocumentReference> genitoreTask = addBambinoRefToGenitore(bambinoRef.getPath(), genitorePath);
                        Task<DocumentReference> logopedistaTask = addBambinoRefToLogopedista(bambinoRef.getPath(), logopedistaPath);
                        return Tasks.whenAllComplete(genitoreTask, logopedistaTask)
                                .continueWith(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Bambino aggiunto con successo: " + bambinoRef.getPath());
                                        return bambinoRef;
                                    } else {
                                        Log.e(TAG, "Errore nell'aggiungere il bambino", task.getException());
                                        return null;
                                    }
                                });
                    } else {
                        Log.e(TAG, "Errore nell'aggiungere il bambino", addTask.getException());
                        return null;
                    }
                });
    }

    /**
     * Aggiunge il riferimento del bambino al genitore specificato
     *
     * @param bambinoPath il path di Firestore del bambino
     * @param genitorePath il path di Firestore del genitore
     * @return un Task che, quando completato, conterrà il DocumentReference del bambino
     */
    public static Task<DocumentReference> addBambinoRefToGenitore(String bambinoPath, String genitorePath) {
        return FirebaseFirestore.getInstance()
                .document(genitorePath)
                .update("bambiniRef", FieldValue.arrayUnion(FirebaseFirestore.getInstance().document(bambinoPath)))
                .continueWith(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Log.d(TAG, "Riferimento bambino aggiunto al genitore: " + genitorePath);
                        return FirebaseFirestore.getInstance().document(bambinoPath);
                    } else {
                        Log.e(TAG, "Errore nell'aggiornare il genitore con il nuovo bambino", updateTask.getException());
                        return null;
                    }
                });
    }

    /**
     * Aggiunge il riferimento del bambino al logopedista specificato
     *
     * @param bambinoPath il path di Firestore del bambino
     * @param logopedistaPath il path di Firestore del logopedista
     * @return un Task che, quando completato, conterrà il DocumentReference del bambino
     */
    public static Task<DocumentReference> addBambinoRefToLogopedista(String bambinoPath, String logopedistaPath) {
        return FirebaseFirestore.getInstance()
                .document(logopedistaPath)
                .update("bambiniRef", FieldValue.arrayUnion(FirebaseFirestore.getInstance().document(bambinoPath)))
                .continueWith(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        Log.d(TAG, "Riferimento bambino aggiunto al logopedista: " + logopedistaPath);
                        return FirebaseFirestore.getInstance().document(bambinoPath);
                    } else {
                        Log.e(TAG, "Errore nell'aggiornare il logopedista con il nuovo bambino", updateTask.getException());
                        return null;
                    }
                });
    }
}