package com.guardpoint.android;

import com.guardpoint.android.util.FileLoggingTree;
import com.guardpoint.android.util.ThemeManager;

import timber.log.Timber;

import dagger.hilt.android.HiltAndroidApp;
import timber.log.Timber;

@HiltAndroidApp
public class GuardPointApp extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.apply(this);
        boolean isDebuggable = (getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        if (isDebuggable) {
            Timber.plant(new Timber.DebugTree());
        }
        Timber.plant(new FileLoggingTree(this));
    }
}
