package com.example.queueapp.api.model;

import com.google.gson.annotations.SerializedName;

public class QueueModel {

    @SerializedName("id")
    private int id;

    @SerializedName("queue_number")
    private String queueNumber;

    @SerializedName("status")
    private String status;

    @SerializedName("position")
    private int position;

    @SerializedName("estimated_wait")
    private int estimatedWait;

    @SerializedName("estimated_wait_minutes")
    private int estimatedWaitMinutes;

    @SerializedName("food_id")
    private Integer foodId;

    @SerializedName("food_name")
    private String foodName;

    @SerializedName("food")
    private FoodModel food;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public int getId() {
        return id;
    }

    public String getQueueNumber() {
        return queueNumber;
    }

    public String getStatus() {
        return status;
    }

    public int getPosition() {
        return position;
    }

    public int getEstimatedWait() {
        return estimatedWait > 0 ? estimatedWait : estimatedWaitMinutes;
    }

    public Integer getFoodId() {
        return foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public FoodModel getFood() {
        return food;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
