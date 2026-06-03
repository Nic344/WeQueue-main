package com.example.queueapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.queueapp.data.AppSession;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFullName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private MaterialButton btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        TextInputEditText etFullName = findViewById(R.id.etFullName);
        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        TextInputEditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);

        btnBack.setOnClickListener(v -> finish());
        tvLoginLink.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            clearErrors();
            String name = getText(etFullName);
            String email = getText(etEmail);
            String password = getText(etPassword);
            String confirm = getText(etConfirmPassword);

            boolean valid = true;
            if (TextUtils.isEmpty(name)) {
                tilFullName.setError(getString(R.string.name_required));
                valid = false;
            }
            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.setError(getString(R.string.email_invalid));
                valid = false;
            }
            if (TextUtils.isEmpty(password) || password.length() < 6) {
                tilPassword.setError(getString(R.string.password_min));
                valid = false;
            }
            if (!password.equals(confirm)) {
                tilConfirmPassword.setError(getString(R.string.password_mismatch));
                valid = false;
            }
            if (!valid) {
                return;
            }

            btnRegister.setEnabled(false);
            btnRegister.setText(R.string.loading);

            AppSession session = AppSession.getInstance();
            session.getAuthRepository().register(email, password,
                    new com.example.queueapp.backend.BackendCallback<FirebaseUser>() {
                        @Override
                        public void onSuccess(FirebaseUser user) {
                            session.getUserRepository().ensureUserDocument(
                                    user.getUid(), name, email,
                                    new com.example.queueapp.backend.BackendCallback<com.example.queueapp.backend.user.UserProfile>() {
                                        @Override
                                        public void onSuccess(com.example.queueapp.backend.user.UserProfile result) {
                                            runOnUiThread(() -> {
                                                session.setUserName(name);
                                                session.setUserEmail(email);
                                                session.getAuthRepository().signOut();
                                                Snackbar.make(findViewById(R.id.registerCard),
                                                        R.string.register_success, Snackbar.LENGTH_SHORT).show();
                                                finish();
                                            });
                                        }

                                        @Override
                                        public void onError(String message) {
                                            runOnUiThread(() -> {
                                                session.setUserName(name);
                                                session.setUserEmail(email);
                                                session.getAuthRepository().signOut();
                                                Snackbar.make(findViewById(R.id.registerCard),
                                                        R.string.register_success, Snackbar.LENGTH_SHORT).show();
                                                finish();
                                            });
                                        }
                                    });
                        }

                        @Override
                        public void onError(String message) {
                            runOnUiThread(() -> {
                                btnRegister.setEnabled(true);
                                btnRegister.setText(R.string.sign_up);
                                tilEmail.setError(message);
                            });
                        }
                    });
        });
    }

    private void clearErrors() {
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
