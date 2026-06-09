package com.example.queueapp.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserListResponse {

    @SerializedName("users")
    private List<UserModel> users;

    @SerializedName("count")
    private int count;

    public List<UserModel> getUsers() {
        return users;
    }

    public int getCount() {
        return count;
    }
}
