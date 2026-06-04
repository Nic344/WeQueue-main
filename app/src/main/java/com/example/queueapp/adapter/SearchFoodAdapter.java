package com.example.queueapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queueapp.R;
import com.example.queueapp.api.model.FoodModel;
import com.example.queueapp.util.FoodImageHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchFoodAdapter extends RecyclerView.Adapter<SearchFoodAdapter.ViewHolder> {

    private final List<FoodModel> items = new ArrayList<>();

    public void setItems(List<FoodModel> foods) {
        List<FoodModel> safeFoods = foods != null ? foods : new ArrayList<>();
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new FoodDiffCallback(items, safeFoods));
        items.clear();
        items.addAll(safeFoods);
        diff.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_vertical, parent, false);
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
        private final View imageContainer;
        private final ImageView foodIcon;
        private final TextView foodName;
        private final TextView foodDescription;
        private final TextView foodPrice;
        private final ImageButton btnFavorite;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageContainer = itemView.findViewById(R.id.foodImageContainer);
            foodIcon = itemView.findViewById(R.id.foodIcon);
            foodName = itemView.findViewById(R.id.foodName);
            foodDescription = itemView.findViewById(R.id.foodDescription);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }

        void bind(FoodModel item) {
            foodName.setText(item.getName());
            foodDescription.setText(item.getDescription());
            foodPrice.setText(String.format(Locale.getDefault(), "Rp %,.0f", item.getPrice()));
            btnFavorite.setVisibility(View.GONE);
            FoodImageHelper.loadApiFoodImage(itemView.getContext(), foodIcon, imageContainer,
                    item.getImageUrl(), item.getName());
        }
    }

    private static class FoodDiffCallback extends DiffUtil.Callback {
        private final List<FoodModel> oldItems;
        private final List<FoodModel> newItems;

        FoodDiffCallback(List<FoodModel> oldItems, List<FoodModel> newItems) {
            this.oldItems = oldItems;
            this.newItems = newItems;
        }

        @Override
        public int getOldListSize() {
            return oldItems.size();
        }

        @Override
        public int getNewListSize() {
            return newItems.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldItems.get(oldItemPosition).getId() == newItems.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            FoodModel oldItem = oldItems.get(oldItemPosition);
            FoodModel newItem = newItems.get(newItemPosition);
            return String.valueOf(oldItem.getName()).equals(String.valueOf(newItem.getName()))
                    && String.valueOf(oldItem.getDescription()).equals(String.valueOf(newItem.getDescription()))
                    && oldItem.getPrice() == newItem.getPrice()
                    && String.valueOf(oldItem.getImageUrl()).equals(String.valueOf(newItem.getImageUrl()));
        }
    }
}
