package com.example.queueapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.queueapp.api.model.QueueHistoryListResponse;
import com.example.queueapp.data.Resource;
import com.example.queueapp.data.repository.QueueRepository;

public class HistoryViewModel extends ViewModel {

    private final QueueRepository repository = new QueueRepository();
    private final MutableLiveData<Resource<QueueHistoryListResponse>> history = new MutableLiveData<>();

    public LiveData<Resource<QueueHistoryListResponse>> getHistory() {
        return history;
    }

    public void loadHistory() {
        repository.getHistory(history);
    }
}
