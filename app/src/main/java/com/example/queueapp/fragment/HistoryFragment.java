package com.example.queueapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queueapp.R;
import com.example.queueapp.adapter.HistoryAdapter;
import com.example.queueapp.api.model.QueueHistoryListResponse;
import com.example.queueapp.viewmodel.HistoryViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;

public class HistoryFragment extends Fragment {

    private HistoryViewModel viewModel;
    private HistoryAdapter adapter;
    private RecyclerView rvHistory;
    private View emptyState;
    private ProgressBar progressHistory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        rvHistory = view.findViewById(R.id.rvHistory);
        emptyState = view.findViewById(R.id.emptyHistoryState);
        progressHistory = view.findViewById(R.id.progressHistory);

        adapter = new HistoryAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHistory.setAdapter(adapter);

        observeViewModel();
        viewModel.loadHistory();
    }

    private void observeViewModel() {
        viewModel.getHistory().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) {
                return;
            }
            setLoading(resource.isLoading());
            if (resource.isSuccess()) {
                List<QueueHistoryListResponse.QueueHistoryItem> history = resource.data != null
                        ? resource.data.getHistory() : null;
                adapter.setItems(history != null ? history : Collections.emptyList());
                updateEmptyState();
            } else if (resource.isError()) {
                adapter.setItems(Collections.emptyList());
                updateEmptyState();
                Snackbar.make(requireView(),
                                resource.message != null ? resource.message
                                        : getString(R.string.load_failed, getString(R.string.history)),
                                Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> viewModel.loadHistory())
                        .show();
            }
        });
    }

    private void updateEmptyState() {
        boolean empty = adapter.getItemCount() == 0;
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvHistory.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progressHistory.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            rvHistory.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        }
    }
}
