package com.example.queueapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.queueapp.auth.RoleProtectedActivity;
import com.example.queueapp.auth.UserRole;
import com.example.queueapp.data.AppSession;
import com.example.queueapp.fragment.FavoritesFragment;
import com.example.queueapp.fragment.HistoryFragment;
import com.example.queueapp.fragment.HomeFragment;
import com.example.queueapp.fragment.ProfileFragment;
import com.example.queueapp.fragment.QueueFragment;
import com.example.queueapp.util.SystemUiHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends RoleProtectedActivity {

    public static final String EXTRA_NAV_TARGET = "nav_target";
    public static final int NAV_HOME = R.id.nav_home;
    public static final int NAV_QUEUE = R.id.nav_queue;
    public static final int NAV_FAVORITES = R.id.nav_favorites;
    public static final int NAV_HISTORY = R.id.nav_history;
    public static final int NAV_PROFILE = R.id.nav_profile;

    private BottomNavigationView bottomNavigation;
    private View navHostFragment;
    private Fragment currentFragment;

    @Override
    protected String getRequiredRole() {
        return UserRole.CUSTOMER;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemUiHelper.enableEdgeToEdge(this);
        setContentView(R.layout.activity_main);

        AppSession.getInstance().refreshFromBackendIfNeeded();

        navHostFragment = findViewById(R.id.navHostFragment);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        applyMainWindowInsets();

        bottomNavigation.setOnItemSelectedListener(item -> {
            switchFragment(item.getItemId());
            updateStatusBarForTab(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            int target = getIntent().getIntExtra(EXTRA_NAV_TARGET, NAV_HOME);
            bottomNavigation.setSelectedItemId(target);
            switchFragment(target);
            updateStatusBarForTab(target);
        }

        handleNavigationIntent(getIntent());
    }

    /**
     * Equivalent to Scaffold(body: SafeArea(...), bottomNavigationBar: ...) — keeps fragment
     * content above the system gesture bar and the bottom nav fully visible.
     */
    private void applyMainWindowInsets() {
        View root = findViewById(R.id.mainRoot);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            ViewGroup.MarginLayoutParams navLp =
                    (ViewGroup.MarginLayoutParams) bottomNavigation.getLayoutParams();
            navLp.bottomMargin = systemBars.bottom;
            bottomNavigation.setLayoutParams(navLp);

            int navVerticalPadding = (int) (4 * getResources().getDisplayMetrics().density);
            bottomNavigation.setPadding(
                    bottomNavigation.getPaddingLeft(),
                    navVerticalPadding,
                    bottomNavigation.getPaddingRight(),
                    navVerticalPadding);

            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void updateStatusBarForTab(int menuItemId) {
        SystemUiHelper.setNavigationBarColor(this);
        if (menuItemId == NAV_HOME) {
            SystemUiHelper.setCustomerHomeStatusBar(this);
        } else if (menuItemId == NAV_QUEUE) {
            SystemUiHelper.setQueueScreenStatusBar(this);
        } else {
            SystemUiHelper.setDefaultStatusBar(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNavigationIntent(intent);
    }

    private void handleNavigationIntent(Intent intent) {
        if (intent != null && intent.hasExtra(EXTRA_NAV_TARGET)) {
            int target = intent.getIntExtra(EXTRA_NAV_TARGET, NAV_HOME);
            bottomNavigation.setSelectedItemId(target);
            switchFragment(target);
            updateStatusBarForTab(target);
        }
    }

    public void navigateTo(int menuItemId) {
        bottomNavigation.setSelectedItemId(menuItemId);
        switchFragment(menuItemId);
        updateStatusBarForTab(menuItemId);
    }

    private void switchFragment(int menuItemId) {
        Fragment fragment;
        if (menuItemId == NAV_QUEUE) {
            fragment = new QueueFragment();
        } else if (menuItemId == NAV_FAVORITES) {
            fragment = new FavoritesFragment();
        } else if (menuItemId == NAV_HISTORY) {
            fragment = new HistoryFragment();
        } else if (menuItemId == NAV_PROFILE) {
            fragment = new ProfileFragment();
        } else {
            fragment = new HomeFragment();
        }

        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            if (fragment instanceof QueueFragment) {
                ((QueueFragment) currentFragment).refreshUi();
            }
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (currentFragment != null) {
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        transaction.replace(R.id.navHostFragment, fragment);
        transaction.commit();
        currentFragment = fragment;
    }

    @Override
    public void onBackPressed() {
        if (bottomNavigation.getSelectedItemId() != NAV_HOME) {
            navigateTo(NAV_HOME);
        } else {
            super.onBackPressed();
        }
    }
}
