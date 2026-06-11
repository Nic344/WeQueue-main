package com.example.queueapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.queueapp.api.model.StaffDashboardResponse;
import com.example.queueapp.api.model.StaffQueueItem;
import com.example.queueapp.data.Resource;
import com.example.queueapp.data.repository.StaffRepository;

/** ViewModel for the staff Dashboard screen. */
public class StaffDashboardViewModel extends ViewModel {

    private final StaffRepository repository = new StaffRepository();

    private final MutableLiveData<Resource<StaffDashboardResponse>> dashboard = new MutableLiveData<>();
    private final MutableLiveData<Resource<StaffQueueItem>> callNextResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<StaffQueueItem>> completeResult = new MutableLiveData<>();

    public LiveData<Resource<StaffDashboardResponse>> getDashboard() {
        return dashboard;
    }

    public LiveData<Resource<StaffQueueItem>> getCallNextResult() {
        return callNextResult;
    }

    public LiveData<Resource<StaffQueueItem>> getCompleteResult() {
        return completeResult;
    }

    public void loadDashboard() {
        repository.getDashboard(dashboard);
    }

    public void callNext() {
        repository.callNext(callNextResult);
    }

    public void completeServing() {
        repository.completeServing(completeResult);
    }
}
