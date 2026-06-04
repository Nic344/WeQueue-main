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

import com.example.queueapp.api.ApiConfig;
import com.example.queueapp.api.ApiErrorHelper;
import com.example.queueapp.api.ApiService;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.LoginRequest;
import com.example.queueapp.api.model.LoginResponse;
import com.example.queueapp.auth.RoleNavigation;
import com.example.queueapp.data.AppSession;
import com.example.queueapp.data.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressLogin;

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
        
        MaterialButton btnQuickCustomer = findViewById(R.id.btnQuickCustomer);
        MaterialButton btnQuickStaff = findViewById(R.id.btnQuickStaff);
        MaterialButton btnQuickAdmin = findViewById(R.id.btnQuickAdmin);

        btnQuickCustomer.setOnClickListener(v -> {
            etEmail.setText("customer@wequeue.com");
            etPassword.setText("customer123");
        });
        
        btnQuickStaff.setOnClickListener(v -> {
            etEmail.setText("staff@wequeue.com");
            etPassword.setText("staff123");
        });
        
        btnQuickAdmin.setOnClickListener(v -> {
            etEmail.setText("admin@wequeue.com");
            etPassword.setText("admin123");
        });

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

        setLoading(true);

        ApiService api = ApiConfig.getApiService();
        api.login(new LoginRequest(email, password)).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call,
                                   Response<ApiResponse<LoginResponse>> response) {
                setLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(LoginActivity.this,
                            ApiErrorHelper.getMessage(response, getString(R.string.login_failed)),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                ApiResponse<LoginResponse> body = response.body();
                if (!body.isSuccess() || body.getData() == null
                        || body.getData().getToken() == null) {
                    Toast.makeText(LoginActivity.this,
                            body.getMessage() != null ? body.getMessage() : getString(R.string.login_failed),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                LoginResponse data = body.getData();
                SessionManager.getInstance().saveSession(data.getToken(), data.getUser());
                AppSession.getInstance().applyUser(data.getUser());

                RoleNavigation.navigateToRoleHome(LoginActivity.this);
                finish();
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this,
                        getString(R.string.network_error, t.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? R.string.loading : R.string.login);
    }
}
