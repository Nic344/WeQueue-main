package com.example.queueapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.queueapp.api.model.FavoriteListResponse;
import com.example.queueapp.api.model.FoodIdRequest;
import com.example.queueapp.data.Resource;

/** Repository for favorites: list, add, remove. */
public class FavoriteRepository extends BaseRepository {

    public void getFavorites(MutableLiveData<Resource<FavoriteListResponse>> target) {
        enqueue(api.getFavorites(), target, "Failed to load favorites");
    }

    public void addFavorite(int foodId, MutableLiveData<Resource<Object>> target) {
        enqueue(api.addFavorite(new FoodIdRequest(foodId)), target, "Failed to add favorite");
    }

    public void removeFavorite(int foodId, MutableLiveData<Resource<Object>> target) {
        enqueue(api.removeFavorite(new FoodIdRequest(foodId)), target, "Failed to remove favorite");
    }
}
