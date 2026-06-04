package com.example.queueapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.queueapp.LoginActivity;
import com.example.queueapp.R;
import com.example.queueapp.api.ApiConfig;
import com.example.queueapp.api.ApiErrorHelper;
import com.example.queueapp.api.ApiService;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.ProfileResponse;
import com.example.queueapp.api.model.UpdateProfileRequest;
import com.example.queueapp.api.model.UserModel;
import com.example.queueapp.data.AppSession;
import com.example.queueapp.data.SessionManager;
import com.example.queueapp.util.ThemePreferences;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private ApiService apiService;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileInitial;
    private ProgressBar progressProfile;
    private MaterialButton btnLogout;
    private UserModel currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = ApiConfig.getApiService();
        ThemePreferences themePrefs = ThemePreferences.getInstance(requireContext());

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileInitial = view.findViewById(R.id.tvProfileInitial);
        progressProfile = view.findViewById(R.id.progressProfile);
        MaterialSwitch switchDarkMode = view.findViewById(R.id.switchDarkMode);
        btnLogout = view.findViewById(R.id.btnLogout);

        currentUser = SessionManager.getInstance().getUser();
        bindProfile(currentUser);

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
                this::showEditProfileDialog);
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
                        .setPositiveButton(R.string.yes, (d, w) -> performLogout())
                        .setNegativeButton(R.string.no, null)
                        .show());

        loadProfile();
    }

    private void loadProfile() {
        setLoading(true);
        apiService.getProfile().enqueue(new Callback<ApiResponse<ProfileResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProfileResponse>> call,
                                   Response<ApiResponse<ProfileResponse>> response) {
                if (!isAdded()) {
                    return;
                }
                ApiResponse<ProfileResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()
                        && body.getData() != null && body.getData().getUser() != null) {
                    currentUser = body.getData().getUser();
                    SessionManager.getInstance().saveSession(
                            SessionManager.getInstance().getToken(), currentUser);
                    bindProfile(currentUser);
                } else {
                    showRetry(response, ProfileFragment.this::loadProfile);
                }
                setLoading(false);
            }

            @Override
            public void onFailure(Call<ApiResponse<ProfileResponse>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                showRetry(t, ProfileFragment.this::loadProfile);
                setLoading(false);
            }
        });
    }

    private void showEditProfileDialog() {
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        content.setPadding(padding, padding / 2, padding, 0);

        EditText nameInput = new EditText(requireContext());
        nameInput.setHint(R.string.full_name);
        nameInput.setSingleLine(true);
        nameInput.setText(currentUser != null ? currentUser.getName() : "");
        content.addView(nameInput);

        EditText emailInput = new EditText(requireContext());
        emailInput.setHint(R.string.email);
        emailInput.setSingleLine(true);
        emailInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setText(currentUser != null ? currentUser.getEmail() : "");
        content.addView(emailInput);

        AlertDialog dialog = new MaterialAlertDialogBuilder(
                requireContext(), R.style.ThemeOverlay_QueueApp_AlertDialog)
                .setTitle(R.string.edit_profile)
                .setView(content)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
            String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
            if (name.isEmpty()) {
                nameInput.setError(getString(R.string.name_required));
                return;
            }
            if (email.isEmpty()) {
                emailInput.setError(getString(R.string.email_required));
                return;
            }
            updateProfile(dialog, name, email);
        }));
        dialog.show();
    }

    private void updateProfile(AlertDialog dialog, String name, String email) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        apiService.updateProfile(new UpdateProfileRequest(name, email, null))
                .enqueue(new Callback<ApiResponse<ProfileResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ProfileResponse>> call,
                                           Response<ApiResponse<ProfileResponse>> response) {
                        if (!isAdded()) {
                            return;
                        }
                        ApiResponse<ProfileResponse> body = response.body();
                        if (response.isSuccessful() && body != null && body.isSuccess()
                                && body.getData() != null && body.getData().getUser() != null) {
                            currentUser = body.getData().getUser();
                            SessionManager.getInstance().saveSession(
                                    SessionManager.getInstance().getToken(), currentUser);
                            bindProfile(currentUser);
                            dialog.dismiss();
                            Snackbar.make(requireView(), R.string.profile_updated, Snackbar.LENGTH_SHORT).show();
                        } else {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            showRetry(response, () -> updateProfile(dialog, name, email));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ProfileResponse>> call, Throwable t) {
                        if (!isAdded()) {
                            return;
                        }
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        showRetry(t, () -> updateProfile(dialog, name, email));
                    }
                });
    }

    private void bindProfile(@Nullable UserModel user) {
        String name = user != null && user.getName() != null ? user.getName() : AppSession.getInstance().getUserName();
        String email = user != null && user.getEmail() != null ? user.getEmail() : AppSession.getInstance().getUserEmail();
        tvProfileName.setText(name);
        tvProfileEmail.setText(email);
        tvProfileInitial.setText(name != null && !name.isEmpty()
                ? String.valueOf(Character.toUpperCase(name.charAt(0)))
                : "W");
    }

    private void performLogout() {
        btnLogout.setEnabled(false);
        apiService.logout().enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                finishLogout();
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                finishLogout();
            }
        });
    }

    private void finishLogout() {
        AppSession.getInstance().resetSession();
        SessionManager.getInstance().clearSession();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void setLoading(boolean loading) {
        progressProfile.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showRetry(Response<?> response, Runnable retry) {
        Snackbar.make(requireView(),
                        ApiErrorHelper.getMessage(response, getString(R.string.load_failed,
                                getString(R.string.profile))),
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, v -> retry.run())
                .show();
    }

    private void showRetry(Throwable t, Runnable retry) {
        Snackbar.make(requireView(), getString(R.string.network_error, t.getMessage()), Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, v -> retry.run())
                .show();
    }

    private void setupMenuItem(View menuRoot, int labelRes, Runnable action) {
        TextView label = menuRoot.findViewById(R.id.menuLabel);
        label.setText(labelRes);
        menuRoot.setOnClickListener(v -> action.run());
    }
}
