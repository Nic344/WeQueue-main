package com.example.queueapp.backend.user;

import com.example.queueapp.auth.UserRole;
import com.example.queueapp.backend.BackendCallback;
import com.example.queueapp.backend.FirestorePaths;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UserBackendRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void ensureUserDocument(String uid, String displayName, String email,
                                   BackendCallback<UserProfile> callback) {
        db.collection(FirestorePaths.COLLECTION_USERS).document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        callback.onSuccess(parseProfile(uid, snapshot.getData()));
                    } else {
                        Map<String, Object> data = new HashMap<>();
                        data.put(FirestorePaths.FIELD_DISPLAY_NAME, displayName);
                        data.put(FirestorePaths.FIELD_EMAIL, email);
                        data.put(FirestorePaths.FIELD_ROLE, UserRole.CUSTOMER);
                        data.put(FirestorePaths.FIELD_FAVORITE_IDS, new ArrayList<Integer>());
                        data.put(FirestorePaths.FIELD_ACTIVE_QUEUE_ID, null);
                        db.collection(FirestorePaths.COLLECTION_USERS).document(uid)
                                .set(data)
                                .addOnSuccessListener(unused ->
                                        callback.onSuccess(new UserProfile(uid, displayName, email,
                                                UserRole.CUSTOMER, new ArrayList<>(), null)))
                                .addOnFailureListener(e ->
                                        callback.onError(e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void loadProfile(String uid, BackendCallback<UserProfile> callback) {
        db.collection(FirestorePaths.COLLECTION_USERS).document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        callback.onError("User profile not found.");
                        return;
                    }
                    callback.onSuccess(parseProfile(uid, snapshot.getData()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void saveFavoriteIds(String uid, List<Integer> favoriteIds) {
        Map<String, Object> update = new HashMap<>();
        update.put(FirestorePaths.FIELD_FAVORITE_IDS, new ArrayList<>(favoriteIds));
        db.collection(FirestorePaths.COLLECTION_USERS).document(uid)
                .set(update, SetOptions.merge());
    }

    public void setActiveQueueId(String uid, String queueId) {
        Map<String, Object> update = new HashMap<>();
        update.put(FirestorePaths.FIELD_ACTIVE_QUEUE_ID, queueId);
        db.collection(FirestorePaths.COLLECTION_USERS).document(uid)
                .set(update, SetOptions.merge());
    }

    public void clearActiveQueueId(String uid) {
        Map<String, Object> update = new HashMap<>();
        update.put(FirestorePaths.FIELD_ACTIVE_QUEUE_ID, FieldValue.delete());
        db.collection(FirestorePaths.COLLECTION_USERS).document(uid)
                .update(update);
    }

    @SuppressWarnings("unchecked")
    private UserProfile parseProfile(String uid, Map<String, Object> data) {
        if (data == null) {
            return new UserProfile(uid, "", "", UserRole.CUSTOMER, new ArrayList<>(), null);
        }
        String name = stringValue(data.get(FirestorePaths.FIELD_DISPLAY_NAME));
        String email = stringValue(data.get(FirestorePaths.FIELD_EMAIL));
        String role = stringValue(data.get(FirestorePaths.FIELD_ROLE));
        String activeQueueId = data.get(FirestorePaths.FIELD_ACTIVE_QUEUE_ID) != null
                ? String.valueOf(data.get(FirestorePaths.FIELD_ACTIVE_QUEUE_ID)) : null;

        List<Integer> favorites = new ArrayList<>();
        Object rawFavorites = data.get(FirestorePaths.FIELD_FAVORITE_IDS);
        if (rawFavorites instanceof List) {
            for (Object item : (List<?>) rawFavorites) {
                if (item instanceof Number) {
                    favorites.add(((Number) item).intValue());
                }
            }
        }
        return new UserProfile(uid, name, email, role, favorites, activeQueueId);
    }

    private String stringValue(Object value) {
        return value != null ? String.valueOf(value) : "";
    }
}
