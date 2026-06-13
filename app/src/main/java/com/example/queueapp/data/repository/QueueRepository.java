package com.example.queueapp.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.queueapp.api.ApiErrorHelper;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.MyQueueResponse;
import com.example.queueapp.api.model.QueueHistoryListResponse;
import com.example.queueapp.api.model.QueueModel;
import com.example.queueapp.api.model.QueueStatusResponse;
import com.example.queueapp.api.model.TakeQueueRequest;
import com.example.queueapp.data.Resource;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QueueRepository extends BaseRepository {

    public void fetchMyQueue(MutableLiveData<Resource<MyQueueResponse>> target) {
        target.setValue(Resource.loading());
        api.getMyQueue().enqueue(new Callback<ApiResponse<MyQueueResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<MyQueueResponse>> call,
                                   @NonNull Response<ApiResponse<MyQueueResponse>> response) {
                ApiResponse<MyQueueResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.getData() != null) {
                    target.postValue(Resource.success(body.getData()));
                } else {
                    target.postValue(Resource.error(
                            ApiErrorHelper.getMessage(response, "Failed to load your queue")));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<MyQueueResponse>> call, @NonNull Throwable t) {
                target.postValue(Resource.error(ApiErrorHelper.getNetworkMessage(t, "Network error")));
            }
        });
    }

    public void takeQueue(MutableLiveData<Resource<QueueModel>> target) {
        enqueue(api.takeQueue(new TakeQueueRequest(null)), target, "Failed to take queue");
    }

    public void cancelQueue(MutableLiveData<Resource<QueueModel>> target) {
        enqueue(api.cancelQueue(), target, "Failed to cancel queue");
    }

    public void getStatus(MutableLiveData<Resource<QueueStatusResponse>> target) {
        enqueue(api.getQueueStatus(), target, "Failed to load queue status");
    }

    public void getHistory(MutableLiveData<Resource<QueueHistoryListResponse>> target) {
        enqueue(api.getQueueHistory(), target, "Failed to load history");
    }
}
