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

import com.example.queueapp.api.ApiConfig;
import com.example.queueapp.api.ApiErrorHelper;
import com.example.queueapp.api.ApiService;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.LoginResponse;
import com.example.queueapp.api.model.RegisterRequest;
import com.example.queueapp.auth.RoleNavigation;
import com.example.queueapp.data.AppSession;
import com.example.queueapp.data.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFullName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private MaterialButton btnRegister;
    private ProgressBar progressRegister;

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

            setLoading(true);

            ApiService api = ApiConfig.getApiService();
            api.register(new RegisterRequest(name, email, password))
                    .enqueue(new Callback<ApiResponse<LoginResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<LoginResponse>> call,
                                               Response<ApiResponse<LoginResponse>> response) {
                            setLoading(false);
                            if (!response.isSuccessful() || response.body() == null) {
                                String msg = ApiErrorHelper.getMessage(response,
                                        getString(R.string.register_failed));
                                tilEmail.setError(msg);
                                Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
                                return;
                            }

                            ApiResponse<LoginResponse> body = response.body();
                            if (!body.isSuccess() || body.getData() == null
                                    || body.getData().getToken() == null) {
                                String msg = body.getMessage() != null
                                        ? body.getMessage()
                                        : getString(R.string.register_failed);
                                tilEmail.setError(msg);
                                Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
                                return;
                            }

                            LoginResponse data = body.getData();
                            SessionManager.getInstance().saveSession(data.getToken(), data.getUser());
                            AppSession.getInstance().applyUser(data.getUser());

                            Toast.makeText(RegisterActivity.this,
                                    R.string.register_success, Toast.LENGTH_SHORT).show();
                            RoleNavigation.navigateToRoleHome(RegisterActivity.this);
                            finish();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                            setLoading(false);
                            Toast.makeText(RegisterActivity.this,
                                    getString(R.string.network_error, t.getMessage()),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
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
