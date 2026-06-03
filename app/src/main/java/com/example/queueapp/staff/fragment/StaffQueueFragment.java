package com.example.queueapp.staff.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.queueapp.R;
import com.example.queueapp.backend.BackendCallback;
import com.example.queueapp.backend.queue.StaffOperationsRepository;
import com.example.queueapp.staff.QueueDetailBottomSheet;
import com.example.queueapp.staff.adapter.StaffQueueAdapter;
import com.example.queueapp.staff.model.StaffQueueEntry;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class StaffQueueFragment extends Fragment {

    private StaffOperationsRepository repository = new StaffOperationsRepository();
    private StaffQueueAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_queue, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvQueueList = view.findViewById(R.id.rvQueueList);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        adapter = new StaffQueueAdapter(entry -> {
            QueueDetailBottomSheet sheet = QueueDetailBottomSheet.newInstance(entry);
            sheet.setOnUpdatedListener(this::loadQueues);
            sheet.show(getParentFragmentManager(), "queue_detail");
        });
        rvQueueList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvQueueList.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.staff_primary);
        swipeRefresh.setOnRefreshListener(this::loadQueues);
        loadQueues();
    }

    private void loadQueues() {
        repository.loadLiveQueues(new BackendCallback<List<StaffQueueEntry>>() {
            @Override
            public void onSuccess(List<StaffQueueEntry> entries) {
                if (!isAdded()) {
                    return;
                }
                adapter.setItems(entries);
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onError(String message) {
                if (isAdded()) {
                    swipeRefresh.setRefreshing(false);
                    Snackbar.make(requireView(), getString(R.string.action_failed, message),
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
}
