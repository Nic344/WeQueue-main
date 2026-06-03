package com.example.queueapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.queueapp.auth.RoleNavigation;
import com.example.queueapp.data.AppSession;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 2500L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView ivSplashLogo = findViewById(R.id.ivSplashLogo);
        TextView tvTagline = findViewById(R.id.tvTagline);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);

        ivSplashLogo.startAnimation(scaleUp);
        tvTagline.startAnimation(fadeIn);

        new Handler(Looper.getMainLooper()).postDelayed(this::routeAfterSplash, SPLASH_DELAY_MS);
    }

    private void routeAfterSplash() {
        AppSession session = AppSession.getInstance();
        FirebaseUser user = session.getAuthRepository().getCurrentUser();
        if (user != null) {
            session.restoreFromFirebase(user, () -> runOnUiThread(() -> {
                RoleNavigation.navigateToRoleHome(SplashActivity.this);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }));
        } else {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }
}
