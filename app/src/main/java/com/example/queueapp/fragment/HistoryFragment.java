package com.example.queueapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queueapp.R;
import com.example.queueapp.adapter.HistoryAdapter;
import com.example.queueapp.api.ApiConfig;
import com.example.queueapp.api.ApiErrorHelper;
import com.example.queueapp.api.ApiService;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.QueueHistoryListResponse;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {

    private ApiService apiService;
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
        apiService = ApiConfig.getApiService();
        rvHistory = view.findViewById(R.id.rvHistory);
        emptyState = view.findViewById(R.id.emptyHistoryState);
        progressHistory = view.findViewById(R.id.progressHistory);

        adapter = new HistoryAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHistory.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        setLoading(true);
        apiService.getQueueHistory().enqueue(new Callback<ApiResponse<QueueHistoryListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<QueueHistoryListResponse>> call,
                                   Response<ApiResponse<QueueHistoryListResponse>> response) {
                if (!isAdded()) {
                    return;
                }
                ApiResponse<QueueHistoryListResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess() && body.getData() != null) {
                    List<QueueHistoryListResponse.QueueHistoryItem> history = body.getData().getHistory();
                    adapter.setItems(history != null ? history : Collections.emptyList());
                    updateEmptyState();
                } else {
                    adapter.setItems(Collections.emptyList());
                    updateEmptyState();
                    showRetry(response, this::retry);
                }
                setLoading(false);
            }

            private void retry() {
                loadHistory();
            }

            @Override
            public void onFailure(Call<ApiResponse<QueueHistoryListResponse>> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }
                adapter.setItems(Collections.emptyList());
                updateEmptyState();
                showRetry(t, HistoryFragment.this::loadHistory);
                setLoading(false);
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

    private void showRetry(Response<?> response, Runnable retry) {
        Snackbar.make(requireView(),
                        ApiErrorHelper.getMessage(response, getString(R.string.load_failed,
                                getString(R.string.history))),
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, v -> retry.run())
                .show();
    }

    private void showRetry(Throwable t, Runnable retry) {
        Snackbar.make(requireView(), getString(R.string.network_error, t.getMessage()), Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, v -> retry.run())
                .show();
    }
}
