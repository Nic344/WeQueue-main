package com.example.queueapp.backend.queue;

import com.example.queueapp.model.QueueTicket;

public class QueueStateSnapshot {

    private final String nowServing;
    private final String userQueue;
    private final int remainingPeople;
    private final int estimatedWaitMinutes;
    private final QueueTicket activeTicket;
    private final boolean hasActiveTicket;
    private final String activeQueueId;

    public QueueStateSnapshot(String nowServing, String userQueue, int remainingPeople,
                              int estimatedWaitMinutes, QueueTicket activeTicket,
                              boolean hasActiveTicket, String activeQueueId) {
        this.nowServing = nowServing;
        this.userQueue = userQueue;
        this.remainingPeople = remainingPeople;
        this.estimatedWaitMinutes = estimatedWaitMinutes;
        this.activeTicket = activeTicket;
        this.hasActiveTicket = hasActiveTicket;
        this.activeQueueId = activeQueueId;
    }

    public QueueStateSnapshot(String nowServing, String userQueue, int remainingPeople,
                              int estimatedWaitMinutes, QueueTicket activeTicket,
                              boolean hasActiveTicket) {
        this(nowServing, userQueue, remainingPeople, estimatedWaitMinutes,
                activeTicket, hasActiveTicket, null);
    }

    public String getNowServing() {
        return nowServing;
    }

    public String getUserQueue() {
        return userQueue;
    }

    public int getRemainingPeople() {
        return remainingPeople;
    }

    public int getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public QueueTicket getActiveTicket() {
        return activeTicket;
    }

    public boolean hasActiveTicket() {
        return hasActiveTicket;
    }

    public String getActiveQueueId() {
        return activeQueueId;
    }
}
