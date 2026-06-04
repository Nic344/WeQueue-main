package com.example.queueapp.api.model;

import com.google.gson.annotations.SerializedName;

public class FavoriteModel {

    @SerializedName("id")
    private int id;

    @SerializedName("favorite_id")
    private int favoriteId;

    @SerializedName("food_id")
    private int foodId;

    @SerializedName("food")
    private FoodModel food;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private double price;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("category")
    private String category;

    public int getId() {
        return id > 0 ? id : foodId;
    }

    public int getFavoriteId() {
        return favoriteId;
    }

    public FoodModel getFood() {
        return food;
    }

    public String getName() {
        return food != null && food.getName() != null ? food.getName() : name;
    }

    public String getDescription() {
        return food != null && food.getDescription() != null ? food.getDescription() : description;
    }

    public double getPrice() {
        return food != null && food.getPrice() > 0 ? food.getPrice() : price;
    }

    public String getImageUrl() {
        return food != null && food.getImageUrl() != null ? food.getImageUrl() : imageUrl;
    }

    public String getCategory() {
        return food != null && food.getCategory() != null ? food.getCategory() : category;
    }
}
