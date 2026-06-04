package com.example.queueapp.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StaffAllQueuesResponse {

    @SerializedName("queues")
    private List<StaffQueueItem> queues;

    public List<StaffQueueItem> getQueues() {
        return queues;
    }
}
