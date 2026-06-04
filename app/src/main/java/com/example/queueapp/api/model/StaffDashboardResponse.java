package com.example.queueapp.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StaffDashboardResponse {

    @SerializedName("now_serving")
    private StaffQueueItem nowServing;

    @SerializedName("waiting_count")
    private int waitingCount;

    @SerializedName("completed_today")
    private int completedToday;

    @SerializedName("cancelled_today")
    private int cancelledToday;

    @SerializedName("waiting_list")
    private List<StaffQueueItem> waitingList;

    public StaffQueueItem getNowServing() {
        return nowServing;
    }

    public int getWaitingCount() {
        return waitingCount;
    }

    public int getCompletedToday() {
        return completedToday;
    }

    public int getCancelledToday() {
        return cancelledToday;
    }

    public List<StaffQueueItem> getWaitingList() {
        return waitingList;
    }
}
