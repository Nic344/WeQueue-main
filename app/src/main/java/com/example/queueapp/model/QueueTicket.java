package com.example.queueapp.model;

public class QueueTicket {
    private final String queueNumber;
    private final int position;
    private final int estimatedMinutes;
    private final String nowServing;

    public QueueTicket(String queueNumber, int position, int estimatedMinutes, String nowServing) {
        this.queueNumber = queueNumber;
        this.position = position;
        this.estimatedMinutes = estimatedMinutes;
        this.nowServing = nowServing;
    }

    public String getQueueNumber() {
        return queueNumber;
    }

    public int getPosition() {
        return position;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public String getNowServing() {
        return nowServing;
    }
}
