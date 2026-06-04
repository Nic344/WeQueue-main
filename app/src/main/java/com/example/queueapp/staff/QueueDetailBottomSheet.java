package com.example.queueapp.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.queueapp.R;
import com.example.queueapp.api.ApiConfig;
import com.example.queueapp.api.ApiService;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.StaffQueueItem;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QueueDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_ENTRY_ID = "entry_id";
    private static final String ARG_ENTRY_NUMBER = "entry_number";
    private static final String ARG_ENTRY_NAME = "entry_name";
    private static final String ARG_ENTRY_STATUS = "entry_status";

    private ApiService apiService;
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
        
        apiService = ApiConfig.getApiService();
        
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
        
        // Setup visibility based on status
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

        btnSkipQueue.setOnClickListener(v -> skipQueue());
        btnCompleteQueue.setOnClickListener(v -> completeQueue());
        btnCancelQueueDetail.setOnClickListener(v -> cancelQueue());
    }

    private void skipQueue() {
        JsonObject body = new JsonObject();
        body.addProperty("queue_id", queueId);
        
        apiService.skipQueue(body).enqueue(new Callback<ApiResponse<StaffQueueItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<StaffQueueItem>> call, Response<ApiResponse<StaffQueueItem>> response) {
                handleResponse(response, "Queue skipped");
            }

            @Override
            public void onFailure(Call<ApiResponse<StaffQueueItem>> call, Throwable t) {
                notifyError(t.getMessage());
            }
        });
    }

    private void completeQueue() {
        JsonObject body = new JsonObject();
        body.addProperty("queue_id", queueId);
        
        apiService.completeQueue(body).enqueue(new Callback<ApiResponse<StaffQueueItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<StaffQueueItem>> call, Response<ApiResponse<StaffQueueItem>> response) {
                handleResponse(response, "Queue completed");
            }

            @Override
            public void onFailure(Call<ApiResponse<StaffQueueItem>> call, Throwable t) {
                notifyError(t.getMessage());
            }
        });
    }

    private void cancelQueue() {
        JsonObject body = new JsonObject();
        body.addProperty("queue_id", queueId);
        
        apiService.staffCancelQueue(body).enqueue(new Callback<ApiResponse<StaffQueueItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<StaffQueueItem>> call, Response<ApiResponse<StaffQueueItem>> response) {
                handleResponse(response, "Queue cancelled");
            }

            @Override
            public void onFailure(Call<ApiResponse<StaffQueueItem>> call, Throwable t) {
                notifyError(t.getMessage());
            }
        });
    }

    private void handleResponse(Response<ApiResponse<StaffQueueItem>> response, String successMessage) {
        if (!isAdded()) return;
        ApiResponse<StaffQueueItem> body = response.body();
        if (response.isSuccessful() && body != null && body.isSuccess()) {
            if (onUpdated != null) {
                onUpdated.run();
            }
            Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show();
            dismiss();
        } else {
            String msg = body != null && body.getMessage() != null ? body.getMessage() : "Action failed";
            notifyError(msg);
        }
    }

    private void notifyError(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), getString(R.string.action_failed, message), Toast.LENGTH_LONG).show();
        }
    }
}
