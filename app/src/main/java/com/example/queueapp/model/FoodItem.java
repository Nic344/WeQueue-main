package com.example.queueapp.model;

import androidx.annotation.Nullable;

public class FoodItem {
    private final int id;
    private final String name;
    private final String description;
    private final int price;
    private final String category;
    private final String budget;
    private final String spicyLevel;
    private final int placeholderColorRes;
    private final int iconRes;
    @Nullable
    private final String imageAssetName;

    public FoodItem(int id, String name, String description, int price, String category,
                    String budget, String spicyLevel, int placeholderColorRes, int iconRes,
                    @Nullable String imageAssetName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.budget = budget;
        this.spicyLevel = spicyLevel;
        this.placeholderColorRes = placeholderColorRes;
        this.iconRes = iconRes;
        this.imageAssetName = imageAssetName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getBudget() {
        return budget;
    }

    public String getSpicyLevel() {
        return spicyLevel;
    }

    public int getPlaceholderColorRes() {
        return placeholderColorRes;
    }

    public int getIconRes() {
        return iconRes;
    }

    @Nullable
    public String getImageAssetName() {
        return imageAssetName;
    }
}
