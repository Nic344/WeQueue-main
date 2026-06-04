package com.example.queueapp.api.model;

import com.google.gson.annotations.SerializedName;

public class StaffStatsResponse {

    @SerializedName("total_queues")
    private int totalQueues;

    @SerializedName("completed_count")
    private int completedCount;

    @SerializedName("cancelled_count")
    private int cancelledCount;

    @SerializedName("currently_serving")
    private StaffQueueItem currentlyServing;

    @SerializedName("average_serve_time")
    private double averageServeTime;

    @SerializedName("peak_hour")
    private String peakHour;

    public int getTotalQueues() {
        return totalQueues;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public int getCancelledCount() {
        return cancelledCount;
    }

    public StaffQueueItem getCurrentlyServing() {
        return currentlyServing;
    }

    public double getAverageServeTime() {
        return averageServeTime;
    }

    public String getPeakHour() {
        return peakHour;
    }
}
