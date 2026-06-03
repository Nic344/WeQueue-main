package com.example.queueapp.auth;

public final class UserRole {

    public static final String CUSTOMER = "customer";
    public static final String STAFF = "staff";

    private UserRole() {
    }

    public static String normalize(String role) {
        if (role == null) {
            return CUSTOMER;
        }
        switch (role.toLowerCase()) {
            case STAFF:
            case "manager":
                return STAFF;
            case CUSTOMER:
            default:
                return CUSTOMER;
        }
    }
}
