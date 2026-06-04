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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.queueapp.R;
import com.example.queueapp.api.ApiConfig;
import com.example.queueapp.api.ApiService;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.StaffDashboardResponse;
import com.example.queueapp.api.model.StaffQueueItem;
import com.example.queueapp.data.AppSession;
import com.example.queueapp.staff.adapter.StaffQueueAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;

import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffDashboardFragment extends Fragment {

    private static final long AUTO_REFRESH_INTERVAL = 30_000L;

    private ApiService apiService;
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
    private final Runnable autoRefreshRunnable = () -> {
        if (isAdded()) {
            loadDashboard();
            autoRefreshHandler.postDelayed(this.autoRefreshRunnable, AUTO_REFRESH_INTERVAL);
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
        apiService = ApiConfig.getApiService();
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

        btnCallNext.setOnClickListener(v -> callNext());
        btnCompleteService.setOnClickListener(v -> confirmComplete());
        btnSkipQueue.setVisibility(View.GONE);
        btnRefreshQueue.setOnClickListener(v -> loadDashboard());

        swipeRefresh.setOnRefreshListener(this::loadDashboard);

        loadDashboard();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboard();
        autoRefreshHandler.postDelayed(autoRefreshRunnable, AUTO_REFRESH_INTERVAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
    }

    private void loadDashboard() {
        apiService.getStaffDashboard().enqueue(new Callback<ApiResponse<StaffDashboardResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<StaffDashboardResponse>> call,
                                   Response<ApiResponse<StaffDashboardResponse>> response) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);

                ApiResponse<StaffDashboardResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    StaffDashboardResponse data = body.getData();
                    populateDashboard(data);
                } else {
                    String msg = body != null && body.getMessage() != null
                            ? body.getMessage() : "Failed to load dashboard";
                    Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG)
                            .setAction(R.string.retry, v -> loadDashboard()).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<StaffDashboardResponse>> call, Throwable t) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                Snackbar.make(requireView(), getString(R.string.network_error, t.getMessage()),
                                Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> loadDashboard()).show();
            }
        });
    }

    private void populateDashboard(StaffDashboardResponse data) {
        StaffQueueItem serving = data.getNowServing();
        if (serving != null) {
            String servingText = getString(R.string.now_serving_label) + ": "
                    + serving.getQueueNumber();
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
        cardCompleted.setText("Total\n" + (data.getWaitingCount() + data.getCompletedToday() + data.getCancelledToday()));

        if (data.getWaitingList() != null) {
            adapter.setItems(data.getWaitingList());
        } else {
            adapter.setItems(Collections.emptyList());
        }
    }

    private void callNext() {
        btnCallNext.setEnabled(false);
        btnCallNext.setText(R.string.loading);

        apiService.callNext().enqueue(new Callback<ApiResponse<StaffQueueItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<StaffQueueItem>> call,
                                   Response<ApiResponse<StaffQueueItem>> response) {
                if (!isAdded()) return;
                btnCallNext.setEnabled(true);
                btnCallNext.setText(R.string.call_next);

                ApiResponse<StaffQueueItem> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    StaffQueueItem item = body.getData();
                    if (item != null) {
                        String dialogMsg = "Now calling: " + item.getQueueNumber();
                        if (item.getCustomerName() != null) {
                            dialogMsg += " — " + item.getCustomerName();
                        }
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Queue Called")
                                .setMessage(dialogMsg)
                                .setPositiveButton(android.R.string.ok, (d, w) -> loadDashboard())
                                .setCancelable(false)
                                .show();
                    } else {
                        Snackbar.make(requireView(), "No customers waiting", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    String msg = body != null && body.getMessage() != null
                            ? body.getMessage() : "Failed to call next";
                    Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<StaffQueueItem>> call, Throwable t) {
                if (!isAdded()) return;
                btnCallNext.setEnabled(true);
                btnCallNext.setText(R.string.call_next);
                Snackbar.make(requireView(), getString(R.string.network_error, t.getMessage()),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void confirmComplete() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.complete_queue)
                .setMessage("Mark current queue as completed?")
                .setPositiveButton(R.string.yes, (d, w) -> completeQueue())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void completeQueue() {
        btnCompleteService.setEnabled(false);

        apiService.completeQueue(new JsonObject()).enqueue(new Callback<ApiResponse<StaffQueueItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<StaffQueueItem>> call,
                                   Response<ApiResponse<StaffQueueItem>> response) {
                if (!isAdded()) return;
                btnCompleteService.setEnabled(true);

                ApiResponse<StaffQueueItem> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    Snackbar.make(requireView(), "Queue completed!", Snackbar.LENGTH_SHORT).show();
                    loadDashboard();
                } else {
                    String msg = body != null && body.getMessage() != null
                            ? body.getMessage() : "No serving queue found";
                    Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<StaffQueueItem>> call, Throwable t) {
                if (!isAdded()) return;
                btnCompleteService.setEnabled(true);
                Snackbar.make(requireView(), getString(R.string.network_error, t.getMessage()),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
