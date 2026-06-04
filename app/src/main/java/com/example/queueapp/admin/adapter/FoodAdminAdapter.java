package com.example.queueapp.admin.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.queueapp.R;
import com.example.queueapp.api.model.FoodModel;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodAdminAdapter extends RecyclerView.Adapter<FoodAdminAdapter.FoodViewHolder> implements Filterable {

    private List<FoodModel> allFoods = new ArrayList<>();
    private List<FoodModel> filteredFoods = new ArrayList<>();
    private final OnFoodAdminClickListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public interface OnFoodAdminClickListener {
        void onEditClick(FoodModel food);
        void onDeleteClick(FoodModel food);
    }

    public FoodAdminAdapter(OnFoodAdminClickListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<FoodModel> items) {
        this.allFoods = new ArrayList<>(items);
        this.filteredFoods = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodModel food = filteredFoods.get(position);
        holder.bind(food);
    }

    @Override
    public int getItemCount() {
        return filteredFoods.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString().toLowerCase().trim();
                List<FoodModel> filtered = new ArrayList<>();
                if (query.isEmpty()) {
                    filtered.addAll(allFoods);
                } else {
                    for (FoodModel item : allFoods) {
                        if (item.getName().toLowerCase().contains(query) ||
                            (item.getCategory() != null && item.getCategory().toLowerCase().contains(query))) {
                            filtered.add(item);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filtered;
                return results;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredFoods.clear();
                if (results.values != null) {
                    filteredFoods.addAll((List<FoodModel>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    class FoodViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivFoodImage;
        TextView tvFoodName, tvFoodCategory, tvFoodPrice, tvFoodAvailable;
        ImageButton btnEditFood, btnDeleteFood;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodCategory = itemView.findViewById(R.id.tvFoodCategory);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodPrice);
            tvFoodAvailable = itemView.findViewById(R.id.tvFoodAvailable);
            btnEditFood = itemView.findViewById(R.id.btnEditFood);
            btnDeleteFood = itemView.findViewById(R.id.btnDeleteFood);
        }

        void bind(FoodModel food) {
            tvFoodName.setText(food.getName());
            tvFoodCategory.setText(food.getCategory() != null ? food.getCategory() : "Uncategorized");
            tvFoodPrice.setText(currencyFormat.format(food.getPrice()));
            
            if (food.isAvailable()) {
                tvFoodAvailable.setText("Available");
                tvFoodAvailable.setTextColor(Color.parseColor("#4CAF50")); // Green
            } else {
                tvFoodAvailable.setText("Out of Stock");
                tvFoodAvailable.setTextColor(Color.parseColor("#F44336")); // Red
            }

            Glide.with(itemView.getContext())
                    .load(food.getImageUrl())
                    .placeholder(R.drawable.placeholder_food)
                    .error(R.drawable.placeholder_food)
                    .into(ivFoodImage);

            btnEditFood.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(food);
            });

            btnDeleteFood.setOnClickListener(v -> {
                Context context = itemView.getContext();
                new AlertDialog.Builder(context)
                        .setTitle("Delete Food")
                        .setMessage("Are you sure you want to delete " + food.getName() + "?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            if (listener != null) listener.onDeleteClick(food);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }
}
