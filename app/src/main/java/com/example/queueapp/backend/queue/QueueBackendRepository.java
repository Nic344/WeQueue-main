package com.example.queueapp.backend.queue;

import com.example.queueapp.backend.BackendCallback;
import com.example.queueapp.backend.FirestorePaths;
import com.example.queueapp.model.QueueHistory;
import com.example.queueapp.model.QueueTicket;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class QueueBackendRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void ensureGlobalQueueState(Runnable onReady) {
        DocumentReference stateRef = db.collection(FirestorePaths.COLLECTION_QUEUE_GLOBAL)
                .document(FirestorePaths.DOC_QUEUE_STATE);
        stateRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Map<String, Object> initial = new HashMap<>();
                initial.put(FirestorePaths.FIELD_NOW_SERVING, "A015");
                initial.put(FirestorePaths.FIELD_NEXT_COUNTER, 21L);
                stateRef.set(initial).addOnCompleteListener(task -> onReady.run());
            } else {
                onReady.run();
            }
        }).addOnFailureListener(e -> onReady.run());
    }

    public void syncQueueState(String uid, String activeQueueId, BackendCallback<QueueStateSnapshot> callback) {
        DocumentReference stateRef = db.collection(FirestorePaths.COLLECTION_QUEUE_GLOBAL)
                .document(FirestorePaths.DOC_QUEUE_STATE);

        stateRef.get().addOnSuccessListener(stateSnapshot -> {
            final String nowServing = (stateSnapshot.exists()
                    && stateSnapshot.getString(FirestorePaths.FIELD_NOW_SERVING) != null)
                    ? stateSnapshot.getString(FirestorePaths.FIELD_NOW_SERVING)
                    : "A015";

            if (activeQueueId == null || activeQueueId.isEmpty()) {
                callback.onSuccess(new QueueStateSnapshot(nowServing, "-", 0, 0, null, false));
                return;
            }

            db.collection(FirestorePaths.COLLECTION_QUEUES).document(activeQueueId)
                    .get()
                    .addOnSuccessListener(queueSnapshot -> {
                        if (!queueSnapshot.exists()) {
                            callback.onSuccess(new QueueStateSnapshot(nowServing, "-", 0, 0, null, false));
                            return;
                        }
                        QueueTicket ticket = ticketFromSnapshot(queueSnapshot, nowServing);
                        String status = queueSnapshot.getString(FirestorePaths.FIELD_STATUS);
                        boolean active = FirestorePaths.STATUS_WAITING.equals(status)
                                || FirestorePaths.STATUS_SERVING.equals(status);
                        if (!active) {
                            callback.onSuccess(new QueueStateSnapshot(nowServing, "-", 0, 0, null, false));
                            return;
                        }
                        callback.onSuccess(new QueueStateSnapshot(
                                nowServing,
                                ticket.getQueueNumber(),
                                ticket.getPosition(),
                                ticket.getEstimatedMinutes(),
                                ticket,
                                true,
                                activeQueueId
                        ));
                    })
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void takeQueue(String uid, String customerName, BackendCallback<QueueStateSnapshot> callback) {
        DocumentReference stateRef = db.collection(FirestorePaths.COLLECTION_QUEUE_GLOBAL)
                .document(FirestorePaths.DOC_QUEUE_STATE);

        db.runTransaction((Transaction.Function<QueueStateSnapshot>) transaction -> {
            DocumentSnapshot stateSnapshot = transaction.get(stateRef);
            long nextCounter = 21L;
            String nowServing = "A015";
            if (stateSnapshot.exists()) {
                Long counterValue = stateSnapshot.getLong(FirestorePaths.FIELD_NEXT_COUNTER);
                if (counterValue != null) {
                    nextCounter = counterValue;
                }
                String servingValue = stateSnapshot.getString(FirestorePaths.FIELD_NOW_SERVING);
                if (servingValue != null) {
                    nowServing = servingValue;
                }
            }

            String queueNumber = String.format(Locale.US, "A%03d", nextCounter);
            int position = calculatePosition(queueNumber, nowServing);
            int estimatedMinutes = Math.max(3, position * 3);

            transaction.set(stateRef, Map.of(
                    FirestorePaths.FIELD_NOW_SERVING, nowServing,
                    FirestorePaths.FIELD_NEXT_COUNTER, nextCounter + 1
            ), com.google.firebase.firestore.SetOptions.merge());

            DocumentReference queueRef = db.collection(FirestorePaths.COLLECTION_QUEUES).document();
            Map<String, Object> queueData = new HashMap<>();
            queueData.put(FirestorePaths.FIELD_USER_ID, uid);
            queueData.put(FirestorePaths.FIELD_CUSTOMER_NAME, customerName != null ? customerName : "Guest");
            queueData.put(FirestorePaths.FIELD_IS_VIP, false);
            queueData.put(FirestorePaths.FIELD_IS_PRIORITY, false);
            queueData.put(FirestorePaths.FIELD_QUEUE_NUMBER, queueNumber);
            queueData.put(FirestorePaths.FIELD_POSITION, position);
            queueData.put(FirestorePaths.FIELD_ESTIMATED_MINUTES, estimatedMinutes);
            queueData.put(FirestorePaths.FIELD_STATUS, FirestorePaths.STATUS_WAITING);
            queueData.put(FirestorePaths.FIELD_CREATED_AT, FieldValue.serverTimestamp());
            transaction.set(queueRef, queueData);

            DocumentReference userRef = db.collection(FirestorePaths.COLLECTION_USERS).document(uid);
            transaction.set(userRef, Map.of(FirestorePaths.FIELD_ACTIVE_QUEUE_ID, queueRef.getId()),
                    com.google.firebase.firestore.SetOptions.merge());

            QueueTicket ticket = new QueueTicket(queueNumber, position, estimatedMinutes, nowServing);
            return new QueueStateSnapshot(nowServing, queueNumber, position, estimatedMinutes,
                    ticket, true, queueRef.getId());
        }).addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void refreshQueue(String uid, String activeQueueId, BackendCallback<QueueStateSnapshot> callback) {
        DocumentReference stateRef = db.collection(FirestorePaths.COLLECTION_QUEUE_GLOBAL)
                .document(FirestorePaths.DOC_QUEUE_STATE);

        db.runTransaction((Transaction.Function<QueueStateSnapshot>) transaction -> {
            DocumentSnapshot stateSnapshot = transaction.get(stateRef);
            String nowServing = "A015";
            long nextCounter = 21L;
            if (stateSnapshot.exists()) {
                String servingValue = stateSnapshot.getString(FirestorePaths.FIELD_NOW_SERVING);
                if (servingValue != null) {
                    nowServing = servingValue;
                }
                Long counterValue = stateSnapshot.getLong(FirestorePaths.FIELD_NEXT_COUNTER);
                if (counterValue != null) {
                    nextCounter = counterValue;
                }
            }

            int servingNum = parseQueueNumber(nowServing);
            if (servingNum < nextCounter - 1) {
                nowServing = String.format(Locale.US, "A%03d", servingNum + 1);
                transaction.update(stateRef, FirestorePaths.FIELD_NOW_SERVING, nowServing);
            }

            DocumentReference queueRef = db.collection(FirestorePaths.COLLECTION_QUEUES).document(activeQueueId);
            DocumentSnapshot queueSnapshot = transaction.get(queueRef);
            if (!queueSnapshot.exists()) {
                return new QueueStateSnapshot(nowServing, "-", 0, 0, null, false);
            }

            String queueNumber = queueSnapshot.getString(FirestorePaths.FIELD_QUEUE_NUMBER);
            int position = calculatePosition(queueNumber, nowServing);
            int estimatedMinutes = Math.max(3, position * 3);
            String status = position <= 0 ? FirestorePaths.STATUS_SERVING : FirestorePaths.STATUS_WAITING;

            transaction.update(queueRef,
                    FirestorePaths.FIELD_POSITION, position,
                    FirestorePaths.FIELD_ESTIMATED_MINUTES, estimatedMinutes,
                    FirestorePaths.FIELD_STATUS, status);

            QueueTicket ticket = new QueueTicket(queueNumber, position, estimatedMinutes, nowServing);
            return new QueueStateSnapshot(nowServing, queueNumber, position, estimatedMinutes, ticket, true);
        }).addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void cancelQueue(String uid, String activeQueueId, String queueNumber,
                            BackendCallback<Void> callback) {
        DocumentReference queueRef = db.collection(FirestorePaths.COLLECTION_QUEUES).document(activeQueueId);
        queueRef.update(FirestorePaths.FIELD_STATUS, FirestorePaths.STATUS_CANCELLED)
                .addOnSuccessListener(unused -> {
                    appendHistory(uid, queueNumber, "-", QueueHistory.STATUS_CANCELLED);
                    db.collection(FirestorePaths.COLLECTION_USERS).document(uid)
                            .update(FirestorePaths.FIELD_ACTIVE_QUEUE_ID, FieldValue.delete())
                            .addOnSuccessListener(v -> callback.onSuccess(null))
                            .addOnFailureListener(e -> callback.onSuccess(null));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void appendHistory(String uid, String queueNumber, String foodName, String status) {
        String dateLabel = new SimpleDateFormat("MMM d, yyyy", Locale.US).format(new Date());
        Map<String, Object> entry = new HashMap<>();
        entry.put(FirestorePaths.FIELD_DATE_LABEL, dateLabel);
        entry.put(FirestorePaths.FIELD_QUEUE_NUMBER, queueNumber);
        entry.put(FirestorePaths.FIELD_FOOD_NAME, foodName);
        entry.put(FirestorePaths.FIELD_STATUS, status);
        entry.put(FirestorePaths.FIELD_CREATED_AT, FieldValue.serverTimestamp());

        db.collection(FirestorePaths.COLLECTION_USERS).document(uid)
                .collection(FirestorePaths.SUBCOLLECTION_HISTORY)
                .add(entry);
    }

    private QueueTicket ticketFromSnapshot(DocumentSnapshot snapshot, String nowServing) {
        String queueNumber = snapshot.getString(FirestorePaths.FIELD_QUEUE_NUMBER);
        Long position = snapshot.getLong(FirestorePaths.FIELD_POSITION);
        Long estimated = snapshot.getLong(FirestorePaths.FIELD_ESTIMATED_MINUTES);
        int pos = position != null ? position.intValue() : 0;
        int wait = estimated != null ? estimated.intValue() : 0;
        return new QueueTicket(queueNumber, pos, wait, nowServing);
    }

    private int calculatePosition(String queueNumber, String nowServing) {
        return Math.max(0, parseQueueNumber(queueNumber) - parseQueueNumber(nowServing));
    }

    private int parseQueueNumber(String value) {
        if (value == null || value.length() < 2) {
            return 0;
        }
        try {
            return Integer.parseInt(value.substring(1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
