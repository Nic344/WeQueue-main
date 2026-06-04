package com.example.queueapp.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.queueapp.LoginActivity;
import com.example.queueapp.MainActivity;
import com.example.queueapp.admin.AdminMainActivity;
import com.example.queueapp.staff.StaffMainActivity;

public final class RoleNavigation {

    private RoleNavigation() {
    }

    public static Intent createHomeIntent(Context context) {
        if (RoleManager.getInstance().isAdmin()) {
            return new Intent(context, AdminMainActivity.class);
        } else if (RoleManager.getInstance().isStaff()) {
            return new Intent(context, StaffMainActivity.class);
        }
        return new Intent(context, MainActivity.class);
    }

    public static void navigateToRoleHome(Activity activity) {
        activity.startActivity(createHomeIntent(activity));
    }

    public static void navigateToCorrectDashboard(Activity activity) {
        Intent intent = createHomeIntent(activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void navigateToLogin(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
