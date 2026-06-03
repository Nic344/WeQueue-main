package com.example.queueapp.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.queueapp.R;
import com.example.queueapp.backend.BackendCallback;
import com.example.queueapp.backend.queue.StaffOperationsRepository;
import com.example.queueapp.staff.model.StaffQueueEntry;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class QueueDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_ENTRY = "entry";

    private StaffQueueEntry entry;
    private StaffOperationsRepository repository = new StaffOperationsRepository();
    private Runnable onUpdated;

    public static QueueDetailBottomSheet newInstance(StaffQueueEntry entry) {
        QueueDetailBottomSheet sheet = new QueueDetailBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ENTRY, new SerializableEntry(entry));
        sheet.setArguments(args);
        return sheet;
    }

    public void setOnUpdatedListener(Runnable onUpdated) {
        this.onUpdated = onUpdated;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_queue_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SerializableEntry serialized = (SerializableEntry) requireArguments().getSerializable(ARG_ENTRY);
        entry = serialized.toEntry();

        TextView tvDetailQueueNumber = view.findViewById(R.id.tvDetailQueueNumber);
        TextView tvDetailCustomer = view.findViewById(R.id.tvDetailCustomer);
        TextView tvDetailStatus = view.findViewById(R.id.tvDetailStatus);
        MaterialButton btnCallCustomer = view.findViewById(R.id.btnCallCustomer);
        MaterialButton btnCompleteQueue = view.findViewById(R.id.btnCompleteQueue);
        MaterialButton btnSkipQueue = view.findViewById(R.id.btnSkipQueue);
        MaterialButton btnCancelQueueDetail = view.findViewById(R.id.btnCancelQueueDetail);

        tvDetailQueueNumber.setText(entry.getQueueNumber());
        tvDetailCustomer.setText(entry.getCustomerName());
        tvDetailStatus.setText(entry.getStatus());

        btnCallCustomer.setOnClickListener(v -> repository.callNext(new BackendCallback<String>() {
            @Override
            public void onSuccess(String result) {
                notifyUpdated(view, getString(R.string.action_success));
            }

            @Override
            public void onError(String message) {
                notifyError(view, message);
            }
        }));

        btnCompleteQueue.setOnClickListener(v ->
                repository.completeQueue(entry.getId(), entry.getUserId(), entry.getQueueNumber(),
                        new BackendCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                notifyUpdated(view, getString(R.string.action_success));
                            }

                            @Override
                            public void onError(String message) {
                                notifyError(view, message);
                            }
                        }));

        btnSkipQueue.setOnClickListener(v ->
                repository.skipQueue(entry.getId(), new BackendCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        notifyUpdated(view, getString(R.string.action_success));
                    }

                    @Override
                    public void onError(String message) {
                        notifyError(view, message);
                    }
                }));

        btnCancelQueueDetail.setOnClickListener(v ->
                repository.cancelQueue(entry.getId(), entry.getUserId(), new BackendCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        notifyUpdated(view, getString(R.string.action_success));
                    }

                    @Override
                    public void onError(String message) {
                        notifyError(view, message);
                    }
                }));
    }

    private void notifyUpdated(View view, String message) {
        if (onUpdated != null) {
            onUpdated.run();
        }
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
        dismiss();
    }

    private void notifyError(View view, String message) {
        Snackbar.make(view, getString(R.string.action_failed, message), Snackbar.LENGTH_LONG).show();
    }

    static class SerializableEntry implements java.io.Serializable {
        private final String id;
        private final String queueNumber;
        private final String customerName;
        private final int waitingMinutes;
        private final String status;
        private final boolean vip;
        private final boolean priority;
        private final String userId;

        SerializableEntry(StaffQueueEntry entry) {
            id = entry.getId();
            queueNumber = entry.getQueueNumber();
            customerName = entry.getCustomerName();
            waitingMinutes = entry.getWaitingMinutes();
            status = entry.getStatus();
            vip = entry.isVip();
            priority = entry.isPriority();
            userId = entry.getUserId();
        }

        StaffQueueEntry toEntry() {
            return new StaffQueueEntry(id, queueNumber, customerName, waitingMinutes,
                    status, vip, priority, userId);
        }
    }
}
