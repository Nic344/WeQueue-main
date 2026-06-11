package com.example.queueapp.staff.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.queueapp.R;
import com.example.queueapp.api.model.StaffDashboardResponse;
import com.example.queueapp.api.model.StaffQueueItem;
import com.example.queueapp.data.AppSession;
import com.example.queueapp.staff.adapter.StaffQueueAdapter;
import com.example.queueapp.viewmodel.StaffDashboardViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;

/** Staff Dashboard screen (MVVM). */
public class StaffDashboardFragment extends Fragment {

    private static final long AUTO_REFRESH_INTERVAL = 30_000L;

    private StaffDashboardViewModel viewModel;
    private StaffQueueAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

    private TextView tvNowServing;
    private TextView cardActiveQueue;
    private TextView cardCustomersToday;
    private TextView cardAvgWait;
    private TextView cardCompleted;
    private MaterialButton btnCallNext;
    private MaterialButton btnCompleteService;

    private final Handler autoRefreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAdded()) {
                viewModel.loadDashboard();
                autoRefreshHandler.postDelayed(this, AUTO_REFRESH_INTERVAL);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StaffDashboardViewModel.class);
        AppSession session = AppSession.getInstance();

        TextView tvStaffWelcome = view.findViewById(R.id.tvStaffWelcome);
        tvNowServing = view.findViewById(R.id.tvNowServing);
        cardActiveQueue = view.findViewById(R.id.cardActiveQueue);
        cardCustomersToday = view.findViewById(R.id.cardCustomersToday);
        cardAvgWait = view.findViewById(R.id.cardAvgWait);
        cardCompleted = view.findViewById(R.id.cardCompleted);
        RecyclerView rvLiveQueue = view.findViewById(R.id.rvLiveQueue);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        btnCallNext = view.findViewById(R.id.btnCallNext);
        MaterialButton btnSkipQueue = view.findViewById(R.id.btnSkipQueue);
        btnCompleteService = view.findViewById(R.id.btnCompleteService);
        MaterialButton btnRefreshQueue = view.findViewById(R.id.btnRefreshQueue);

        tvStaffWelcome.setText(getString(R.string.staff_welcome, session.getUserName()));

        adapter = new StaffQueueAdapter(null, false);
        rvLiveQueue.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvLiveQueue.setAdapter(adapter);

        btnCallNext.setOnClickListener(v -> {
            btnCallNext.setEnabled(false);
            btnCallNext.setText(R.string.loading);
            viewModel.callNext();
        });
        btnCompleteService.setOnClickListener(v -> confirmComplete());
        btnSkipQueue.setVisibility(View.GONE);
        btnRefreshQueue.setOnClickListener(v -> viewModel.loadDashboard());

        swipeRefresh.setOnRefreshListener(() -> viewModel.loadDashboard());

        observeViewModel();
        viewModel.loadDashboard();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadDashboard();
        autoRefreshHandler.postDelayed(autoRefreshRunnable, AUTO_REFRESH_INTERVAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
    }

    private void observeViewModel() {
        viewModel.getDashboard().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            swipeRefresh.setRefreshing(false);
            if (resource.isSuccess() && resource.data != null) {
                populateDashboard(resource.data);
            } else if (resource.isError()) {
                Snackbar.make(requireView(),
                                resource.message != null ? resource.message : "Failed to load dashboard",
                                Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> viewModel.loadDashboard()).show();
            }
        });

        viewModel.getCallNextResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            btnCallNext.setEnabled(true);
            btnCallNext.setText(R.string.call_next);
            if (resource.isSuccess()) {
                StaffQueueItem item = resource.data;
                if (item != null) {
                    String dialogMsg = "Now calling: " + item.getQueueNumber();
                    if (item.getCustomerName() != null) {
                        dialogMsg += " — " + item.getCustomerName();
                    }
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Queue Called")
                            .setMessage(dialogMsg)
                            .setPositiveButton(android.R.string.ok, (d, w) -> viewModel.loadDashboard())
                            .setCancelable(false)
                            .show();
                } else {
                    Snackbar.make(requireView(), "No customers waiting", Snackbar.LENGTH_SHORT).show();
                }
            } else if (resource.isError()) {
                Snackbar.make(requireView(),
                        resource.message != null ? resource.message : "Failed to call next",
                        Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getCompleteResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            btnCompleteService.setEnabled(true);
            if (resource.isSuccess()) {
                Snackbar.make(requireView(), "Queue completed!", Snackbar.LENGTH_SHORT).show();
                viewModel.loadDashboard();
            } else if (resource.isError()) {
                Snackbar.make(requireView(),
                        resource.message != null ? resource.message : "No serving queue found",
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void populateDashboard(StaffDashboardResponse data) {
        StaffQueueItem serving = data.getNowServing();
        if (serving != null) {
            String servingText = getString(R.string.now_serving_label) + ": " + serving.getQueueNumber();
            if (serving.getCustomerName() != null) {
                servingText += " — " + serving.getCustomerName();
            }
            if (serving.getFoodName() != null) {
                servingText += " (" + serving.getFoodName() + ")";
            }
            tvNowServing.setText(servingText);
        } else {
            tvNowServing.setText(getString(R.string.now_serving_label) + ": —");
        }

        cardActiveQueue.setText(getString(R.string.status_waiting) + "\n" + data.getWaitingCount());
        cardCustomersToday.setText(getString(R.string.status_completed) + "\n" + data.getCompletedToday());
        cardAvgWait.setText(getString(R.string.status_cancelled) + "\n" + data.getCancelledToday());
        cardCompleted.setText("Total\n"
                + (data.getWaitingCount() + data.getCompletedToday() + data.getCancelledToday()));

        if (data.getWaitingList() != null) {
            adapter.setItems(data.getWaitingList());
        } else {
            adapter.setItems(Collections.emptyList());
        }
    }

    private void confirmComplete() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.complete_queue)
                .setMessage("Mark current queue as completed?")
                .setPositiveButton(R.string.yes, (d, w) -> {
                    btnCompleteService.setEnabled(false);
                    viewModel.completeServing();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
