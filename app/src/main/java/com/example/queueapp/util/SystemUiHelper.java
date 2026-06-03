package com.example.queueapp.util;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.View;
import android.view.Window;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.queueapp.R;

public final class SystemUiHelper {

    private SystemUiHelper() {
    }

    public static void enableEdgeToEdge(Activity activity) {
        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        setNavigationBarColor(activity);
    }

    public static void setNavigationBarColor(Activity activity) {
        activity.getWindow().setNavigationBarColor(activity.getColor(R.color.nav_bar_background));
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                activity.getWindow(), activity.getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightNavigationBars(!isNightMode(activity));
        }
    }

    public static void applyHeaderInsets(View headerView, int horizontalPaddingPx, int bottomPaddingPx) {
        final int initialLeft = headerView.getPaddingLeft();
        final int initialRight = headerView.getPaddingRight();
        final int initialBottom = headerView.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(headerView, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    Math.max(initialLeft, horizontalPaddingPx),
                    bars.top,
                    Math.max(initialRight, horizontalPaddingPx),
                    initialBottom + bottomPaddingPx
            );
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(headerView);
    }

    public static void setLightStatusBarIcons(Activity activity, boolean lightIconsOnDarkBackground) {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                activity.getWindow(), activity.getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(!lightIconsOnDarkBackground);
        }
    }

    public static void setCustomerHomeStatusBar(Activity activity) {
        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        setLightStatusBarIcons(activity, true);
    }

    public static void setDefaultStatusBar(Activity activity) {
        activity.getWindow().setStatusBarColor(activity.getColor(R.color.background));
        setLightStatusBarIcons(activity, isNightMode(activity));
    }

    public static void setQueueScreenStatusBar(Activity activity) {
        activity.getWindow().setStatusBarColor(activity.getColor(R.color.queue_app_bar_background));
        setLightStatusBarIcons(activity, isNightMode(activity));
    }

    public static void setQueueEmptyStatusBar(Activity activity) {
        activity.getWindow().setStatusBarColor(activity.getColor(R.color.queue_empty_background));
        setLightStatusBarIcons(activity, isNightMode(activity));
    }

    private static boolean isNightMode(Activity activity) {
        int nightMode = activity.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return nightMode == Configuration.UI_MODE_NIGHT_YES;
    }
}
