package com.example.queueapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.queueapp.api.model.MyQueueResponse;
import com.example.queueapp.api.model.QueueModel;
import com.example.queueapp.data.Resource;
import com.example.queueapp.data.repository.QueueRepository;

public class QueueViewModel extends ViewModel {

    private final QueueRepository repository = new QueueRepository();

    private final MutableLiveData<Resource<MyQueueResponse>> myQueue = new MutableLiveData<>();
    private final MutableLiveData<Resource<QueueModel>> takeResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<QueueModel>> cancelResult = new MutableLiveData<>();

    public LiveData<Resource<MyQueueResponse>> getMyQueue() {
        return myQueue;
    }

    public LiveData<Resource<QueueModel>> getTakeResult() {
        return takeResult;
    }

    public LiveData<Resource<QueueModel>> getCancelResult() {
        return cancelResult;
    }

    public void loadMyQueue() {
        repository.fetchMyQueue(myQueue);
    }

    public void takeQueue() {
        repository.takeQueue(takeResult);
    }

    public void cancelQueue() {
        repository.cancelQueue(cancelResult);
    }
}
