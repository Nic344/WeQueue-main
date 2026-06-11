package com.example.queueapp.api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.queueapp.LoginActivity;
import com.example.queueapp.data.AppSession;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Detects expired/invalid sessions globally. When the backend rejects a request
 * with HTTP 401, the local session is cleared and the user is sent back to the
 * login screen, instead of being stuck on a screen with a failing "Retry".
 */
public class SessionExpiryInterceptor implements Interceptor {

    private final Context appContext;

    /** Prevents a burst of parallel 401s from launching login multiple times. */
    private static volatile boolean handlingExpiry = false;

    public SessionExpiryInterceptor(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        if (response.code() == 401 && !isAuthEndpoint(request)) {
            handleExpiredSession();
        }
        return response;
    }

    /** Login/register also return 401 (wrong credentials); those are not expiry. */
    private boolean isAuthEndpoint(Request request) {
        String path = request.url().encodedPath();
        return path.contains("auth/login") || path.contains("auth/register");
    }

    private void handleExpiredSession() {
        if (handlingExpiry) {
            return;
        }
        handlingExpiry = true;

        AppSession.getInstance().resetSession();

        new Handler(Looper.getMainLooper()).post(() -> {
            Intent intent = new Intent(appContext, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(LoginActivity.EXTRA_SESSION_EXPIRED, true);
            appContext.startActivity(intent);
            handlingExpiry = false;
        });
    }
}
