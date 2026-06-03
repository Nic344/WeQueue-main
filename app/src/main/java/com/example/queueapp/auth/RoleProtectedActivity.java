package com.example.queueapp.auth;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.queueapp.data.AppSession;

public abstract class RoleProtectedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppSession session = AppSession.getInstance();
        if (!session.isLoggedIn()) {
            RoleNavigation.navigateToLogin(this);
            finish();
            return;
        }
        if (!isRoleAllowed()) {
            RoleNavigation.navigateToCorrectDashboard(this);
        }
    }

    protected abstract String getRequiredRole();

    protected boolean isRoleAllowed() {
        return RoleManager.getInstance().hasRole(getRequiredRole());
    }
}
