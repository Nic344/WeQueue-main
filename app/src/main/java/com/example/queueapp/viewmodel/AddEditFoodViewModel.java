package com.example.queueapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.queueapp.api.model.FoodModel;
import com.example.queueapp.api.model.UploadResponse;
import com.example.queueapp.data.Resource;
import com.example.queueapp.data.repository.FoodRepository;
import com.example.queueapp.data.repository.UserRepository;

import okhttp3.MultipartBody;

public class AddEditFoodViewModel extends ViewModel {

    private final FoodRepository foodRepository = new FoodRepository();
    private final UserRepository userRepository = new UserRepository();

    private final MutableLiveData<Resource<FoodModel>> saveResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<UploadResponse>> uploadResult = new MutableLiveData<>();

    public LiveData<Resource<FoodModel>> getSaveResult() {
        return saveResult;
    }

    public LiveData<Resource<UploadResponse>> getUploadResult() {
        return uploadResult;
    }

    public void saveFood(FoodModel food, boolean isEdit) {
        foodRepository.saveFood(food, isEdit, saveResult);
    }

    public void uploadImage(MultipartBody.Part part) {
        userRepository.uploadImage(part, uploadResult);
    }
}
