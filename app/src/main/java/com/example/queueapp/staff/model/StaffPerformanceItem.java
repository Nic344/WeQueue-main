package com.example.queueapp.staff.model;

public class StaffPerformanceItem {

    private final String name;
    private final int completedQueues;
    private final int avgServiceMinutes;
    private final int ranking;

    public StaffPerformanceItem(String name, int completedQueues, int avgServiceMinutes, int ranking) {
        this.name = name;
        this.completedQueues = completedQueues;
        this.avgServiceMinutes = avgServiceMinutes;
        this.ranking = ranking;
    }

    public String getName() {
        return name;
    }

    public int getCompletedQueues() {
        return completedQueues;
    }

    public int getAvgServiceMinutes() {
        return avgServiceMinutes;
    }

    public int getRanking() {
        return ranking;
    }
}
