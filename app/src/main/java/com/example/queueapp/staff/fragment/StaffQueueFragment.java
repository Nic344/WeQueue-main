package com.example.queueapp.staff.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.queueapp.R;
import com.example.queueapp.api.ApiConfig;
import com.example.queueapp.api.ApiService;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.StaffAllQueuesResponse;
import com.example.queueapp.api.model.StaffQueueItem;
import com.example.queueapp.staff.adapter.StaffQueueAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffQueueFragment extends Fragment implements StaffQueueAdapter.OnQueueActionListener {

    private ApiService apiService;
    private StaffQueueAdapter adapter;
    private RecyclerView rvQueueList;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private String currentStatusFilter = "all";
    private List<StaffQueueItem> masterList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_queue, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = ApiConfig.getApiService();

        rvQueueList = view.findViewById(R.id.rvQueueList);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        SearchView searchView = view.findViewById(R.id.searchView);

        adapter = new StaffQueueAdapter(this, true);
        rvQueueList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvQueueList.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.staff_primary);
        swipeRefresh.setOnRefreshListener(() -> loadQueues(currentStatusFilter));

        setupChips(view);
        setupSearch(searchView);
        loadQueues(currentStatusFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadQueues(currentStatusFilter);
    }

    private void setupChips(View view) {
        Chip chipAll = view.findViewById(R.id.chipAll);
        Chip chipWaiting = view.findViewById(R.id.chipWaiting);
        Chip chipServing = view.findViewById(R.id.chipServing);
        Chip chipCompleted = view.findViewById(R.id.chipCompleted);
        Chip chipCancelled = view.findViewById(R.id.chipCancelled);

        View.OnClickListener chipListener = v -> {
            if (v == chipAll) currentStatusFilter = "all";
            else if (v == chipWaiting) currentStatusFilter = "waiting";
            else if (v == chipServing) currentStatusFilter = "serving";
            else if (v == chipCompleted) currentStatusFilter = "completed";
            else if (v == chipCancelled) currentStatusFilter = "cancelled";
            loadQueues(currentStatusFilter);
        };

        chipAll.setOnClickListener(chipListener);
        chipWaiting.setOnClickListener(chipListener);
        chipServing.setOnClickListener(chipListener);
        chipCompleted.setOnClickListener(chipListener);
        chipCancelled.setOnClickListener(chipListener);
    }

    private void setupSearch(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterLocally(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterLocally(newText);
                return true;
            }
        });

        searchView.setOnCloseListener(() -> {
            adapter.setItems(masterList);
            updateEmptyState();
            return false;
        });
    }

    private void filterLocally(String query) {
        if (query == null || query.trim().isEmpty()) {
            adapter.setItems(masterList);
            updateEmptyState();
            return;
        }
        String lowerQuery = query.toLowerCase().trim();
        List<StaffQueueItem> filtered = new ArrayList<>();
        for (StaffQueueItem item : masterList) {
            boolean matchQueue = item.getQueueNumber() != null
                    && item.getQueueNumber().toLowerCase().contains(lowerQuery);
            boolean matchName = item.getCustomerName() != null
                    && item.getCustomerName().toLowerCase().contains(lowerQuery);
            if (matchQueue || matchName) {
                filtered.add(item);
            }
        }
        adapter.setItems(filtered);
        updateEmptyState();
    }

    private void loadQueues(String statusFilter) {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        apiService.getAllQueues(statusFilter, "", 1, 50)
                .enqueue(new Callback<ApiResponse<StaffAllQueuesResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<StaffAllQueuesResponse>> call,
                                           Response<ApiResponse<StaffAllQueuesResponse>> response) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);

                        ApiResponse<StaffAllQueuesResponse> body = response.body();
                        if (response.isSuccessful() && body != null && body.isSuccess()
                                && body.getData() != null && body.getData().getQueues() != null) {
                            masterList = new ArrayList<>(body.getData().getQueues());
                            adapter.setItems(masterList);
                        } else {
                            masterList = Collections.emptyList();
                            adapter.setItems(masterList);
                            String msg = body != null && body.getMessage() != null
                                    ? body.getMessage() : "Failed to load queues";
                            Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.retry, v -> loadQueues(statusFilter)).show();
                        }
                        updateEmptyState();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<StaffAllQueuesResponse>> call, Throwable t) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        masterList = Collections.emptyList();
                        adapter.setItems(masterList);
                        updateEmptyState();
                        Snackbar.make(requireView(), getString(R.string.network_error, t.getMessage()),
                                        Snackbar.LENGTH_LONG)
                                .setAction(R.string.retry, v -> loadQueues(statusFilter)).show();
                    }
                });
    }

    private void updateEmptyState() {
        boolean empty = adapter.getItemCount() == 0;
        tvEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvQueueList.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // --- Action callbacks ---

    @Override
    public void onSkip(StaffQueueItem item) {
        JsonObject body = new JsonObject();
        body.addProperty("queue_id", item.getId());

        apiService.skipQueue(body).enqueue(new Callback<ApiResponse<StaffQueueItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<StaffQueueItem>> call,
                                   Response<ApiResponse<StaffQueueItem>> response) {
                if (!isAdded()) return;
                ApiResponse<StaffQueueItem> b = response.body();
                if (response.isSuccessful() && b != null && b.isSuccess()) {
                    Snackbar.make(requireView(), "Queue skipped", Snackbar.LENGTH_SHORT).show();
                    loadQueues(currentStatusFilter);
                } else {
                    String msg = b != null && b.getMessage() != null ? b.getMessage() : "Skip failed";
                    Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<StaffQueueItem>> call, Throwable t) {
                if (!isAdded()) return;
                Snackbar.make(requireView(), getString(R.string.network_error, t.getMessage()),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onCancel(StaffQueueItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.cancel_queue)
                .setMessage("Cancel queue " + item.getQueueNumber() + "?")
                .setPositiveButton(R.string.yes, (d, w) -> {
                    JsonObject body = new JsonObject();
                    body.addProperty("queue_id", item.getId());

                    apiService.staffCancelQueue(body).enqueue(new Callback<ApiResponse<StaffQueueItem>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<StaffQueueItem>> call,
                                               Response<ApiResponse<StaffQueueItem>> response) {
                            if (!isAdded()) return;
                            ApiResponse<StaffQueueItem> b = response.body();
                            if (response.isSuccessful() && b != null && b.isSuccess()) {
                                Snackbar.make(requireView(), "Queue cancelled", Snackbar.LENGTH_SHORT).show();
                                loadQueues(currentStatusFilter);
                            } else {
                                String msg = b != null && b.getMessage() != null ? b.getMessage() : "Cancel failed";
                                Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<StaffQueueItem>> call, Throwable t) {
                            if (!isAdded()) return;
                            Snackbar.make(requireView(), getString(R.string.network_error, t.getMessage()),
                                    Snackbar.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public void onComplete(StaffQueueItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.complete_queue)
                .setMessage("Complete queue " + item.getQueueNumber() + "?")
                .setPositiveButton(R.string.yes, (d, w) -> {
                    JsonObject body = new JsonObject();
                    body.addProperty("queue_id", item.getId());

                    apiService.completeQueue(body).enqueue(new Callback<ApiResponse<StaffQueueItem>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<StaffQueueItem>> call,
                                               Response<ApiResponse<StaffQueueItem>> response) {
                            if (!isAdded()) return;
                            ApiResponse<StaffQueueItem> b = response.body();
                            if (response.isSuccessful() && b != null && b.isSuccess()) {
                                Snackbar.make(requireView(), "Queue completed!", Snackbar.LENGTH_SHORT).show();
                                loadQueues(currentStatusFilter);
                            } else {
                                String msg = b != null && b.getMessage() != null ? b.getMessage() : "Complete failed";
                                Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<StaffQueueItem>> call, Throwable t) {
                            if (!isAdded()) return;
                            Snackbar.make(requireView(), getString(R.string.network_error, t.getMessage()),
                                    Snackbar.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public void onItemClick(StaffQueueItem item) {
        // Optional: open detail bottom sheet
    }
}
