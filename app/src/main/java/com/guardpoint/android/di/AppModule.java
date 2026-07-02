package com.guardpoint.android.di;

import android.content.Context;

import com.guardpoint.android.data.local.prefs.SecurePrefs;
import com.guardpoint.android.data.repository.AuthRepositoryImpl;
import com.guardpoint.android.data.repository.CheckinRepositoryImpl;
import com.guardpoint.android.data.repository.TurnoRepositoryImpl;
import com.guardpoint.android.domain.repository.AuthRepository;
import com.guardpoint.android.domain.repository.CheckinRepository;
import com.guardpoint.android.domain.repository.TurnoRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

import com.guardpoint.android.util.NetworkMonitor;
import com.guardpoint.android.util.NotificationHelper;
import com.guardpoint.android.util.ServiceStateManager;

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
    public CheckinRepository provideCheckinRepository(CheckinRepositoryImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    public ServiceStateManager provideServiceStateManager() {
        return new ServiceStateManager();
    }

    @Provides
    @Singleton
    public NotificationHelper provideNotificationHelper(@ApplicationContext Context context) {
        return new NotificationHelper(context);
    }

    @Provides
    @Singleton
    public NetworkMonitor provideNetworkMonitor(@ApplicationContext Context context) {
        return new NetworkMonitor(context);
    }
}
