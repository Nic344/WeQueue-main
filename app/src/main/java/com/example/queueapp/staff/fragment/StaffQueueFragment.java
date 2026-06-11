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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.queueapp.R;
import com.example.queueapp.api.model.StaffQueueItem;
import com.example.queueapp.staff.adapter.StaffQueueAdapter;
import com.example.queueapp.viewmodel.StaffQueueViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/** Staff Queue Monitor screen (MVVM). */
public class StaffQueueFragment extends Fragment implements StaffQueueAdapter.OnQueueActionListener {

    private StaffQueueViewModel viewModel;
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
        viewModel = new ViewModelProvider(this).get(StaffQueueViewModel.class);

        rvQueueList = view.findViewById(R.id.rvQueueList);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        SearchView searchView = view.findViewById(R.id.searchView);

        adapter = new StaffQueueAdapter(this, true);
        rvQueueList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvQueueList.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.staff_primary);
        swipeRefresh.setOnRefreshListener(() -> viewModel.loadQueues(currentStatusFilter));

        setupChips(view);
        setupSearch(searchView);
        observeViewModel();
        viewModel.loadQueues(currentStatusFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadQueues(currentStatusFilter);
    }

    private void observeViewModel() {
        viewModel.getQueues().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) {
                return;
            }
            progressBar.setVisibility(resource.isLoading() ? View.VISIBLE : View.GONE);
            if (!resource.isLoading()) {
                swipeRefresh.setRefreshing(false);
            }
            if (resource.isSuccess()) {
                masterList = resource.data != null && resource.data.getQueues() != null
                        ? new ArrayList<>(resource.data.getQueues()) : new ArrayList<>();
                adapter.setItems(masterList);
                updateEmptyState();
            } else if (resource.isError()) {
                masterList = new ArrayList<>();
                adapter.setItems(masterList);
                updateEmptyState();
                Snackbar.make(requireView(),
                                resource.message != null ? resource.message : "Failed to load queues",
                                Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> viewModel.loadQueues(currentStatusFilter)).show();
            }
        });

        viewModel.getActionResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            if (resource.isSuccess()) {
                Snackbar.make(requireView(), "Done", Snackbar.LENGTH_SHORT).show();
                viewModel.loadQueues(currentStatusFilter);
            } else if (resource.isError()) {
                Snackbar.make(requireView(),
                        resource.message != null ? resource.message : "Action failed",
                        Snackbar.LENGTH_LONG).show();
            }
        });
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
            viewModel.loadQueues(currentStatusFilter);
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

    private void updateEmptyState() {
        boolean empty = adapter.getItemCount() == 0;
        tvEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvQueueList.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // --- Action callbacks ---

    @Override
    public void onSkip(StaffQueueItem item) {
        viewModel.skip(item.getId());
    }

    @Override
    public void onCancel(StaffQueueItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.cancel_queue)
                .setMessage("Cancel queue " + item.getQueueNumber() + "?")
                .setPositiveButton(R.string.yes, (d, w) -> viewModel.cancel(item.getId()))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public void onComplete(StaffQueueItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.complete_queue)
                .setMessage("Complete queue " + item.getQueueNumber() + "?")
                .setPositiveButton(R.string.yes, (d, w) -> viewModel.complete(item.getId()))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public void onItemClick(StaffQueueItem item) {
        // Optional: open detail bottom sheet
    }
}
