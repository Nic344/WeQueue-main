package com.example.queueapp.staff.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.queueapp.R;
import com.example.queueapp.backend.BackendCallback;
import com.example.queueapp.backend.queue.StaffOperationsRepository;
import com.example.queueapp.data.AppSession;
import com.example.queueapp.staff.model.StaffDashboardStats;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class StaffOverviewFragment extends Fragment {

    private final StaffOperationsRepository repository = new StaffOperationsRepository();

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

        TextView tvStaffOverviewWelcome = view.findViewById(R.id.tvStaffOverviewWelcome);
        TextView kpiCustomers = view.findViewById(R.id.kpiCustomers);
        TextView kpiAvgWait = view.findViewById(R.id.kpiAvgWait);
        TextView kpiCompletion = view.findViewById(R.id.kpiCompletion);
        TextView kpiSatisfaction = view.findViewById(R.id.kpiSatisfaction);
        TextView kpiRevenue = view.findViewById(R.id.kpiRevenue);
        TextView tvPeakHour = view.findViewById(R.id.tvPeakHour);
        MaterialButton btnResetDaily = view.findViewById(R.id.btnResetDaily);

        tvStaffOverviewWelcome.setText(getString(R.string.staff_welcome, session.getUserName()));

        repository.loadDashboardStats(new BackendCallback<StaffDashboardStats>() {
            @Override
            public void onSuccess(StaffDashboardStats stats) {
                if (!isAdded()) {
                    return;
                }
                kpiCustomers.setText(getString(R.string.kpi_customers_today) + "\n" + stats.getCustomersToday());
                kpiAvgWait.setText(getString(R.string.kpi_avg_wait) + "\n" + stats.getAverageWaitMinutes() + " min");
                int completion = stats.getCustomersToday() > 0
                        ? (stats.getCompletedOrders() * 100) / stats.getCustomersToday() : 0;
                kpiCompletion.setText(getString(R.string.kpi_completion_rate) + "\n" + completion + "%");
                kpiSatisfaction.setText(getString(R.string.kpi_satisfaction) + "\n4.7 / 5.0");
                kpiRevenue.setText(getString(R.string.kpi_revenue) + "\nRp 12.4M");
                tvPeakHour.setText(getString(R.string.peak_hour_indicator, "11:30 AM"));
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    Snackbar.make(view, getString(R.string.action_failed, message), Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        btnResetDaily.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.reset_daily_queue)
                        .setMessage(R.string.reset_daily_confirm)
                        .setPositiveButton(R.string.yes, (d, w) ->
                                repository.resetDailyQueue(new BackendCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        if (isAdded()) {
                                            Snackbar.make(view, R.string.reset_success, Snackbar.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onError(String message) {
                                        if (isAdded()) {
                                            Snackbar.make(view, getString(R.string.action_failed, message),
                                                    Snackbar.LENGTH_SHORT).show();
                                        }
                                    }
                                }))
                        .setNegativeButton(R.string.no, null)
                        .show());
    }
}
