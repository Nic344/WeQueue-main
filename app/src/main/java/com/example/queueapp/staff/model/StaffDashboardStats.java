package com.example.queueapp.staff.model;

public class StaffDashboardStats {

    private final int activeQueue;
    private final int customersToday;
    private final int averageWaitMinutes;
    private final int completedOrders;
    private final String nowServing;

    public StaffDashboardStats(int activeQueue, int customersToday, int averageWaitMinutes,
                               int completedOrders, String nowServing) {
        this.activeQueue = activeQueue;
        this.customersToday = customersToday;
        this.averageWaitMinutes = averageWaitMinutes;
        this.completedOrders = completedOrders;
        this.nowServing = nowServing;
    }

    public int getActiveQueue() {
        return activeQueue;
    }

    public int getCustomersToday() {
        return customersToday;
    }

    public int getAverageWaitMinutes() {
        return averageWaitMinutes;
    }

    public int getCompletedOrders() {
        return completedOrders;
    }

    public String getNowServing() {
        return nowServing;
    }
}
