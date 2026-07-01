package com.guardpoint.android.di;

import com.guardpoint.android.data.remote.api.AuthInterceptor;
import com.guardpoint.android.data.remote.api.GuardPointApi;
import com.guardpoint.android.util.Constants;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(
            AuthInterceptor authInterceptor,
            AuthInterceptor.TokenAuthenticator tokenAuthenticator) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(authInterceptor);
        builder.authenticator(tokenAuthenticator);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggingInterceptor);

        return builder.build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public GuardPointApi provideGuardPointApi(Retrofit retrofit) {
        return retrofit.create(GuardPointApi.class);
    }
}
