package com.example.queueapp.api;

import androidx.annotation.NonNull;

import com.example.queueapp.data.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        String token = SessionManager.getInstance().getToken();
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }
        builder.header("Accept", "application/json");
        builder.header("Content-Type", "application/json");
        return chain.proceed(builder.build());
    }
}
