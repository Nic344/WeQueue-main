package com.example.queueapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.queueapp.api.model.LoginResponse;
import com.example.queueapp.data.Resource;
import com.example.queueapp.data.repository.AuthRepository;

/** ViewModel for authentication (login, register, logout). */
public class AuthViewModel extends ViewModel {

    private final AuthRepository repository = new AuthRepository();
    private final MutableLiveData<Resource<LoginResponse>> authResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Object>> logoutResult = new MutableLiveData<>();

    public LiveData<Resource<LoginResponse>> getAuthResult() {
        return authResult;
    }

    public LiveData<Resource<Object>> getLogoutResult() {
        return logoutResult;
    }

    public void login(String email, String password) {
        repository.login(email, password, authResult);
    }

    public void register(String name, String email, String password) {
        repository.register(name, email, password, authResult);
    }

    public void logout() {
        repository.logout(logoutResult);
    }
}
