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
import com.example.queueapp.api.model.FavoriteModel;
import com.example.queueapp.util.FoodImageHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    public interface OnRemoveClickListener {
        void onRemoveClick(FavoriteModel favorite, int position);
    }

    private final List<FavoriteModel> items = new ArrayList<>();
    private final OnRemoveClickListener listener;

    public FavoriteAdapter(OnRemoveClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<FavoriteModel> favorites) {
        List<FavoriteModel> safeFavorites = favorites != null ? favorites : new ArrayList<>();
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new FavoriteDiffCallback(items, safeFavorites));
        items.clear();
        items.addAll(safeFavorites);
        diff.dispatchUpdatesTo(this);
    }

    public void removeAt(int position) {
        if (position < 0 || position >= items.size()) {
            return;
        }
        items.remove(position);
        notifyItemRemoved(position);
    }

    public boolean isEmpty() {
        return items.isEmpty();
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

    class ViewHolder extends RecyclerView.ViewHolder {
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

        void bind(FavoriteModel item) {
            foodName.setText(item.getName());
            foodDescription.setText(item.getDescription());
            foodPrice.setText(String.format(Locale.getDefault(), "Rp %,.0f", item.getPrice()));
            btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
            FoodImageHelper.loadApiFoodImage(itemView.getContext(), foodIcon, imageContainer,
                    item.getImageUrl(), item.getName());
            btnFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveClick(item, getBindingAdapterPosition());
                }
            });
        }
    }

    private static class FavoriteDiffCallback extends DiffUtil.Callback {
        private final List<FavoriteModel> oldItems;
        private final List<FavoriteModel> newItems;

        FavoriteDiffCallback(List<FavoriteModel> oldItems, List<FavoriteModel> newItems) {
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
            FavoriteModel oldItem = oldItems.get(oldItemPosition);
            FavoriteModel newItem = newItems.get(newItemPosition);
            return String.valueOf(oldItem.getName()).equals(String.valueOf(newItem.getName()))
                    && String.valueOf(oldItem.getDescription()).equals(String.valueOf(newItem.getDescription()))
                    && oldItem.getPrice() == newItem.getPrice()
                    && String.valueOf(oldItem.getImageUrl()).equals(String.valueOf(newItem.getImageUrl()));
        }
    }
}
