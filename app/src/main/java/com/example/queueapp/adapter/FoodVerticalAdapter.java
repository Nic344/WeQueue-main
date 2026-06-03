package com.example.queueapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.queueapp.R;
import com.example.queueapp.data.AppSession;
import com.example.queueapp.model.FoodItem;
import com.example.queueapp.util.FoodImageHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodVerticalAdapter extends RecyclerView.Adapter<FoodVerticalAdapter.ViewHolder> {

    public interface OnFoodActionListener {
        void onFavoriteToggled(FoodItem item, boolean isFavorite);
    }

    private final List<FoodItem> items = new ArrayList<>();
    private final OnFoodActionListener listener;

    public FoodVerticalAdapter(OnFoodActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<FoodItem> foods) {
        items.clear();
        items.addAll(foods);
        notifyDataSetChanged();
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

        void bind(FoodItem item) {
            foodName.setText(item.getName());
            foodDescription.setText(item.getDescription());
            foodPrice.setText(String.format(Locale.getDefault(), "Rp %,d", item.getPrice()));
            FoodImageHelper.loadFoodImage(itemView.getContext(), foodIcon, imageContainer, item);
            updateFavoriteIcon(item.getId());

            btnFavorite.setOnClickListener(v -> {
                boolean isFavorite = AppSession.getInstance().toggleFavorite(item.getId());
                updateFavoriteIcon(item.getId());
                if (listener != null) {
                    listener.onFavoriteToggled(item, isFavorite);
                }
            });
        }

        private void updateFavoriteIcon(int foodId) {
            boolean fav = AppSession.getInstance().isFavorite(foodId);
            btnFavorite.setImageResource(fav ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
        }
    }
}
