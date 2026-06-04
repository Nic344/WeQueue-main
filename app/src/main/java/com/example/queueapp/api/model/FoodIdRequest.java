package com.example.queueapp.api.model;

import com.google.gson.annotations.SerializedName;

public class FoodIdRequest {

    @SerializedName("food_id")
    private final int foodId;

    public FoodIdRequest(int foodId) {
        this.foodId = foodId;
    }
}
