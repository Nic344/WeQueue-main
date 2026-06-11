package com.example.queueapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.queueapp.api.model.StaffAllQueuesResponse;
import com.example.queueapp.api.model.StaffDashboardResponse;
import com.example.queueapp.api.model.StaffQueueItem;
import com.example.queueapp.data.Resource;

import com.google.gson.JsonObject;

/** Repository for staff operations: dashboard + queue management. */
public class StaffRepository extends BaseRepository {

    public void getDashboard(MutableLiveData<Resource<StaffDashboardResponse>> target) {
        enqueue(api.getStaffDashboard(), target, "Failed to load dashboard");
    }

    public void callNext(MutableLiveData<Resource<StaffQueueItem>> target) {
        enqueue(api.callNext(), target, "Failed to call next");
    }

    /** Completes the currently serving queue (no specific id). */
    public void completeServing(MutableLiveData<Resource<StaffQueueItem>> target) {
        enqueue(api.completeQueue(new JsonObject()), target, "No serving queue found");
    }

    public void getAllQueues(String status, MutableLiveData<Resource<StaffAllQueuesResponse>> target) {
        enqueue(api.getAllQueues(status, "", 1, 50), target, "Failed to load queues");
    }

    public void skipQueue(int queueId, MutableLiveData<Resource<StaffQueueItem>> target) {
        enqueue(api.skipQueue(queueBody(queueId)), target, "Skip failed");
    }

    public void cancelQueue(int queueId, MutableLiveData<Resource<StaffQueueItem>> target) {
        enqueue(api.staffCancelQueue(queueBody(queueId)), target, "Cancel failed");
    }

    public void completeQueue(int queueId, MutableLiveData<Resource<StaffQueueItem>> target) {
        enqueue(api.completeQueue(queueBody(queueId)), target, "Complete failed");
    }

    private JsonObject queueBody(int queueId) {
        JsonObject body = new JsonObject();
        body.addProperty("queue_id", queueId);
        return body;
    }
}
