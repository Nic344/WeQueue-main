package com.example.queueapp.backend.history;

import com.example.queueapp.backend.BackendCallback;
import com.example.queueapp.backend.FirestorePaths;
import com.example.queueapp.model.QueueHistory;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public final class HistoryBackendRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void loadHistory(String uid, BackendCallback<List<QueueHistory>> callback) {
        db.collection(FirestorePaths.COLLECTION_USERS).document(uid)
                .collection(FirestorePaths.SUBCOLLECTION_HISTORY)
                .orderBy(FirestorePaths.FIELD_CREATED_AT,
                        com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<QueueHistory> history = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String date = doc.getString(FirestorePaths.FIELD_DATE_LABEL);
                        String queueNumber = doc.getString(FirestorePaths.FIELD_QUEUE_NUMBER);
                        String foodName = doc.getString(FirestorePaths.FIELD_FOOD_NAME);
                        String status = doc.getString(FirestorePaths.FIELD_STATUS);
                        if (date == null) {
                            date = "";
                        }
                        if (queueNumber == null) {
                            queueNumber = "-";
                        }
                        if (foodName == null) {
                            foodName = "-";
                        }
                        if (status == null) {
                            status = QueueHistory.STATUS_COMPLETED;
                        }
                        history.add(new QueueHistory(date, queueNumber, foodName, status));
                    }
                    callback.onSuccess(history);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
