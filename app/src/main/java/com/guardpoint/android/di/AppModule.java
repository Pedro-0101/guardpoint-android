package com.guardpoint.android.di;

import android.content.Context;

import com.guardpoint.android.data.local.prefs.SecurePrefs;

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
}
