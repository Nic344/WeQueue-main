package com.example.queueapp.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.queueapp.LoginActivity;
import com.example.queueapp.R;
import com.example.queueapp.admin.fragment.AdminFoodsFragment;
import com.example.queueapp.admin.fragment.AdminUsersFragment;
import com.example.queueapp.data.SessionManager;
import com.example.queueapp.staff.fragment.StaffDashboardFragment;
import com.example.queueapp.staff.fragment.StaffQueueFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminMainActivity extends AppCompatActivity {

    private Toolbar toolbar;

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
        
        toolbar = findViewById(R.id.adminToolbar);
        setSupportActionBar(toolbar);
        
        BottomNavigationView bottomNav = findViewById(R.id.adminBottomNav);
        
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
}
