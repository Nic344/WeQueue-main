package com.example.queueapp.staff;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.queueapp.R;
import com.example.queueapp.auth.RoleProtectedActivity;
import com.example.queueapp.auth.UserRole;
import com.example.queueapp.staff.fragment.StaffDashboardFragment;
import com.example.queueapp.staff.fragment.StaffOverviewFragment;
import com.example.queueapp.staff.fragment.StaffProfileFragment;
import com.example.queueapp.staff.fragment.StaffQueueFragment;
import com.example.queueapp.staff.fragment.StaffReportsFragment;
import com.example.queueapp.staff.fragment.StaffTeamPerformanceFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StaffMainActivity extends RoleProtectedActivity {

    private Fragment currentFragment;

    @Override
    protected String getRequiredRole() {
        return UserRole.STAFF;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_main);

        BottomNavigationView bottomNavigation = findViewById(R.id.staffBottomNav);
        bottomNavigation.setOnItemSelectedListener(item -> {
            switchFragment(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.staff_nav_dashboard);
            switchFragment(R.id.staff_nav_dashboard);
        }
    }

    private void switchFragment(int menuItemId) {
        Fragment fragment;
        if (menuItemId == R.id.staff_nav_queue) {
            fragment = new StaffQueueFragment();
        } else if (menuItemId == R.id.staff_nav_overview) {
            fragment = new StaffOverviewFragment();
        } else if (menuItemId == R.id.staff_nav_reports) {
            fragment = new StaffReportsFragment();
        } else if (menuItemId == R.id.staff_nav_team) {
            fragment = new StaffTeamPerformanceFragment();
        } else if (menuItemId == R.id.staff_nav_profile) {
            fragment = new StaffProfileFragment();
        } else {
            fragment = new StaffDashboardFragment();
        }

        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.staffNavHost, fragment);
        transaction.commit();
        currentFragment = fragment;
    }
}
