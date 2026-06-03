package com.example.queueapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queueapp.R;
import com.example.queueapp.adapter.HistoryAdapter;
import com.example.queueapp.data.AppSession;

public class HistoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvHistory = view.findViewById(R.id.rvHistory);
        View emptyState = view.findViewById(R.id.emptyHistoryState);

        HistoryAdapter adapter = new HistoryAdapter();
        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHistory.setAdapter(adapter);

        AppSession.getInstance().loadQueueHistory(new com.example.queueapp.backend.BackendCallback<java.util.List<com.example.queueapp.model.QueueHistory>>() {
            @Override
            public void onSuccess(java.util.List<com.example.queueapp.model.QueueHistory> history) {
                if (!isAdded()) {
                    return;
                }
                adapter.setItems(history);
                emptyState.setVisibility(history.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                adapter.setItems(java.util.Collections.emptyList());
                emptyState.setVisibility(View.VISIBLE);
            }
        });
    }
}
