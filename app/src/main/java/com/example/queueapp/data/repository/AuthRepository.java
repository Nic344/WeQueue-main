package com.example.queueapp.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.queueapp.api.model.LoginRequest;
import com.example.queueapp.api.model.LoginResponse;
import com.example.queueapp.api.model.RegisterRequest;
import com.example.queueapp.data.Resource;

public class AuthRepository extends BaseRepository {

    public void login(String email, String password,
                      MutableLiveData<Resource<LoginResponse>> target) {
        enqueue(api.login(new LoginRequest(email, password)), target, "Login failed");
    }

    public void register(String name, String email, String password,
                         MutableLiveData<Resource<LoginResponse>> target) {
        enqueue(api.register(new RegisterRequest(name, email, password)), target, "Registration failed");
    }

    public void logout(MutableLiveData<Resource<Object>> target) {
        enqueue(api.logout(), target, "Logout failed");
    }
}
