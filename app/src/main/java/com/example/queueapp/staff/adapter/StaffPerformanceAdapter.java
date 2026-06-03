package com.example.queueapp.staff.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queueapp.R;
import com.example.queueapp.staff.model.StaffPerformanceItem;

import java.util.ArrayList;
import java.util.List;

public class StaffPerformanceAdapter extends RecyclerView.Adapter<StaffPerformanceAdapter.ViewHolder> {

    private final List<StaffPerformanceItem> items = new ArrayList<>();

    public void setItems(List<StaffPerformanceItem> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff_performance, parent, false);
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEmployeeName;
        private final TextView tvCompletedQueues;
        private final TextView tvAvgServiceTime;
        private final TextView tvRanking;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvCompletedQueues = itemView.findViewById(R.id.tvCompletedQueues);
            tvAvgServiceTime = itemView.findViewById(R.id.tvAvgServiceTime);
            tvRanking = itemView.findViewById(R.id.tvRanking);
        }

        void bind(StaffPerformanceItem item) {
            tvEmployeeName.setText(item.getName());
            tvCompletedQueues.setText(itemView.getContext().getString(R.string.completed_queues)
                    + ": " + item.getCompletedQueues());
            tvAvgServiceTime.setText(itemView.getContext().getString(R.string.avg_service_time)
                    + ": " + item.getAvgServiceMinutes() + " min");
            tvRanking.setText(itemView.getContext().getString(R.string.performance_ranking)
                    + ": #" + item.getRanking());
        }
    }
}
