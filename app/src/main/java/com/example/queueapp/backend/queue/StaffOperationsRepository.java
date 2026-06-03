package com.example.queueapp.backend.queue;

import com.example.queueapp.backend.BackendCallback;
import com.example.queueapp.backend.FirestorePaths;
import com.example.queueapp.staff.model.StaffDashboardStats;
import com.example.queueapp.staff.model.StaffQueueEntry;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class StaffOperationsRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void loadDashboardStats(BackendCallback<StaffDashboardStats> callback) {
        DocumentReference stateRef = db.collection(FirestorePaths.COLLECTION_QUEUE_GLOBAL)
                .document(FirestorePaths.DOC_QUEUE_STATE);

        stateRef.get().addOnSuccessListener(stateSnapshot -> {
            final String nowServing = stateSnapshot.exists()
                    && stateSnapshot.getString(FirestorePaths.FIELD_NOW_SERVING) != null
                    ? stateSnapshot.getString(FirestorePaths.FIELD_NOW_SERVING) : "-";

            db.collection(FirestorePaths.COLLECTION_QUEUES)
                    .get()
                    .addOnSuccessListener(queuesSnapshot -> {
                        int active = 0;
                        int completed = 0;
                        int customersToday = 0;
                        int totalWait = 0;
                        int waitCount = 0;
                        Calendar today = Calendar.getInstance();

                        for (QueryDocumentSnapshot doc : queuesSnapshot) {
                            String status = doc.getString(FirestorePaths.FIELD_STATUS);
                            if (FirestorePaths.STATUS_WAITING.equals(status)
                                    || FirestorePaths.STATUS_CALLED.equals(status)
                                    || FirestorePaths.STATUS_SERVING.equals(status)) {
                                active++;
                            }
                            if (FirestorePaths.STATUS_COMPLETED.equals(status)) {
                                completed++;
                            }
                            Timestamp createdAt = doc.getTimestamp(FirestorePaths.FIELD_CREATED_AT);
                            if (createdAt != null && isSameDay(createdAt, today)) {
                                customersToday++;
                            }
                            Long wait = doc.getLong(FirestorePaths.FIELD_ESTIMATED_MINUTES);
                            if (wait != null && FirestorePaths.STATUS_WAITING.equals(status)) {
                                totalWait += wait.intValue();
                                waitCount++;
                            }
                        }
                        int avgWait = waitCount > 0 ? totalWait / waitCount : 0;
                        callback.onSuccess(new StaffDashboardStats(
                                active, customersToday, avgWait, completed, nowServing));
                    })
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void loadLiveQueues(BackendCallback<List<StaffQueueEntry>> callback) {
        db.collection(FirestorePaths.COLLECTION_QUEUES)
                .orderBy(FirestorePaths.FIELD_CREATED_AT, Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<StaffQueueEntry> entries = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String status = doc.getString(FirestorePaths.FIELD_STATUS);
                        if (FirestorePaths.STATUS_COMPLETED.equals(status)
                                || FirestorePaths.STATUS_CANCELLED.equals(status)
                                || FirestorePaths.STATUS_SKIPPED.equals(status)) {
                            continue;
                        }
                        entries.add(mapEntry(doc));
                    }
                    callback.onSuccess(entries);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void callNext(BackendCallback<String> callback) {
        db.collection(FirestorePaths.COLLECTION_QUEUES)
                .whereEqualTo(FirestorePaths.FIELD_STATUS, FirestorePaths.STATUS_WAITING)
                .orderBy(FirestorePaths.FIELD_CREATED_AT, Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        callback.onError("No waiting customers in queue.");
                        return;
                    }
                    DocumentSnapshot next = snapshot.getDocuments().get(0);
                    String queueNumber = next.getString(FirestorePaths.FIELD_QUEUE_NUMBER);
                    String queueId = next.getId();

                    DocumentReference stateRef = db.collection(FirestorePaths.COLLECTION_QUEUE_GLOBAL)
                            .document(FirestorePaths.DOC_QUEUE_STATE);

                    db.runTransaction((Transaction.Function<Void>) transaction -> {
                        transaction.update(stateRef, FirestorePaths.FIELD_NOW_SERVING, queueNumber);
                        transaction.update(db.collection(FirestorePaths.COLLECTION_QUEUES).document(queueId),
                                FirestorePaths.FIELD_STATUS, FirestorePaths.STATUS_CALLED);
                        return null;
                    }).addOnSuccessListener(unused -> callback.onSuccess(queueNumber))
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void skipQueue(String queueId, BackendCallback<Void> callback) {
        updateQueueStatus(queueId, FirestorePaths.STATUS_SKIPPED, callback);
    }

    public void completeQueue(String queueId, String userId, String queueNumber,
                              BackendCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(FirestorePaths.FIELD_STATUS, FirestorePaths.STATUS_COMPLETED);
        db.collection(FirestorePaths.COLLECTION_QUEUES).document(queueId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    if (userId != null && !userId.isEmpty()) {
                        db.collection(FirestorePaths.COLLECTION_USERS).document(userId)
                                .update(FirestorePaths.FIELD_ACTIVE_QUEUE_ID,
                                        com.google.firebase.firestore.FieldValue.delete());
                    }
                    appendHistory(userId, queueNumber);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void cancelQueue(String queueId, String userId, BackendCallback<Void> callback) {
        db.collection(FirestorePaths.COLLECTION_QUEUES).document(queueId)
                .update(FirestorePaths.FIELD_STATUS, FirestorePaths.STATUS_CANCELLED)
                .addOnSuccessListener(unused -> {
                    if (userId != null && !userId.isEmpty()) {
                        db.collection(FirestorePaths.COLLECTION_USERS).document(userId)
                                .update(FirestorePaths.FIELD_ACTIVE_QUEUE_ID,
                                        com.google.firebase.firestore.FieldValue.delete());
                    }
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void resetDailyQueue(BackendCallback<Void> callback) {
        DocumentReference stateRef = db.collection(FirestorePaths.COLLECTION_QUEUE_GLOBAL)
                .document(FirestorePaths.DOC_QUEUE_STATE);
        Map<String, Object> reset = new HashMap<>();
        reset.put(FirestorePaths.FIELD_NOW_SERVING, "-");
        reset.put(FirestorePaths.FIELD_NEXT_COUNTER, 1L);
        stateRef.set(reset)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private void updateQueueStatus(String queueId, String status, BackendCallback<Void> callback) {
        db.collection(FirestorePaths.COLLECTION_QUEUES).document(queueId)
                .update(FirestorePaths.FIELD_STATUS, status)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private void appendHistory(String userId, String queueNumber) {
        if (userId == null || userId.isEmpty()) {
            return;
        }
        Map<String, Object> entry = new HashMap<>();
        entry.put(FirestorePaths.FIELD_DATE_LABEL,
                new java.text.SimpleDateFormat("MMM d, yyyy", Locale.US).format(new java.util.Date()));
        entry.put(FirestorePaths.FIELD_QUEUE_NUMBER, queueNumber);
        entry.put(FirestorePaths.FIELD_FOOD_NAME, "-");
        entry.put(FirestorePaths.FIELD_STATUS, FirestorePaths.STATUS_COMPLETED);
        entry.put(FirestorePaths.FIELD_CREATED_AT, com.google.firebase.firestore.FieldValue.serverTimestamp());
        db.collection(FirestorePaths.COLLECTION_USERS).document(userId)
                .collection(FirestorePaths.SUBCOLLECTION_HISTORY)
                .add(entry);
    }

    private StaffQueueEntry mapEntry(QueryDocumentSnapshot doc) {
        String queueNumber = doc.getString(FirestorePaths.FIELD_QUEUE_NUMBER);
        String customerName = doc.getString(FirestorePaths.FIELD_CUSTOMER_NAME);
        if (customerName == null || customerName.isEmpty()) {
            customerName = "Guest";
        }
        Long wait = doc.getLong(FirestorePaths.FIELD_ESTIMATED_MINUTES);
        String status = doc.getString(FirestorePaths.FIELD_STATUS);
        Boolean vip = doc.getBoolean(FirestorePaths.FIELD_IS_VIP);
        Boolean priority = doc.getBoolean(FirestorePaths.FIELD_IS_PRIORITY);
        String userId = doc.getString(FirestorePaths.FIELD_USER_ID);
        return new StaffQueueEntry(
                doc.getId(),
                queueNumber != null ? queueNumber : "-",
                customerName,
                wait != null ? wait.intValue() : 0,
                status != null ? status : FirestorePaths.STATUS_WAITING,
                vip != null && vip,
                priority != null && priority,
                userId != null ? userId : ""
        );
    }

    private boolean isSameDay(Timestamp timestamp, Calendar today) {
        Calendar created = Calendar.getInstance();
        created.setTime(timestamp.toDate());
        return created.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && created.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
    }
}
