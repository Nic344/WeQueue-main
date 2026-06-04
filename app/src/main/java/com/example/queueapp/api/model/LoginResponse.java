package com.example.queueapp.api.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("token")
    private String token;

    @SerializedName("user")
    private UserModel user;

    public String getToken() {
        return token;
    }

    public UserModel getUser() {
        return user;
    }
}
