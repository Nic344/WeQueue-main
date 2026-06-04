package com.example.queueapp.api.model;

import com.google.gson.annotations.SerializedName;

public class TakeQueueRequest {

    @SerializedName("food_id")
    private final Integer foodId;

    public TakeQueueRequest(Integer foodId) {
        this.foodId = foodId;
    }
}
