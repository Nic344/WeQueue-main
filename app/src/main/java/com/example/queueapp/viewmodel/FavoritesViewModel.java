package com.example.queueapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.queueapp.api.model.FavoriteListResponse;
import com.example.queueapp.data.Resource;
import com.example.queueapp.data.repository.FavoriteRepository;

public class FavoritesViewModel extends ViewModel {

    private final FavoriteRepository repository = new FavoriteRepository();

    private final MutableLiveData<Resource<FavoriteListResponse>> favorites = new MutableLiveData<>();
    private final MutableLiveData<Resource<Object>> removeResult = new MutableLiveData<>();

    public LiveData<Resource<FavoriteListResponse>> getFavorites() {
        return favorites;
    }

    public LiveData<Resource<Object>> getRemoveResult() {
        return removeResult;
    }

    public void loadFavorites() {
        repository.getFavorites(favorites);
    }

    public void removeFavorite(int foodId) {
        repository.removeFavorite(foodId, removeResult);
    }
}
