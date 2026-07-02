package com.guardpoint.android.di;

import android.content.Context;

import androidx.room.Room;

import com.guardpoint.android.data.local.db.AppDatabase;
import com.guardpoint.android.data.local.db.dao.CheckinDao;
import com.guardpoint.android.data.local.db.dao.TurnoDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                "guardpoint.db"
        ).fallbackToDestructiveMigration()
        .build();
    }

    @Provides
    public CheckinDao provideCheckinDao(AppDatabase database) {
        return database.checkinDao();
    }

    @Provides
    public TurnoDao provideTurnoDao(AppDatabase database) {
        return database.turnoDao();
    }
}
