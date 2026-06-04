package com.example.queueapp.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FoodListResponse {

    @SerializedName("foods")
    private List<FoodModel> foods;

    public List<FoodModel> getFoods() {
        return foods;
    }
}
