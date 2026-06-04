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
import com.example.queueapp.api.model.StaffQueueItem;
import com.example.queueapp.api.model.StaffStatsResponse;
import com.example.queueapp.data.AppSession;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffOverviewFragment extends Fragment {

    private ApiService apiService;

    private TextView kpiCustomers;
    private TextView kpiAvgWait;
    private TextView kpiCompletion;
    private TextView kpiSatisfaction;
    private TextView kpiRevenue;
    private TextView tvPeakHour;
    private TextView tvStaffOverviewWelcome;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppSession session = AppSession.getInstance();
        apiService = ApiConfig.getApiService();

        tvStaffOverviewWelcome = view.findViewById(R.id.tvStaffOverviewWelcome);
        kpiCustomers = view.findViewById(R.id.kpiCustomers);
        kpiAvgWait = view.findViewById(R.id.kpiAvgWait);
        kpiCompletion = view.findViewById(R.id.kpiCompletion);
        kpiSatisfaction = view.findViewById(R.id.kpiSatisfaction);
        kpiRevenue = view.findViewById(R.id.kpiRevenue);
        tvPeakHour = view.findViewById(R.id.tvPeakHour);
        MaterialButton btnResetDaily = view.findViewById(R.id.btnResetDaily);
        
        // Hide unused/irrelevant mock metrics
        kpiSatisfaction.setVisibility(View.GONE);
        kpiRevenue.setVisibility(View.GONE);
        btnResetDaily.setVisibility(View.GONE);

        tvStaffOverviewWelcome.setText(getString(R.string.staff_welcome, session.getUserName()));

        loadStats();
    }

    private void loadStats() {
        apiService.getStaffStats().enqueue(new Callback<ApiResponse<StaffStatsResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<StaffStatsResponse>> call,
                                   Response<ApiResponse<StaffStatsResponse>> response) {
                if (!isAdded()) return;

                ApiResponse<StaffStatsResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    StaffStatsResponse stats = body.getData();
                    
                    kpiCustomers.setText("Total Queues Today\n" + stats.getTotalQueues());
                    kpiAvgWait.setText(getString(R.string.kpi_avg_wait) + "\n" + stats.getAverageServeTime() + " min");
                    kpiCompletion.setText("Completed\n" + stats.getCompletedCount());
                    
                    StaffQueueItem serving = stats.getCurrentlyServing();
                    if (serving != null) {
                        tvStaffOverviewWelcome.setText(getString(R.string.staff_welcome, AppSession.getInstance().getUserName()) 
                            + "\nNow Serving: " + serving.getQueueNumber());
                    }
                    
                    if (stats.getPeakHour() != null) {
                        tvPeakHour.setText(getString(R.string.peak_hour_indicator, stats.getPeakHour()));
                    } else {
                        tvPeakHour.setText("Peak Hour: -");
                    }
                } else {
                    String msg = body != null && body.getMessage() != null ? body.getMessage() : "Failed to load stats";
                    Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG)
                            .setAction(R.string.retry, v -> loadStats()).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<StaffStatsResponse>> call, Throwable t) {
                if (!isAdded()) return;
                Snackbar.make(requireView(), getString(R.string.network_error, t.getMessage()), Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> loadStats()).show();
            }
        });
    }
}
