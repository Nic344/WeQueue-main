package com.example.queueapp.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.queueapp.api.model.UserModel;
import com.google.gson.Gson;

public final class SessionManager {

    private static final String PREFS_NAME = "wequeue_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_JSON = "user_json";

    private static SessionManager instance;

    private SharedPreferences prefs;
    private final Gson gson = new Gson();

    private SessionManager() {
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void init(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, UserModel user) {
        if (prefs == null) {
            return;
        }
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_JSON, gson.toJson(user))
                .apply();
    }

    public String getToken() {
        if (prefs == null) {
            return null;
        }
        return prefs.getString(KEY_TOKEN, null);
    }

    public UserModel getUser() {
        if (prefs == null) {
            return null;
        }
        String json = prefs.getString(KEY_USER_JSON, null);
        if (json == null || json.isEmpty()) {
            return null;
        }
        return gson.fromJson(json, UserModel.class);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }

    public void clearSession() {
        if (prefs == null) {
            return;
        }
        prefs.edit().clear().apply();
    }
}
