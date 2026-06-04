package com.example.queueapp.api.model;

import com.google.gson.annotations.SerializedName;

public class StaffQueueItem {

    @SerializedName("id")
    private int id;

    @SerializedName("queue_number")
    private String queueNumber;

    @SerializedName("customer_name")
    private String customerName;

    @SerializedName("customer_email")
    private String customerEmail;

    @SerializedName("food_id")
    private Integer foodId;

    @SerializedName("food_name")
    private String foodName;

    @SerializedName("food_image")
    private String foodImage;

    @SerializedName("status")
    private String status;

    @SerializedName("served_at")
    private String servedAt;

    @SerializedName("completed_at")
    private String completedAt;

    @SerializedName("cancelled_at")
    private String cancelledAt;

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

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public Integer getFoodId() {
        return foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public String getFoodImage() {
        return foodImage;
    }

    public String getStatus() {
        return status;
    }

    public String getServedAt() {
        return servedAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public String getCancelledAt() {
        return cancelledAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
