package com.example.queueapp.staff.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.queueapp.R;
import com.example.queueapp.api.ApiConfig;
import com.example.queueapp.api.ApiService;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.ProfileResponse;
import com.example.queueapp.auth.RoleNavigation;
import com.example.queueapp.data.AppSession;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffProfileFragment extends Fragment {

    private ApiService apiService;
    private TextView tvStaffProfileName;
    private TextView tvStaffProfileEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        apiService = ApiConfig.getApiService();
        tvStaffProfileName = view.findViewById(R.id.tvStaffProfileName);
        tvStaffProfileEmail = view.findViewById(R.id.tvStaffProfileEmail);
        MaterialButton btnStaffLogout = view.findViewById(R.id.btnStaffLogout);

        btnStaffLogout.setOnClickListener(v -> logout());

        loadProfile();
    }

    private void loadProfile() {
        apiService.getProfile().enqueue(new Callback<ApiResponse<ProfileResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProfileResponse>> call, Response<ApiResponse<ProfileResponse>> response) {
                if (!isAdded()) return;
                
                ApiResponse<ProfileResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null && body.getData().getUser() != null) {
                    ProfileResponse profile = body.getData();
                    tvStaffProfileName.setText(profile.getUser().getName());
                    tvStaffProfileEmail.setText(profile.getUser().getEmail());
                } else {
                    AppSession session = AppSession.getInstance();
                    tvStaffProfileName.setText(session.getUserName());
                    tvStaffProfileEmail.setText(session.getUserEmail());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProfileResponse>> call, Throwable t) {
                if (!isAdded()) return;
                AppSession session = AppSession.getInstance();
                tvStaffProfileName.setText(session.getUserName());
                tvStaffProfileEmail.setText(session.getUserEmail());
            }
        });
    }

    private void logout() {
        apiService.logout().enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                doLocalLogout();
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                doLocalLogout();
            }
        });
    }

    private void doLocalLogout() {
        if (!isAdded()) return;
        AppSession.getInstance().resetSession();
        RoleNavigation.navigateToLogin(requireContext());
        requireActivity().finish();
    }
}
