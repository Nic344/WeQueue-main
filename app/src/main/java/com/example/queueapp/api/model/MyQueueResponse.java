package com.example.queueapp.api.model;

import com.google.gson.annotations.SerializedName;

public class MyQueueResponse {

    @SerializedName("has_active_queue")
    private boolean hasActiveQueue;

    @SerializedName("queue")
    private QueueModel queue;

    public boolean isHasActiveQueue() {
        return hasActiveQueue;
    }

    public QueueModel getQueue() {
        return queue;
    }
}
