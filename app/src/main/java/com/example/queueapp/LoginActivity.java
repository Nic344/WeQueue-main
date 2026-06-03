package com.example.queueapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.queueapp.auth.RoleNavigation;
import com.example.queueapp.data.AppSession;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);

        etEmail.setText("devin@wequeue.app");
        etPassword.setText("password123");

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegisterLink.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        boolean valid = true;
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.email_required));
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.email_invalid));
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.password_required));
            valid = false;
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.password_min));
            valid = false;
        }
        if (!valid) {
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText(R.string.loading);

        AppSession session = AppSession.getInstance();
        session.getAuthRepository().signIn(email, password, new com.example.queueapp.backend.BackendCallback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser user) {
                String displayName = user.getDisplayName();
                if (displayName == null || displayName.isEmpty()) {
                    displayName = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
                }
                String finalDisplayName = displayName;
                session.onLoginSuccess(user, finalDisplayName, () -> runOnUiThread(() -> {
                    session.setLoggedIn(true);
                    session.setUserEmail(email);

                    Snackbar.make(findViewById(R.id.loginCard), R.string.login_success, Snackbar.LENGTH_SHORT).show();
                    RoleNavigation.navigateToRoleHome(LoginActivity.this);
                    finish();
                }));
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText(R.string.login);
                    tilPassword.setError(message);
                });
            }
        });
    }
}
