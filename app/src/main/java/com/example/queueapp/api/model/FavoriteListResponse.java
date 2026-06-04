package com.example.queueapp.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FavoriteListResponse {

    @SerializedName("favorites")
    private List<FavoriteModel> favorites;

    public List<FavoriteModel> getFavorites() {
        return favorites;
    }
}
