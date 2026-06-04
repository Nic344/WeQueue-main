package com.example.queueapp.api;

import com.example.queueapp.api.model.ApiResponse;
import com.google.gson.Gson;

import retrofit2.Response;

public final class ApiErrorHelper {

    private static final Gson GSON = new Gson();

    private ApiErrorHelper() {
    }

    public static String getMessage(Response<?> response, String fallback) {
        if (response.body() instanceof ApiResponse) {
            ApiResponse<?> body = (ApiResponse<?>) response.body();
            if (body.getMessage() != null && !body.getMessage().isEmpty()) {
                return body.getMessage();
            }
        }
        if (response.errorBody() != null) {
            try {
                ApiResponse<?> error = GSON.fromJson(response.errorBody().string(), ApiResponse.class);
                if (error != null && error.getMessage() != null && !error.getMessage().isEmpty()) {
                    return error.getMessage();
                }
            } catch (Exception ignored) {
                // fall through
            }
        }
        return fallback;
    }
}
