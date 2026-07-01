package com.guardpoint.android.data.remote.api;

import androidx.annotation.NonNull;

import com.guardpoint.android.data.local.prefs.SecurePrefs;
import com.guardpoint.android.data.remote.dto.RefreshRequest;
import com.guardpoint.android.data.remote.dto.RefreshResponse;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;
import retrofit2.Retrofit;

@Singleton
public class AuthInterceptor implements Interceptor {

    private final SecurePrefs securePrefs;

    @Inject
    public AuthInterceptor(SecurePrefs securePrefs) {
        this.securePrefs = securePrefs;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String token = securePrefs.getAccessToken();

        if (token == null) {
            return chain.proceed(chain.request());
        }

        Request request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(request);
    }

    @Singleton
    public static class TokenAuthenticator implements Authenticator {

        private final SecurePrefs securePrefs;
        private final Lazy<Retrofit> retrofit;

        @Inject
        public TokenAuthenticator(SecurePrefs securePrefs, Lazy<Retrofit> retrofit) {
            this.securePrefs = securePrefs;
            this.retrofit = retrofit;
        }

        @Override
        public Request authenticate(Route route, @NonNull Response response) throws IOException {
            if (responseCount(response) >= 2) {
                return null;
            }

            String refreshToken = securePrefs.getRefreshToken();
            if (refreshToken == null || refreshToken.isEmpty()) {
                return null;
            }

            GuardPointApi api = retrofit.get().create(GuardPointApi.class);
            Call<RefreshResponse> call = api.refreshToken(new RefreshRequest(refreshToken));
            retrofit2.Response<RefreshResponse> refreshResponse = call.execute();

            if (!refreshResponse.isSuccessful() || refreshResponse.body() == null) {
                securePrefs.clear();
                return null;
            }

            RefreshResponse body = refreshResponse.body();
            securePrefs.saveAccessToken(body.getAccessToken());
            if (body.getRefreshToken() != null) {
                securePrefs.saveRefreshToken(body.getRefreshToken());
            }

            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + body.getAccessToken())
                    .build();
        }

        private int responseCount(Response response) {
            int count = 1;
            while ((response = response.priorResponse()) != null) {
                count++;
            }
            return count;
        }
    }
}
