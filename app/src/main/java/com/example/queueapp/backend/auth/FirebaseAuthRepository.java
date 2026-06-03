package com.example.queueapp.backend.auth;

import com.example.queueapp.backend.BackendCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public final class FirebaseAuthRepository {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public boolean isSignedIn() {
        return auth.getCurrentUser() != null;
    }

    public void signIn(String email, String password, BackendCallback<FirebaseUser> callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError("Login failed. Please try again.");
                    }
                })
                .addOnFailureListener(e -> callback.onError(mapAuthError(e)));
    }

    public void register(String email, String password, BackendCallback<FirebaseUser> callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError("Registration failed. Please try again.");
                    }
                })
                .addOnFailureListener(e -> callback.onError(mapAuthError(e)));
    }

    public void signOut() {
        auth.signOut();
    }

    private String mapAuthError(Exception e) {
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            return "Authentication failed. Please try again.";
        }
        if (message.contains("ERROR_EMAIL_ALREADY_IN_USE")) {
            return "This email is already registered.";
        }
        if (message.contains("ERROR_INVALID_EMAIL")) {
            return "Enter a valid email address.";
        }
        if (message.contains("ERROR_WRONG_PASSWORD")
                || message.contains("ERROR_INVALID_CREDENTIAL")
                || message.contains("ERROR_USER_NOT_FOUND")) {
            return "Invalid email or password.";
        }
        if (message.contains("ERROR_WEAK_PASSWORD")) {
            return "Password must be at least 6 characters.";
        }
        if (message.contains("network")) {
            return "Network error. Check your connection.";
        }
        return message;
    }
}
