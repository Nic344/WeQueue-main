package com.example.queueapp.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.queueapp.api.ApiErrorHelper;
import com.example.queueapp.api.model.ApiResponse;
import com.example.queueapp.api.model.FoodIdRequest;
import com.example.queueapp.api.model.FoodListResponse;
import com.example.queueapp.api.model.FoodModel;
import com.example.queueapp.data.Resource;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for food data — the single source of truth between the Retrofit
 * API and the ViewModels. The UI never calls the API directly.
 */
public class FoodRepository extends BaseRepository {

    /** Loads all foods (admin) and pushes the list into {@code target}. */
    public void fetchFoods(MutableLiveData<Resource<List<FoodModel>>> target) {
        target.setValue(Resource.loading());
        api.getFoodList().enqueue(new Callback<ApiResponse<FoodListResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<FoodListResponse>> call,
                                   @NonNull Response<ApiResponse<FoodListResponse>> response) {
                ApiResponse<FoodListResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()
                        && body.getData() != null) {
                    target.postValue(Resource.success(body.getData().getFoods()));
                } else {
                    target.postValue(Resource.error(
                            ApiErrorHelper.getMessage(response, "Failed to load foods")));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<FoodListResponse>> call, @NonNull Throwable t) {
                target.postValue(Resource.error(ApiErrorHelper.getNetworkMessage(t, "Network error")));
            }
        });
    }

    /** Creates or updates a food. */
    public void saveFood(FoodModel food, boolean isEdit, MutableLiveData<Resource<FoodModel>> target) {
        if (isEdit) {
            enqueue(api.updateFood(food), target, "Failed to save food");
        } else {
            enqueue(api.createFood(food), target, "Failed to save food");
        }
    }

    public void deleteFood(int foodId, MutableLiveData<Resource<Object>> target) {
        enqueue(api.deleteFood(new FoodIdRequest(foodId)), target, "Failed to delete food");
    }

    public void getPopularFoods(MutableLiveData<Resource<FoodListResponse>> target) {
        enqueue(api.getPopularFoods(), target, "Failed to load foods");
    }

    public void searchFoods(String query, MutableLiveData<Resource<FoodListResponse>> target) {
        enqueue(api.searchFoods(query), target, "Failed to search foods");
    }
}
