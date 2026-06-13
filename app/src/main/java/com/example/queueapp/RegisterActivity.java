package com.example.queueapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.queueapp.viewmodel.AuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFullName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private MaterialButton btnRegister;
    private ProgressBar progressRegister;
    private AuthViewModel authViewModel;
    private String registeredEmail = "";

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
        progressRegister = findViewById(R.id.progressRegister);
        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);

        btnBack.setOnClickListener(v -> finish());
        tvLoginLink.setOnClickListener(v -> finish());

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
            if (resource.isSuccess()) {
                // Do NOT auto-login. Send the user to Login to sign in manually.
                Toast.makeText(RegisterActivity.this, R.string.register_success_login,
                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.putExtra(LoginActivity.EXTRA_PREFILL_EMAIL, registeredEmail);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            } else {
                String msg = resource.message != null ? resource.message : getString(R.string.register_failed);
                tilEmail.setError(msg);
                Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });

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

            registeredEmail = email;
            authViewModel.register(name, email, password);
        });
    }

    private void setLoading(boolean loading) {
        progressRegister.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
        btnRegister.setText(loading ? R.string.loading : R.string.sign_up);
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
