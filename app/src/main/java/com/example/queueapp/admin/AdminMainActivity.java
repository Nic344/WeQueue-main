package com.example.queueapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.queueapp.LoginActivity;
import com.example.queueapp.R;
import com.example.queueapp.admin.fragment.AdminFoodsFragment;
import com.example.queueapp.admin.fragment.AdminUsersFragment;
import com.example.queueapp.auth.RoleNavigation;
import com.example.queueapp.data.AppSession;
import com.example.queueapp.data.SessionManager;
import com.example.queueapp.fragment.ProfileFragment;
import com.example.queueapp.staff.fragment.StaffDashboardFragment;
import com.example.queueapp.staff.fragment.StaffQueueFragment;
import com.example.queueapp.viewmodel.AuthViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.lifecycle.ViewModelProvider;

public class AdminMainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!"admin".equals(SessionManager.getInstance().getUser().getRole())) {
            SessionManager.getInstance().clearSession();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_admin_main);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.getLogoutResult().observe(this, resource -> {
            if (resource != null && !resource.isLoading()) {
                finishLogout();
            }
        });

        toolbar = findViewById(R.id.adminToolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.adminBottomNav);

        // The bottom nav is taller than ?attr/actionBarSize (it has labels), so match
        // the fragment container's bottom padding to the real nav height. This keeps
        // content — and the "+" FAB anchored to the bottom — fully above the nav bar.
        View fragmentContainer = findViewById(R.id.adminFragmentContainer);
        bottomNav.post(() -> fragmentContainer.setPadding(
                fragmentContainer.getPaddingLeft(),
                fragmentContainer.getPaddingTop(),
                fragmentContainer.getPaddingRight(),
                bottomNav.getHeight()));

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selectedFragment = null;
            String title = "";
            
            if (id == R.id.nav_dashboard) {
                selectedFragment = new StaffDashboardFragment();
                title = "Dashboard";
            } else if (id == R.id.nav_queue) {
                selectedFragment = new StaffQueueFragment();
                title = "Queue Monitor";
            } else if (id == R.id.nav_foods) {
                selectedFragment = new AdminFoodsFragment();
                title = "Manage Foods";
            } else if (id == R.id.nav_users) {
                selectedFragment = new AdminUsersFragment();
                title = "Manage Users";
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                title = "Profile";
            }
            
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.adminFragmentContainer, selectedFragment)
                        .commit();
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(title);
                }
                return true;
            }
            return false;
        });

        // Load default tab
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_dashboard);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            confirmLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirm)
                .setPositiveButton(R.string.yes, (d, w) -> performLogout())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void performLogout() {
        authViewModel.logout();
    }

    private void finishLogout() {
        AppSession.getInstance().resetSession();
        RoleNavigation.navigateToLogin(this);
        finish();
    }
}
