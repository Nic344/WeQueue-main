package com.example.queueapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.queueapp.util.NotificationHelper;
import com.example.queueapp.util.NotificationPreferences;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

public class NotificationSettingsActivity extends AppCompatActivity {

    private NotificationPreferences prefs;
    private MaterialSwitch switchMaster;
    private MaterialSwitch switchQueueCalled;
    private MaterialSwitch switchQueueAlmost;
    private View rowQueueCalled;
    private View rowQueueAlmost;
    private TextView tvPermissionWarning;

    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        prefs = NotificationPreferences.getInstance(this);
        NotificationHelper.createChannels(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarNotifications);
        toolbar.setNavigationOnClickListener(v -> finish());

        switchMaster = findViewById(R.id.switchMaster);
        switchQueueCalled = findViewById(R.id.switchQueueCalled);
        switchQueueAlmost = findViewById(R.id.switchQueueAlmost);
        rowQueueCalled = findViewById(R.id.rowQueueCalled);
        rowQueueAlmost = findViewById(R.id.rowQueueAlmost);
        tvPermissionWarning = findViewById(R.id.tvPermissionWarning);
        MaterialButton btnTest = findViewById(R.id.btnTestNotification);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (!granted) {

                        prefs.setMasterEnabled(false);
                        switchMaster.setChecked(false);
                        Toast.makeText(this, R.string.notif_permission_denied, Toast.LENGTH_LONG).show();
                    }
                    syncUi();
                });

        switchMaster.setChecked(prefs.isMasterEnabled());
        switchQueueCalled.setChecked(prefs.isQueueCalledEnabled());
        switchQueueAlmost.setChecked(prefs.isQueueAlmostEnabled());
        syncUi();

        switchMaster.setOnCheckedChangeListener((b, isChecked) -> {
            if (!b.isPressed()) {
                return;
            }
            prefs.setMasterEnabled(isChecked);
            if (isChecked) {
                requestPermissionIfNeeded();
            }
            syncUi();
        });

        switchQueueCalled.setOnCheckedChangeListener((b, isChecked) -> {
            if (b.isPressed()) {
                prefs.setQueueCalledEnabled(isChecked);
            }
        });

        switchQueueAlmost.setOnCheckedChangeListener((b, isChecked) -> {
            if (b.isPressed()) {
                prefs.setQueueAlmostEnabled(isChecked);
            }
        });

        btnTest.setOnClickListener(v -> {
            if (!prefs.isMasterEnabled()) {
                Toast.makeText(this, R.string.notif_enable_first, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!NotificationHelper.hasPermission(this)) {
                requestPermissionIfNeeded();
                return;
            }
            NotificationHelper.notifyTest(this);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncUi();
    }

    private void requestPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void syncUi() {
        boolean master = prefs.isMasterEnabled();
        switchQueueCalled.setEnabled(master);
        switchQueueAlmost.setEnabled(master);
        rowQueueCalled.setAlpha(master ? 1f : 0.5f);
        rowQueueAlmost.setAlpha(master ? 1f : 0.5f);

        boolean permissionMissing = master && !NotificationHelper.hasPermission(this);
        tvPermissionWarning.setVisibility(permissionMissing ? View.VISIBLE : View.GONE);
    }
}
