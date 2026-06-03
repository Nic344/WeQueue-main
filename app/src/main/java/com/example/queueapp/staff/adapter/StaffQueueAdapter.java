package com.example.queueapp.staff.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queueapp.R;
import com.example.queueapp.backend.FirestorePaths;
import com.example.queueapp.staff.model.StaffQueueEntry;

import java.util.ArrayList;
import java.util.List;

public class StaffQueueAdapter extends RecyclerView.Adapter<StaffQueueAdapter.ViewHolder> {

    public interface OnQueueClickListener {
        void onQueueClicked(StaffQueueEntry entry);
    }

    private final List<StaffQueueEntry> items = new ArrayList<>();
    private final OnQueueClickListener listener;

    public StaffQueueAdapter(OnQueueClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<StaffQueueEntry> entries) {
        items.clear();
        items.addAll(entries);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff_queue, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvQueueNumber;
        private final TextView tvStatusBadge;
        private final TextView tvCustomerName;
        private final TextView tvWaitingTime;
        private final LinearLayout tagContainer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQueueNumber = itemView.findViewById(R.id.tvQueueNumber);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvWaitingTime = itemView.findViewById(R.id.tvWaitingTime);
            tagContainer = itemView.findViewById(R.id.tagContainer);
        }

        void bind(StaffQueueEntry entry) {
            tvQueueNumber.setText(entry.getQueueNumber());
            tvCustomerName.setText(entry.getCustomerName());
            tvWaitingTime.setText(itemView.getContext().getString(
                    R.string.waiting_time_format, entry.getWaitingMinutes()));
            tvStatusBadge.setText(formatStatus(entry.getStatus()));
            applyStatusStyle(entry.getStatus());

            tagContainer.removeAllViews();
            if (entry.isVip()) {
                tagContainer.addView(createTag(R.string.status_vip, R.color.status_vip_bg, R.color.status_vip_text));
            }
            if (entry.isPriority()) {
                tagContainer.addView(createTag(R.string.status_priority, R.color.status_priority_bg, R.color.status_priority_text));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQueueClicked(entry);
                }
            });
        }

        private TextView createTag(int labelRes, int bgColor, int textColor) {
            TextView tag = new TextView(itemView.getContext());
            tag.setText(labelRes);
            tag.setTextSize(11f);
            tag.setPadding(16, 8, 16, 8);
            tag.setTextColor(ContextCompat.getColor(itemView.getContext(), textColor));
            tag.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), bgColor));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(8);
            tag.setLayoutParams(params);
            return tag;
        }

        private void applyStatusStyle(String status) {
            int bg = R.color.status_serving;
            int text = R.color.status_serving_text;
            if (FirestorePaths.STATUS_WAITING.equals(status)) {
                bg = R.color.accent;
                text = R.color.text_primary;
            } else if (FirestorePaths.STATUS_CALLED.equals(status)) {
                bg = R.color.status_called_bg;
                text = R.color.status_called_text;
            } else if (FirestorePaths.STATUS_COMPLETED.equals(status)) {
                bg = R.color.status_completed;
                text = R.color.status_completed_text;
            } else if (FirestorePaths.STATUS_CANCELLED.equals(status)
                    || FirestorePaths.STATUS_SKIPPED.equals(status)) {
                bg = R.color.status_cancelled;
                text = R.color.status_cancelled_text;
            }
            tvStatusBadge.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), bg));
            tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), text));
        }

        private String formatStatus(String status) {
            if (FirestorePaths.STATUS_CALLED.equals(status)) {
                return itemView.getContext().getString(R.string.status_called);
            }
            if (FirestorePaths.STATUS_WAITING.equals(status)) {
                return itemView.getContext().getString(R.string.status_waiting);
            }
            if (FirestorePaths.STATUS_COMPLETED.equals(status)) {
                return itemView.getContext().getString(R.string.status_completed);
            }
            if (FirestorePaths.STATUS_CANCELLED.equals(status)) {
                return itemView.getContext().getString(R.string.status_cancelled);
            }
            return status;
        }
    }
}
