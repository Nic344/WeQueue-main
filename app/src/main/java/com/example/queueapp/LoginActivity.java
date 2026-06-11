package com.example.queueapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.queueapp.api.model.LoginResponse;
import com.example.queueapp.auth.RoleNavigation;
import com.example.queueapp.data.AppSession;
import com.example.queueapp.data.SessionManager;
import com.example.queueapp.viewmodel.AuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_SESSION_EXPIRED = "session_expired";

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressLogin;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SessionManager.getInstance().isLoggedIn()) {
            AppSession.getInstance().restoreFromSession();
            RoleNavigation.navigateToRoleHome(this);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressLogin = findViewById(R.id.progressLogin);
        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegisterLink.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.getAuthResult().observe(this, resource -> {
            if (resource == null) {
                return;
            }
            if (resource.isLoading()) {
                setLoading(true);
                return;
            }
            setLoading(false);
            if (resource.isSuccess() && resource.data != null && resource.data.getToken() != null) {
                LoginResponse data = resource.data;
                SessionManager.getInstance().saveSession(data.getToken(), data.getUser());
                AppSession.getInstance().applyUser(data.getUser());
                RoleNavigation.navigateToRoleHome(LoginActivity.this);
                finish();
            } else {
                Toast.makeText(LoginActivity.this,
                        resource.message != null ? resource.message : getString(R.string.login_failed),
                        Toast.LENGTH_LONG).show();
            }
        });

        if (getIntent().getBooleanExtra(EXTRA_SESSION_EXPIRED, false)) {
            Toast.makeText(this, R.string.session_expired, Toast.LENGTH_LONG).show();
        }
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

        authViewModel.login(email, password);
    }

    private void setLoading(boolean loading) {
        progressLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? R.string.loading : R.string.login);
    }
}
