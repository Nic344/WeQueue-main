package com.example.queueapp.model;

public class QueueHistory {
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_SERVING = "serving";
    public static final String STATUS_WAITING = "waiting";

    private final String date;
    private final String queueNumber;
    private final String foodName;
    private final String status;

    public QueueHistory(String date, String queueNumber, String foodName, String status) {
        this.date = date;
        this.queueNumber = queueNumber;
        this.foodName = foodName;
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public String getQueueNumber() {
        return queueNumber;
    }

    public String getFoodName() {
        return foodName;
    }

    public String getStatus() {
        return status;
    }
}
