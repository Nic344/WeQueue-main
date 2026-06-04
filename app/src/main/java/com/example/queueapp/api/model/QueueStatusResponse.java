package com.example.queueapp.api.model;

import com.google.gson.annotations.SerializedName;

public class QueueStatusResponse {

    @SerializedName("now_serving")
    private String nowServing;

    @SerializedName("total_waiting")
    private int totalWaiting;

    @SerializedName("remaining")
    private int remaining;

    @SerializedName("your_queue")
    private String yourQueue;

    @SerializedName("estimated_wait_per_person")
    private int estimatedWaitPerPerson;

    @SerializedName("estimated_wait_minutes")
    private int estimatedWaitMinutes;

    public String getNowServing() {
        return nowServing;
    }

    public int getTotalWaiting() {
        return totalWaiting;
    }

    public int getRemaining() {
        return remaining;
    }

    public String getYourQueue() {
        return yourQueue;
    }

    public int getEstimatedWaitPerPerson() {
        return estimatedWaitPerPerson;
    }

    public int getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }
}
