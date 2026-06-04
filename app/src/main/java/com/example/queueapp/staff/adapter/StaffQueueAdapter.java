package com.example.queueapp.staff.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queueapp.R;
import com.example.queueapp.api.model.StaffQueueItem;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class StaffQueueAdapter extends RecyclerView.Adapter<StaffQueueAdapter.ViewHolder> {

    public interface OnQueueActionListener {
        void onSkip(StaffQueueItem item);
        void onCancel(StaffQueueItem item);
        void onComplete(StaffQueueItem item);
        void onItemClick(StaffQueueItem item);
    }

    private final List<StaffQueueItem> items = new ArrayList<>();
    private final OnQueueActionListener listener;
    private final boolean showActions;

    public StaffQueueAdapter(OnQueueActionListener listener, boolean showActions) {
        this.listener = listener;
        this.showActions = showActions;
    }

    public StaffQueueAdapter(OnQueueActionListener listener) {
        this(listener, true);
    }

    public void setItems(List<StaffQueueItem> entries) {
        items.clear();
        if (entries != null) {
            items.addAll(entries);
        }
        notifyDataSetChanged();
    }

    public List<StaffQueueItem> getItems() {
        return new ArrayList<>(items);
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
        private final MaterialButton btnSkip;
        private final MaterialButton btnCancel;
        private final MaterialButton btnComplete;
        private final View actionContainer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQueueNumber = itemView.findViewById(R.id.tvQueueNumber);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvWaitingTime = itemView.findViewById(R.id.tvWaitingTime);
            btnSkip = itemView.findViewById(R.id.btnSkipItem);
            btnCancel = itemView.findViewById(R.id.btnCancelItem);
            btnComplete = itemView.findViewById(R.id.btnCompleteItem);
            actionContainer = itemView.findViewById(R.id.actionContainer);
        }

        void bind(StaffQueueItem entry) {
            tvQueueNumber.setText(entry.getQueueNumber());
            String customerInfo = entry.getCustomerName() != null ? entry.getCustomerName() : "";
            if (entry.getFoodName() != null && !entry.getFoodName().isEmpty()) {
                customerInfo += " • " + entry.getFoodName();
            }
            tvCustomerName.setText(customerInfo);

            String timeText = formatTime(entry.getCreatedAt());
            tvWaitingTime.setText(timeText);

            tvStatusBadge.setText(formatStatus(entry.getStatus()));
            applyStatusStyle(entry.getStatus());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(entry);
                }
            });

            if (!showActions || actionContainer == null) {
                if (actionContainer != null) {
                    actionContainer.setVisibility(View.GONE);
                }
                return;
            }

            String status = entry.getStatus() != null ? entry.getStatus() : "";
            switch (status) {
                case "waiting":
                    actionContainer.setVisibility(View.VISIBLE);
                    btnSkip.setVisibility(View.VISIBLE);
                    btnCancel.setVisibility(View.VISIBLE);
                    btnComplete.setVisibility(View.GONE);
                    break;
                case "serving":
                    actionContainer.setVisibility(View.VISIBLE);
                    btnSkip.setVisibility(View.GONE);
                    btnCancel.setVisibility(View.VISIBLE);
                    btnComplete.setVisibility(View.VISIBLE);
                    break;
                default:
                    actionContainer.setVisibility(View.GONE);
                    break;
            }

            btnSkip.setOnClickListener(v -> {
                if (listener != null) listener.onSkip(entry);
            });
            btnCancel.setOnClickListener(v -> {
                if (listener != null) listener.onCancel(entry);
            });
            btnComplete.setOnClickListener(v -> {
                if (listener != null) listener.onComplete(entry);
            });
        }

        private void applyStatusStyle(String status) {
            if (status == null) status = "";
            int bg, text;
            switch (status) {
                case "waiting":
                    bg = R.color.accent;
                    text = R.color.text_primary;
                    break;
                case "serving":
                    bg = R.color.status_serving;
                    text = R.color.status_serving_text;
                    break;
                case "completed":
                    bg = R.color.status_completed;
                    text = R.color.status_completed_text;
                    break;
                case "cancelled":
                    bg = R.color.status_cancelled;
                    text = R.color.status_cancelled_text;
                    break;
                default:
                    bg = R.color.accent;
                    text = R.color.text_primary;
                    break;
            }
            tvStatusBadge.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), bg));
            tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), text));
        }

        private String formatStatus(String status) {
            if (status == null) return "";
            switch (status) {
                case "waiting":
                    return itemView.getContext().getString(R.string.status_waiting);
                case "serving":
                    return itemView.getContext().getString(R.string.status_serving);
                case "completed":
                    return itemView.getContext().getString(R.string.status_completed);
                case "cancelled":
                    return itemView.getContext().getString(R.string.status_cancelled);
                default:
                    return status;
            }
        }

        private String formatTime(String createdAt) {
            if (createdAt == null || createdAt.isEmpty()) return "";
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                Date date = sdf.parse(createdAt);
                if (date != null) {
                    long diffMs = System.currentTimeMillis() - date.getTime();
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs);
                    if (minutes < 1) return "Just now";
                    if (minutes < 60) return minutes + " min ago";
                    long hours = TimeUnit.MILLISECONDS.toHours(diffMs);
                    return hours + "h ago";
                }
            } catch (ParseException ignored) {
            }
            // Fallback: show time portion
            try {
                SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                SimpleDateFormat out = new SimpleDateFormat("HH:mm", Locale.US);
                Date d = in.parse(createdAt);
                if (d != null) return out.format(d);
            } catch (ParseException ignored) {
            }
            return createdAt;
        }
    }
}
