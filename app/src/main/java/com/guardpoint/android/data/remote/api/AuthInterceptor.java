package com.guardpoint.android.data.remote.api;

import androidx.annotation.NonNull;

import com.guardpoint.android.data.local.prefs.SecurePrefs;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final SecurePrefs securePrefs;

    public AuthInterceptor(SecurePrefs securePrefs) {
        this.securePrefs = securePrefs;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String token = securePrefs.getAccessToken();
        if (token == null) return chain.proceed(chain.request());

        Request request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();
        Response response = chain.proceed(request);

        if (response.code() == 401) {
            try {
                okhttp3.ResponseBody peekBody = response.peekBody(Long.MAX_VALUE);
                String bodyString = peekBody.string();
                if (bodyString.contains("token expirado")) {
                    securePrefs.clear();
                }
            } catch (Exception e) {
                securePrefs.clear();
            }
        }

        return response;
    }
}
