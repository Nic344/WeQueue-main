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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

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

        tvDailyReport.setText(getString(R.string.daily_reports) + "\nCustomers: 86 | Completed: 81 | Avg wait: 12m");
        tvWeeklyReport.setText(getString(R.string.weekly_reports) + "\nTotal customers: 542 | Peak: Wed");
        tvMonthlyReport.setText(getString(R.string.monthly_reports) + "\nTotal customers: 2,180 | Growth: +8%");
        tvQueueStats.setText(getString(R.string.queue_statistics) + "\nAvg queue length: 4.2\nMax queue: 18");
        tvServiceStats.setText(getString(R.string.service_statistics) + "\nAvg service: 6.5 min\nCancellation: 2.1%");

        btnExportReport.setOnClickListener(v ->
                Snackbar.make(view, "Export report (UI preview)", Snackbar.LENGTH_SHORT).show());
    }
}
