package com.example.queueapp.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.queueapp.api.ApiConfig;
import com.example.queueapp.api.ApiErrorHelper;
import com.example.queueapp.api.ApiService;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.data.Resource;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseRepository {

    protected final ApiService api = ApiConfig.getApiService();

    protected <T> void enqueue(Call<ApiResponse<T>> call,
                               MutableLiveData<Resource<T>> target,
                               String fallbackError) {
        target.setValue(Resource.loading());
        call.enqueue(new Callback<ApiResponse<T>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<T>> c,
                                   @NonNull Response<ApiResponse<T>> response) {
                ApiResponse<T> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    target.postValue(Resource.success(body.getData()));
                } else {
                    target.postValue(Resource.error(
                            ApiErrorHelper.getMessage(response, fallbackError)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<T>> c, @NonNull Throwable t) {
                target.postValue(Resource.error(
                        ApiErrorHelper.getNetworkMessage(t, "Network error")));
            }
        });
    }
}
