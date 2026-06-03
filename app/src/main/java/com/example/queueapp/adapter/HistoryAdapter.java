package com.example.queueapp.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queueapp.R;
import com.example.queueapp.model.QueueHistory;
import com.example.queueapp.util.FoodImageHelper;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<QueueHistory> items = new ArrayList<>();

    public void setItems(List<QueueHistory> history) {
        items.clear();
        items.addAll(history);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), position == items.size() - 1);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final View timelineLine;
        private final TextView historyDate;
        private final TextView historyQueue;
        private final TextView historyFood;
        private final TextView statusBadge;
        private final ImageView historyFoodImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            timelineLine = itemView.findViewById(R.id.timelineLine);
            historyDate = itemView.findViewById(R.id.historyDate);
            historyQueue = itemView.findViewById(R.id.historyQueue);
            historyFood = itemView.findViewById(R.id.historyFood);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            historyFoodImage = itemView.findViewById(R.id.historyFoodImage);
        }

        void bind(QueueHistory item, boolean isLast) {
            historyDate.setText(item.getDate());
            historyQueue.setText(item.getQueueNumber());
            historyFood.setText(item.getFoodName());
            timelineLine.setVisibility(isLast ? View.INVISIBLE : View.VISIBLE);
            FoodImageHelper.loadHistoryThumbnail(itemView.getContext(), historyFoodImage, item.getFoodName());
            applyStatus(item.getStatus());
        }

        private void applyStatus(String status) {
            int bgColor;
            int textColor;
            String label;
            switch (status) {
                case QueueHistory.STATUS_CANCELLED:
                    bgColor = R.color.history_cancelled_bg;
                    textColor = R.color.history_cancelled_text;
                    label = itemView.getContext().getString(R.string.status_cancelled);
                    break;
                case QueueHistory.STATUS_SERVING:
                    bgColor = R.color.history_serving_bg;
                    textColor = R.color.history_serving_text;
                    label = itemView.getContext().getString(R.string.status_serving);
                    break;
                default:
                    bgColor = R.color.history_completed_bg;
                    textColor = R.color.history_completed_text;
                    label = itemView.getContext().getString(R.string.status_completed);
                    break;
            }
            statusBadge.setText(label);
            statusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), textColor));

            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setCornerRadius(24f);
            badgeBg.setColor(ContextCompat.getColor(itemView.getContext(), bgColor));
            statusBadge.setBackground(badgeBg);
        }
    }
}
