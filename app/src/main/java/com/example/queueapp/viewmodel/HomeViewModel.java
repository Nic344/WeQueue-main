package com.example.queueapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.queueapp.api.model.FavoriteListResponse;
import com.example.queueapp.api.model.FoodListResponse;
import com.example.queueapp.api.model.MyQueueResponse;
import com.example.queueapp.api.model.QueueStatusResponse;
import com.example.queueapp.data.Resource;
import com.example.queueapp.data.repository.FavoriteRepository;
import com.example.queueapp.data.repository.FoodRepository;
import com.example.queueapp.data.repository.QueueRepository;

public class HomeViewModel extends ViewModel {

    private final QueueRepository queueRepository = new QueueRepository();
    private final FoodRepository foodRepository = new FoodRepository();
    private final FavoriteRepository favoriteRepository = new FavoriteRepository();

    private final MutableLiveData<Resource<QueueStatusResponse>> queueStatus = new MutableLiveData<>();
    private final MutableLiveData<Resource<MyQueueResponse>> myQueue = new MutableLiveData<>();
    private final MutableLiveData<Resource<FavoriteListResponse>> favorites = new MutableLiveData<>();
    private final MutableLiveData<Resource<FoodListResponse>> popularFoods = new MutableLiveData<>();
    private final MutableLiveData<Resource<Object>> favoriteToggle = new MutableLiveData<>();

    public LiveData<Resource<QueueStatusResponse>> getQueueStatus() {
        return queueStatus;
    }

    public LiveData<Resource<MyQueueResponse>> getMyQueue() {
        return myQueue;
    }

    public LiveData<Resource<FavoriteListResponse>> getFavorites() {
        return favorites;
    }

    public LiveData<Resource<FoodListResponse>> getPopularFoods() {
        return popularFoods;
    }

    public LiveData<Resource<Object>> getFavoriteToggle() {
        return favoriteToggle;
    }

    public void loadQueueStatus() {
        queueRepository.getStatus(queueStatus);
    }

    public void loadMyQueue() {
        queueRepository.fetchMyQueue(myQueue);
    }

    public void loadFavorites() {
        favoriteRepository.getFavorites(favorites);
    }

    public void loadPopularFoods() {
        foodRepository.getPopularFoods(popularFoods);
    }

    public void toggleFavorite(int foodId, boolean currentlyFavorite) {
        if (currentlyFavorite) {
            favoriteRepository.removeFavorite(foodId, favoriteToggle);
        } else {
            favoriteRepository.addFavorite(foodId, favoriteToggle);
        }
    }
}
