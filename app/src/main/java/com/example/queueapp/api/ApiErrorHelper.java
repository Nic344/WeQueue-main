package com.example.queueapp.api;

import com.example.queueapp.api.model.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;

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

            }
        }
        return fallback;
    }

    public static boolean isTransientError(Throwable t) {
        if (t == null) {
            return false;
        }
        String message = t.getMessage();
        if (message == null) {
            return t instanceof IOException;
        }
        String lower = message.toLowerCase(Locale.US);
        return lower.contains("unexpected end of stream")
                || lower.contains("connection reset")
                || lower.contains("connection refused")
                || lower.contains("failed to connect")
                || lower.contains("timeout")
                || lower.contains("broken pipe")
                || t instanceof SocketTimeoutException;
    }

    public static String getNetworkMessage(Throwable t, String fallback) {
        if (t == null) {
            return fallback;
        }
        if (t instanceof JsonSyntaxException) {
            return "Server mengembalikan respons tidak valid. Pastikan folder API di XAMPP sudah terbaru (webabiq).";
        }
        if (t instanceof UnknownHostException) {
            return "Tidak bisa terhubung ke server. Periksa BASE_URL di ApiConfig dan pastikan XAMPP Apache aktif.";
        }
        if (t instanceof SocketTimeoutException) {
            return "Koneksi ke server timeout. Coba lagi.";
        }
        if (t instanceof IOException) {
            String message = t.getMessage();
            if (message != null && !message.isEmpty()) {
                return message;
            }
        }
        String message = t.getMessage();
        return message != null && !message.isEmpty() ? message : fallback;
    }
}
