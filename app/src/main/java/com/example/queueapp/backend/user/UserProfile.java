package com.example.queueapp.backend.user;

import com.example.queueapp.auth.UserRole;

import java.util.ArrayList;
import java.util.List;

public class UserProfile {

    private final String uid;
    private final String displayName;
    private final String email;
    private final String role;
    private final List<Integer> favoriteIds;
    private final String activeQueueId;

    public UserProfile(String uid, String displayName, String email, String role,
                       List<Integer> favoriteIds, String activeQueueId) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
        this.role = UserRole.normalize(role);
        this.favoriteIds = favoriteIds != null ? new ArrayList<>(favoriteIds) : new ArrayList<>();
        this.activeQueueId = activeQueueId;
    }

    public String getUid() {
        return uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public List<Integer> getFavoriteIds() {
        return favoriteIds;
    }

    public String getActiveQueueId() {
        return activeQueueId;
    }
}
