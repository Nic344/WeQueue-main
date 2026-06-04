package com.example.queueapp.api;

import android.content.Context;

import com.example.queueapp.data.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiConfig {

    /**
     * Emulator: 10.0.2.2 = host machine localhost.
     * Deploy folder wequeue-api as http://localhost/foodqueue/api/ on XAMPP.
     * Physical device: replace with your PC LAN IP, e.g. http://192.168.1.10/foodqueue/api/
     */
    public static final String BASE_URL = "http://10.0.2.2/wequeue-api/";

    private static ApiService apiService;

    private ApiConfig() {
    }

    public static void init(Context context) {
        SessionManager.getInstance().init(context.getApplicationContext());
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor())
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
