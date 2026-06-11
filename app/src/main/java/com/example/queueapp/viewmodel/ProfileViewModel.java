package com.example.queueapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.queueapp.api.model.ProfileResponse;
import com.example.queueapp.api.model.UploadResponse;
import com.example.queueapp.data.Resource;
import com.example.queueapp.data.repository.AuthRepository;
import com.example.queueapp.data.repository.UserRepository;

import okhttp3.MultipartBody;

/** ViewModel for the Profile screen (load/update/change password/upload/logout). */
public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository = new UserRepository();
    private final AuthRepository authRepository = new AuthRepository();

    private final MutableLiveData<Resource<ProfileResponse>> profile = new MutableLiveData<>();
    private final MutableLiveData<Resource<ProfileResponse>> updateResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Object>> passwordResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<UploadResponse>> uploadResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Object>> logoutResult = new MutableLiveData<>();

    public LiveData<Resource<ProfileResponse>> getProfile() {
        return profile;
    }

    public LiveData<Resource<ProfileResponse>> getUpdateResult() {
        return updateResult;
    }

    public LiveData<Resource<Object>> getPasswordResult() {
        return passwordResult;
    }

    public LiveData<Resource<UploadResponse>> getUploadResult() {
        return uploadResult;
    }

    public LiveData<Resource<Object>> getLogoutResult() {
        return logoutResult;
    }

    public void loadProfile() {
        userRepository.getProfile(profile);
    }

    public void updateProfile(String name, String email, String profilePicture) {
        userRepository.updateProfile(name, email, profilePicture, updateResult);
    }

    public void changePassword(String current, String newPass) {
        userRepository.changePassword(current, newPass, passwordResult);
    }

    public void uploadImage(MultipartBody.Part part) {
        userRepository.uploadImage(part, uploadResult);
    }

    public void logout() {
        authRepository.logout(logoutResult);
    }
}
