package com.example.queueapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.queueapp.LoginActivity;
import com.example.queueapp.R;
import com.example.queueapp.data.AppSession;
import com.example.queueapp.util.ThemePreferences;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppSession session = AppSession.getInstance();
        ThemePreferences themePrefs = ThemePreferences.getInstance(requireContext());

        TextView tvProfileName = view.findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        TextView tvProfileInitial = view.findViewById(R.id.tvProfileInitial);
        MaterialSwitch switchDarkMode = view.findViewById(R.id.switchDarkMode);
        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);

        String name = session.getUserName();
        tvProfileName.setText(name);
        tvProfileEmail.setText(session.getUserEmail());
        if (name != null && !name.isEmpty()) {
            tvProfileInitial.setText(String.valueOf(Character.toUpperCase(name.charAt(0))));
        }

        switchDarkMode.setChecked(themePrefs.isDarkMode());
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) {
                return;
            }
            themePrefs.setDarkMode(isChecked);
            Snackbar.make(view, isChecked ? R.string.dark_mode_on : R.string.dark_mode_off,
                    Snackbar.LENGTH_SHORT).show();
            requireActivity().recreate();
        });

        setupMenuItem(view.findViewById(R.id.menuEditProfile), R.string.edit_profile,
                () -> Snackbar.make(view, R.string.edit_profile_message, Snackbar.LENGTH_SHORT).show());
        setupMenuItem(view.findViewById(R.id.menuNotifications), R.string.notifications,
                () -> Snackbar.make(view, R.string.notifications_message, Snackbar.LENGTH_SHORT).show());
        setupMenuItem(view.findViewById(R.id.menuAbout), R.string.about,
                () -> new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.about)
                        .setMessage(R.string.about_message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show());

        btnLogout.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.logout)
                        .setMessage(R.string.logout_confirm)
                        .setPositiveButton(R.string.yes, (d, w) -> {
                            session.resetSession();
                            Intent intent = new Intent(requireContext(), LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            requireActivity().finish();
                        })
                        .setNegativeButton(R.string.no, null)
                        .show());
    }

    private void setupMenuItem(View menuRoot, int labelRes, Runnable action) {
        TextView label = menuRoot.findViewById(R.id.menuLabel);
        label.setText(labelRes);
        menuRoot.setOnClickListener(v -> action.run());
    }
}
