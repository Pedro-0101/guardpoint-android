package com.guardpoint.android.di;

import android.content.Context;

import com.guardpoint.android.data.local.prefs.SecurePrefs;
import com.guardpoint.android.data.remote.api.AuthInterceptor;
import com.guardpoint.android.data.remote.api.GuardPointApi;
import com.guardpoint.android.data.repository.AuthRepositoryImpl;
import com.guardpoint.android.data.repository.TurnoRepositoryImpl;
import com.guardpoint.android.domain.repository.AuthRepository;
import com.guardpoint.android.domain.repository.TurnoRepository;
import com.guardpoint.android.util.NetworkMonitor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public SecurePrefs provideSecurePrefs(@ApplicationContext Context context) {
        return new SecurePrefs(context);
    }

    @Provides
    @Singleton
    public AuthInterceptor provideAuthInterceptor(SecurePrefs securePrefs) {
        return new AuthInterceptor(securePrefs);
    }

    @Provides
    @Singleton
    public AuthRepository provideAuthRepository(AuthRepositoryImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    public TurnoRepository provideTurnoRepository(TurnoRepositoryImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    public NetworkMonitor provideNetworkMonitor(@ApplicationContext Context context) {
        return new NetworkMonitor(context);
    }
}
