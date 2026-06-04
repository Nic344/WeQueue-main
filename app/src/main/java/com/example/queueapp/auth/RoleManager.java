package com.example.queueapp.auth;

import androidx.annotation.Nullable;

public final class RoleManager {

    private static RoleManager instance;

    private String currentRole = UserRole.CUSTOMER;

    private RoleManager() {
    }

    public static synchronized RoleManager getInstance() {
        if (instance == null) {
            instance = new RoleManager();
        }
        return instance;
    }

    public void setRole(@Nullable String role) {
        currentRole = UserRole.normalize(role);
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public void clear() {
        currentRole = UserRole.CUSTOMER;
    }

    public boolean isCustomer() {
        return UserRole.CUSTOMER.equals(currentRole);
    }

    public boolean isStaff() {
        return UserRole.STAFF.equals(currentRole) || UserRole.ADMIN.equals(currentRole);
    }

    public boolean isAdmin() {
        return UserRole.ADMIN.equals(currentRole);
    }

    public boolean hasRole(String role) {
        String normalizedRole = UserRole.normalize(role);
        return currentRole.equals(normalizedRole)
                || (UserRole.STAFF.equals(normalizedRole) && UserRole.ADMIN.equals(currentRole));
    }
}
