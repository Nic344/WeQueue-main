package com.example.queueapp.staff.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.queueapp.R;
import com.example.queueapp.backend.BackendCallback;
import com.example.queueapp.backend.queue.StaffOperationsRepository;
import com.example.queueapp.data.AppSession;
import com.example.queueapp.staff.adapter.StaffQueueAdapter;
import com.example.queueapp.staff.model.StaffDashboardStats;
import com.example.queueapp.staff.model.StaffQueueEntry;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class StaffDashboardFragment extends Fragment {

    private StaffOperationsRepository repository = new StaffOperationsRepository();
    private StaffQueueAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private List<StaffQueueEntry> latestQueues;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppSession session = AppSession.getInstance();

        TextView tvStaffWelcome = view.findViewById(R.id.tvStaffWelcome);
        TextView tvNowServing = view.findViewById(R.id.tvNowServing);
        TextView cardActiveQueue = view.findViewById(R.id.cardActiveQueue);
        TextView cardCustomersToday = view.findViewById(R.id.cardCustomersToday);
        TextView cardAvgWait = view.findViewById(R.id.cardAvgWait);
        TextView cardCompleted = view.findViewById(R.id.cardCompleted);
        RecyclerView rvLiveQueue = view.findViewById(R.id.rvLiveQueue);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        tvStaffWelcome.setText(getString(R.string.staff_welcome, session.getUserName()));

        adapter = new StaffQueueAdapter(null);
        rvLiveQueue.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvLiveQueue.setAdapter(adapter);

        MaterialButton btnCallNext = view.findViewById(R.id.btnCallNext);
        MaterialButton btnSkipQueue = view.findViewById(R.id.btnSkipQueue);
        MaterialButton btnCompleteService = view.findViewById(R.id.btnCompleteService);
        MaterialButton btnRefreshQueue = view.findViewById(R.id.btnRefreshQueue);

        btnCallNext.setOnClickListener(v -> callNext(view));
        btnSkipQueue.setOnClickListener(v -> skipFirstWaiting(view));
        btnCompleteService.setOnClickListener(v -> completeFirstActive(view));
        btnRefreshQueue.setOnClickListener(v -> loadData(cardActiveQueue, cardCustomersToday, cardAvgWait, cardCompleted, tvNowServing));

        swipeRefresh.setOnRefreshListener(() ->
                loadData(cardActiveQueue, cardCustomersToday, cardAvgWait, cardCompleted, tvNowServing));

        loadData(cardActiveQueue, cardCustomersToday, cardAvgWait, cardCompleted, tvNowServing);
    }

    private void loadData(TextView cardActive, TextView cardCustomers, TextView cardAvg, TextView cardCompleted,
                          TextView tvNowServing) {
        repository.loadDashboardStats(new BackendCallback<StaffDashboardStats>() {
            @Override
            public void onSuccess(StaffDashboardStats stats) {
                if (!isAdded()) {
                    return;
                }
                tvNowServing.setText(getString(R.string.now_serving_label) + ": " + stats.getNowServing());
                cardActive.setText(getString(R.string.active_queue) + "\n" + stats.getActiveQueue());
                cardCustomers.setText(getString(R.string.customers_today) + "\n" + stats.getCustomersToday());
                cardAvg.setText(getString(R.string.avg_wait_time) + "\n" + stats.getAverageWaitMinutes() + " min");
                cardCompleted.setText(getString(R.string.completed_orders) + "\n" + stats.getCompletedOrders());
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    Snackbar.make(requireView(), getString(R.string.action_failed, message), Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        repository.loadLiveQueues(new BackendCallback<List<StaffQueueEntry>>() {
            @Override
            public void onSuccess(List<StaffQueueEntry> entries) {
                if (!isAdded()) {
                    return;
                }
                latestQueues = entries;
                adapter.setItems(entries);
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onError(String message) {
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void callNext(View view) {
        repository.callNext(new BackendCallback<String>() {
            @Override
            public void onSuccess(String result) {
                if (isAdded()) {
                    Snackbar.make(view, getString(R.string.queue_taken, result), Snackbar.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(true);
                    onResume();
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    Snackbar.make(view, getString(R.string.action_failed, message), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void skipFirstWaiting(View view) {
        if (latestQueues == null) {
            return;
        }
        for (StaffQueueEntry entry : latestQueues) {
            if (com.example.queueapp.backend.FirestorePaths.STATUS_WAITING.equals(entry.getStatus())) {
                repository.skipQueue(entry.getId(), simpleCallback(view));
                return;
            }
        }
        Snackbar.make(view, R.string.action_failed, Snackbar.LENGTH_SHORT).show();
    }

    private void completeFirstActive(View view) {
        if (latestQueues == null) {
            return;
        }
        for (StaffQueueEntry entry : latestQueues) {
            if (com.example.queueapp.backend.FirestorePaths.STATUS_CALLED.equals(entry.getStatus())
                    || com.example.queueapp.backend.FirestorePaths.STATUS_SERVING.equals(entry.getStatus())) {
                repository.completeQueue(entry.getId(), entry.getUserId(), entry.getQueueNumber(),
                        simpleCallback(view));
                return;
            }
        }
        Snackbar.make(view, getString(R.string.action_failed, "No active service"), Snackbar.LENGTH_SHORT).show();
    }

    private BackendCallback<Void> simpleCallback(View view) {
        return new BackendCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (isAdded()) {
                    Snackbar.make(view, R.string.action_success, Snackbar.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(true);
                    onResume();
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    Snackbar.make(view, getString(R.string.action_failed, message), Snackbar.LENGTH_SHORT).show();
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view != null) {
            loadData(view.findViewById(R.id.cardActiveQueue),
                    view.findViewById(R.id.cardCustomersToday),
                    view.findViewById(R.id.cardAvgWait),
                    view.findViewById(R.id.cardCompleted),
                    view.findViewById(R.id.tvNowServing));
        }
    }
}
