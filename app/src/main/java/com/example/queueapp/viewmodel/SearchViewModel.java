package com.example.queueapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.queueapp.api.model.FoodListResponse;
import com.example.queueapp.data.Resource;
import com.example.queueapp.data.repository.FoodRepository;

public class SearchViewModel extends ViewModel {

    private final FoodRepository repository = new FoodRepository();
    private final MutableLiveData<Resource<FoodListResponse>> results = new MutableLiveData<>();

    public LiveData<Resource<FoodListResponse>> getResults() {
        return results;
    }

    public void search(String query) {
        repository.searchFoods(query, results);
    }
}
