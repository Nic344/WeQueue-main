package com.example.queueapp.staff.model;

public class StaffQueueEntry {

    private final String id;
    private final String queueNumber;
    private final String customerName;
    private final int waitingMinutes;
    private final String status;
    private final boolean vip;
    private final boolean priority;
    private final String userId;

    public StaffQueueEntry(String id, String queueNumber, String customerName,
                           int waitingMinutes, String status, boolean vip,
                           boolean priority, String userId) {
        this.id = id;
        this.queueNumber = queueNumber;
        this.customerName = customerName;
        this.waitingMinutes = waitingMinutes;
        this.status = status;
        this.vip = vip;
        this.priority = priority;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public String getQueueNumber() {
        return queueNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public int getWaitingMinutes() {
        return waitingMinutes;
    }

    public String getStatus() {
        return status;
    }

    public boolean isVip() {
        return vip;
    }

    public boolean isPriority() {
        return priority;
    }

    public String getUserId() {
        return userId;
    }
}
