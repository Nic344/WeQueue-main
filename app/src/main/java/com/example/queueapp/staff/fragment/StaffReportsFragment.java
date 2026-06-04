package com.example.queueapp.staff.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.queueapp.R;
import com.example.queueapp.api.ApiConfig;
import com.example.queueapp.api.ApiService;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.StaffStatsResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffReportsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        TextView tvDailyReport = view.findViewById(R.id.tvDailyReport);
        TextView tvWeeklyReport = view.findViewById(R.id.tvWeeklyReport);
        TextView tvMonthlyReport = view.findViewById(R.id.tvMonthlyReport);
        TextView tvQueueStats = view.findViewById(R.id.tvQueueStats);
        TextView tvServiceStats = view.findViewById(R.id.tvServiceStats);
        MaterialButton btnExportReport = view.findViewById(R.id.btnExportReport);
        
        // TODO: connect to real reports API when ready
        // For now, load basic daily stats from existing stats API
        
        ApiConfig.getApiService().getStaffStats().enqueue(new Callback<ApiResponse<StaffStatsResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<StaffStatsResponse>> call, Response<ApiResponse<StaffStatsResponse>> response) {
                if (!isAdded()) return;
                ApiResponse<StaffStatsResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    StaffStatsResponse stats = body.getData();
                    tvDailyReport.setText(getString(R.string.daily_reports) + "\nCustomers: " + stats.getTotalQueues() 
                            + " | Completed: " + stats.getCompletedCount() 
                            + " | Avg wait: " + stats.getAverageServeTime() + "m");
                    tvQueueStats.setText(getString(R.string.queue_statistics) + "\nAvg queue length: -\nMax queue: -");
                    tvServiceStats.setText(getString(R.string.service_statistics) + "\nAvg service: " + stats.getAverageServeTime() + " min\nCancellation: " + stats.getCancelledCount());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<StaffStatsResponse>> call, Throwable t) {}
        });

        tvWeeklyReport.setText(getString(R.string.weekly_reports) + "\n(Feature coming soon)");
        tvMonthlyReport.setText(getString(R.string.monthly_reports) + "\n(Feature coming soon)");

        btnExportReport.setOnClickListener(v ->
                Snackbar.make(view, "Feature coming soon", Snackbar.LENGTH_SHORT).show());
    }
}
