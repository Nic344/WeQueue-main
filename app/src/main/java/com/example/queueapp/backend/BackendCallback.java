package com.example.queueapp.backend;

public interface BackendCallback<T> {
    void onSuccess(T result);

    void onError(String message);
}
