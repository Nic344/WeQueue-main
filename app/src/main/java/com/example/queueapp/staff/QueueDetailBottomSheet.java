package com.example.queueapp.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.queueapp.R;
import com.example.queueapp.api.model.StaffQueueItem;
import com.example.queueapp.viewmodel.StaffQueueViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class QueueDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_ENTRY_ID = "entry_id";
    private static final String ARG_ENTRY_NUMBER = "entry_number";
    private static final String ARG_ENTRY_NAME = "entry_name";
    private static final String ARG_ENTRY_STATUS = "entry_status";

    private StaffQueueViewModel viewModel;
    private Runnable onUpdated;

    private int queueId;
    private String queueNumber;
    private String customerName;
    private String status;

    public static QueueDetailBottomSheet newInstance(StaffQueueItem item) {
        QueueDetailBottomSheet sheet = new QueueDetailBottomSheet();
        Bundle args = new Bundle();
        args.putInt(ARG_ENTRY_ID, item.getId());
        args.putString(ARG_ENTRY_NUMBER, item.getQueueNumber());
        args.putString(ARG_ENTRY_NAME, item.getCustomerName());
        args.putString(ARG_ENTRY_STATUS, item.getStatus());
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

        viewModel = new ViewModelProvider(this).get(StaffQueueViewModel.class);

        if (getArguments() != null) {
            queueId = getArguments().getInt(ARG_ENTRY_ID);
            queueNumber = getArguments().getString(ARG_ENTRY_NUMBER);
            customerName = getArguments().getString(ARG_ENTRY_NAME);
            status = getArguments().getString(ARG_ENTRY_STATUS);
        }

        TextView tvDetailQueueNumber = view.findViewById(R.id.tvDetailQueueNumber);
        TextView tvDetailCustomer = view.findViewById(R.id.tvDetailCustomer);
        TextView tvDetailStatus = view.findViewById(R.id.tvDetailStatus);
        MaterialButton btnCallCustomer = view.findViewById(R.id.btnCallCustomer);
        MaterialButton btnCompleteQueue = view.findViewById(R.id.btnCompleteQueue);
        MaterialButton btnSkipQueue = view.findViewById(R.id.btnSkipQueue);
        MaterialButton btnCancelQueueDetail = view.findViewById(R.id.btnCancelQueueDetail);

        tvDetailQueueNumber.setText(queueNumber);
        tvDetailCustomer.setText(customerName != null ? customerName : "");
        tvDetailStatus.setText(status != null ? status : "");

        if ("waiting".equals(status)) {
            btnCallCustomer.setVisibility(View.GONE);
            btnCompleteQueue.setVisibility(View.GONE);
            btnSkipQueue.setVisibility(View.VISIBLE);
            btnCancelQueueDetail.setVisibility(View.VISIBLE);
        } else if ("serving".equals(status)) {
            btnCallCustomer.setVisibility(View.GONE);
            btnCompleteQueue.setVisibility(View.VISIBLE);
            btnSkipQueue.setVisibility(View.GONE);
            btnCancelQueueDetail.setVisibility(View.VISIBLE);
        } else {
            btnCallCustomer.setVisibility(View.GONE);
            btnCompleteQueue.setVisibility(View.GONE);
            btnSkipQueue.setVisibility(View.GONE);
            btnCancelQueueDetail.setVisibility(View.GONE);
        }

        btnSkipQueue.setOnClickListener(v -> viewModel.skip(queueId));
        btnCompleteQueue.setOnClickListener(v -> viewModel.complete(queueId));
        btnCancelQueueDetail.setOnClickListener(v -> viewModel.cancel(queueId));

        viewModel.getActionResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            if (resource.isSuccess()) {
                if (onUpdated != null) {
                    onUpdated.run();
                }
                Toast.makeText(requireContext(), "Done", Toast.LENGTH_SHORT).show();
                dismiss();
            } else if (resource.isError()) {
                Toast.makeText(requireContext(),
                        getString(R.string.action_failed,
                                resource.message != null ? resource.message : ""),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
