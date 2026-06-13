package com.example.queueapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.queueapp.api.model.FoodModel;
import com.example.queueapp.data.Resource;
import com.example.queueapp.data.repository.FoodRepository;

import java.util.List;

public class AdminFoodsViewModel extends ViewModel {

    private final FoodRepository repository = new FoodRepository();

    private final MutableLiveData<Resource<List<FoodModel>>> foods = new MutableLiveData<>();
    private final MutableLiveData<Resource<Object>> deleteResult = new MutableLiveData<>();

    public LiveData<Resource<List<FoodModel>>> getFoods() {
        return foods;
    }

    public LiveData<Resource<Object>> getDeleteResult() {
        return deleteResult;
    }

    public void loadFoodsIfNeeded() {
        if (foods.getValue() == null) {
            refresh();
        }
    }

    public void refresh() {
        repository.fetchFoods(foods);
    }

    public void deleteFood(FoodModel food) {
        repository.deleteFood(food.getId(), deleteResult);
    }
}
