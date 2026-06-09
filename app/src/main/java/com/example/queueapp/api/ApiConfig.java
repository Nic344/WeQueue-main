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
     * Emulator: 10.0.2.2 = localhost PC (jangan pakai 127.0.0.1 — itu device emulator sendiri).
     * XAMPP: copy API ke htdocs/webabiq/ → http://localhost/webabiq/
     * HP fisik: ganti dengan IP LAN PC, e.g. http://192.168.1.10/webabiq/
     */
    public static final String BASE_URL = "http://10.0.2.2/webabiq/";

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
                    .retryOnConnectionFailure(true)
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
