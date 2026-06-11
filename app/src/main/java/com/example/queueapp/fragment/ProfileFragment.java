package com.example.queueapp.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.queueapp.LoginActivity;
import com.example.queueapp.NotificationSettingsActivity;
import com.example.queueapp.R;
import com.example.queueapp.api.model.UserModel;
import com.example.queueapp.data.AppSession;
import com.example.queueapp.data.SessionManager;
import com.example.queueapp.util.ImageUploadHelper;
import com.example.queueapp.util.ThemePreferences;
import com.example.queueapp.viewmodel.ProfileViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

import okhttp3.MultipartBody;

/** Profile screen (MVVM): observes {@link ProfileViewModel} for all data work. */
public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileInitial;
    private ImageView ivProfileAvatar;
    private ProgressBar progressProfile;
    private MaterialButton btnLogout;
    private UserModel currentUser;

    private AlertDialog editDialog;
    private AlertDialog passwordDialog;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickAvatarLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    uploadAvatar(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        ThemePreferences themePrefs = ThemePreferences.getInstance(requireContext());

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileInitial = view.findViewById(R.id.tvProfileInitial);
        ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar);
        progressProfile = view.findViewById(R.id.progressProfile);
        MaterialSwitch switchDarkMode = view.findViewById(R.id.switchDarkMode);
        btnLogout = view.findViewById(R.id.btnLogout);

        View avatarContainer = view.findViewById(R.id.avatarContainer);
        avatarContainer.setOnClickListener(v -> pickAvatarLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));

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
        setupMenuItem(view.findViewById(R.id.menuChangePassword), R.string.change_password,
                this::showChangePasswordDialog);
        setupMenuItem(view.findViewById(R.id.menuNotifications), R.string.notifications,
                () -> startActivity(new Intent(requireContext(), NotificationSettingsActivity.class)));
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

        observeViewModel();
        viewModel.loadProfile();
    }

    private void observeViewModel() {
        viewModel.getProfile().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) {
                return;
            }
            setLoading(resource.isLoading());
            if (resource.isSuccess() && resource.data != null && resource.data.getUser() != null) {
                currentUser = resource.data.getUser();
                SessionManager.getInstance().saveSession(
                        SessionManager.getInstance().getToken(), currentUser);
                bindProfile(currentUser);
            } else if (resource.isError()) {
                showRetry(resource.message, () -> viewModel.loadProfile());
            }
        });

        viewModel.getUpdateResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            setLoading(false);
            if (resource.isSuccess() && resource.data != null && resource.data.getUser() != null) {
                currentUser = resource.data.getUser();
                SessionManager.getInstance().saveSession(
                        SessionManager.getInstance().getToken(), currentUser);
                bindProfile(currentUser);
                if (editDialog != null && editDialog.isShowing()) {
                    editDialog.dismiss();
                }
                Snackbar.make(requireView(), R.string.profile_updated, Snackbar.LENGTH_SHORT).show();
            } else if (resource.isError()) {
                enableDialogButton(editDialog);
                Snackbar.make(requireView(),
                        resource.message != null ? resource.message : getString(R.string.image_upload_failed),
                        Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getPasswordResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            if (resource.isSuccess()) {
                if (passwordDialog != null && passwordDialog.isShowing()) {
                    passwordDialog.dismiss();
                }
                Snackbar.make(requireView(), R.string.password_changed, Snackbar.LENGTH_SHORT).show();
            } else if (resource.isError()) {
                enableDialogButton(passwordDialog);
                Snackbar.make(requireView(),
                        resource.message != null ? resource.message : getString(R.string.password_change_failed),
                        Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getUploadResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            if (resource.isSuccess() && resource.data != null && resource.data.getUrl() != null) {
                // Persist the uploaded image URL onto the profile.
                String name = currentUser != null ? currentUser.getName() : AppSession.getInstance().getUserName();
                String email = currentUser != null ? currentUser.getEmail() : AppSession.getInstance().getUserEmail();
                viewModel.updateProfile(name, email, resource.data.getUrl());
            } else if (resource.isError()) {
                setLoading(false);
                Snackbar.make(requireView(),
                        resource.message != null ? resource.message : getString(R.string.image_upload_failed),
                        Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getLogoutResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && !resource.isLoading()) {
                finishLogout();
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

        editDialog = new MaterialAlertDialogBuilder(
                requireContext(), R.style.ThemeOverlay_QueueApp_AlertDialog)
                .setTitle(R.string.edit_profile)
                .setView(content)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();
        editDialog.setOnShowListener(d -> editDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
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
            editDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            viewModel.updateProfile(name, email, null);
        }));
        editDialog.show();
    }

    private void showChangePasswordDialog() {
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_change_password, null);
        TextInputLayout tilCurrent = content.findViewById(R.id.tilCurrentPassword);
        TextInputLayout tilNew = content.findViewById(R.id.tilNewPassword);
        TextInputLayout tilConfirm = content.findViewById(R.id.tilConfirmPassword);
        TextInputEditText etCurrent = content.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNew = content.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirm = content.findViewById(R.id.etConfirmPassword);

        passwordDialog = new MaterialAlertDialogBuilder(
                requireContext(), R.style.ThemeOverlay_QueueApp_AlertDialog)
                .setTitle(R.string.change_password)
                .setView(content)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();

        passwordDialog.setOnShowListener(d -> passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            tilCurrent.setError(null);
            tilNew.setError(null);
            tilConfirm.setError(null);

            String current = etCurrent.getText() != null ? etCurrent.getText().toString() : "";
            String newPass = etNew.getText() != null ? etNew.getText().toString() : "";
            String confirm = etConfirm.getText() != null ? etConfirm.getText().toString() : "";

            if (current.isEmpty()) {
                tilCurrent.setError(getString(R.string.password_required));
                return;
            }
            if (newPass.length() < 6) {
                tilNew.setError(getString(R.string.password_min));
                return;
            }
            if (newPass.equals(current)) {
                tilNew.setError(getString(R.string.password_same_as_current));
                return;
            }
            if (!newPass.equals(confirm)) {
                tilConfirm.setError(getString(R.string.password_mismatch));
                return;
            }
            passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            viewModel.changePassword(current, newPass);
        }));
        passwordDialog.show();
    }

    private void uploadAvatar(@NonNull Uri uri) {
        setLoading(true);
        Snackbar.make(requireView(), R.string.uploading_image, Snackbar.LENGTH_SHORT).show();

        MultipartBody.Part part;
        try {
            part = ImageUploadHelper.createImagePart(requireContext(), uri);
        } catch (IOException e) {
            setLoading(false);
            Snackbar.make(requireView(), R.string.image_upload_failed, Snackbar.LENGTH_LONG).show();
            return;
        }
        viewModel.uploadImage(part);
    }

    private void bindProfile(@Nullable UserModel user) {
        String name = user != null && user.getName() != null ? user.getName() : AppSession.getInstance().getUserName();
        String email = user != null && user.getEmail() != null ? user.getEmail() : AppSession.getInstance().getUserEmail();
        tvProfileName.setText(name);
        tvProfileEmail.setText(email);
        tvProfileInitial.setText(name != null && !name.isEmpty()
                ? String.valueOf(Character.toUpperCase(name.charAt(0)))
                : "W");

        String picture = user != null ? user.getProfilePicture() : null;
        if (picture != null && !picture.trim().isEmpty()) {
            ivProfileAvatar.setVisibility(View.VISIBLE);
            tvProfileInitial.setVisibility(View.GONE);
            Glide.with(this)
                    .load(picture.trim())
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(ivProfileAvatar);
        } else {
            ivProfileAvatar.setVisibility(View.GONE);
            tvProfileInitial.setVisibility(View.VISIBLE);
        }
    }

    private void performLogout() {
        btnLogout.setEnabled(false);
        viewModel.logout();
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

    private void enableDialogButton(@Nullable AlertDialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }
    }

    private void showRetry(@Nullable String message, Runnable retry) {
        Snackbar.make(requireView(),
                        message != null ? message : getString(R.string.load_failed, getString(R.string.profile)),
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, v -> retry.run())
                .show();
    }

    private void setupMenuItem(View menuRoot, int labelRes, Runnable action) {
        TextView label = menuRoot.findViewById(R.id.menuLabel);
        label.setText(labelRes);
        menuRoot.setOnClickListener(v -> action.run());
    }
}
