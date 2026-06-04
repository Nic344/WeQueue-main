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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PopularFoodAdapter extends RecyclerView.Adapter<PopularFoodAdapter.ViewHolder> {

    public interface OnFavoriteClickListener {
        void onFavoriteClick(FoodModel food, boolean currentlyFavorite);
    }

    private final List<FoodModel> items = new ArrayList<>();
    private final Set<Integer> favoriteIds = new HashSet<>();
    private final OnFavoriteClickListener listener;

    public PopularFoodAdapter(OnFavoriteClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<FoodModel> foods) {
        List<FoodModel> safeFoods = foods != null ? foods : new ArrayList<>();
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new FoodDiffCallback(items, safeFoods));
        items.clear();
        items.addAll(safeFoods);
        diff.dispatchUpdatesTo(this);
    }

    public void setFavoriteIds(Set<Integer> ids) {
        favoriteIds.clear();
        if (ids != null) {
            favoriteIds.addAll(ids);
        }
        notifyDataSetChanged();
    }

    public void setFavorite(int foodId, boolean favorite) {
        if (favorite) {
            favoriteIds.add(foodId);
        } else {
            favoriteIds.remove(foodId);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_horizontal, parent, false);
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
        private final View imageContainer;
        private final ImageView foodIcon;
        private final TextView foodName;
        private final TextView foodPrice;
        private final ImageButton btnFavorite;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageContainer = itemView.findViewById(R.id.foodImageContainer);
            foodIcon = itemView.findViewById(R.id.foodIcon);
            foodName = itemView.findViewById(R.id.foodName);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }

        void bind(FoodModel item) {
            foodName.setText(item.getName());
            foodPrice.setText(String.format(Locale.getDefault(), "Rp %,.0f", item.getPrice()));
            FoodImageHelper.loadApiFoodImage(itemView.getContext(), foodIcon, imageContainer,
                    item.getImageUrl(), item.getName());
            boolean favorite = favoriteIds.contains(item.getId());
            btnFavorite.setImageResource(favorite
                    ? R.drawable.ic_favorite_filled
                    : R.drawable.ic_favorite_border);
            btnFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(item, favoriteIds.contains(item.getId()));
                }
            });
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
                    && oldItem.getPrice() == newItem.getPrice()
                    && String.valueOf(oldItem.getImageUrl()).equals(String.valueOf(newItem.getImageUrl()));
        }
    }
}
