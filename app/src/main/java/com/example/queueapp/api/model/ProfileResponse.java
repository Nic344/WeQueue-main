package com.example.queueapp.api.model;

import com.google.gson.annotations.SerializedName;

public class ProfileResponse {

    @SerializedName("user")
    private UserModel user;

    public UserModel getUser() {
        return user;
    }
}
