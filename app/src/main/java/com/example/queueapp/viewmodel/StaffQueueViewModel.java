package com.example.queueapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.queueapp.api.model.StaffAllQueuesResponse;
import com.example.queueapp.api.model.StaffQueueItem;
import com.example.queueapp.data.Resource;
import com.example.queueapp.data.repository.StaffRepository;

public class StaffQueueViewModel extends ViewModel {

    private final StaffRepository repository = new StaffRepository();

    private final MutableLiveData<Resource<StaffAllQueuesResponse>> queues = new MutableLiveData<>();
    private final MutableLiveData<Resource<StaffQueueItem>> actionResult = new MutableLiveData<>();

    public LiveData<Resource<StaffAllQueuesResponse>> getQueues() {
        return queues;
    }

    public LiveData<Resource<StaffQueueItem>> getActionResult() {
        return actionResult;
    }

    public void loadQueues(String status) {
        repository.getAllQueues(status, queues);
    }

    public void skip(int queueId) {
        repository.skipQueue(queueId, actionResult);
    }

    public void cancel(int queueId) {
        repository.cancelQueue(queueId, actionResult);
    }

    public void complete(int queueId) {
        repository.completeQueue(queueId, actionResult);
    }
}
