package com.guardpoint.android;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.guardpoint.android.data.local.prefs.SecurePrefs;
import com.guardpoint.android.util.FileLoggingTree;
import com.guardpoint.android.util.ThemeManager;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import timber.log.Timber;

@HiltAndroidApp
public class GuardPointApp extends Application {

    @Inject
    SecurePrefs securePrefs;

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.apply(this);
        boolean isDebuggable = (getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        if (isDebuggable) {
            Timber.plant(new Timber.DebugTree());
        }
        Timber.plant(new FileLoggingTree(this));

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                securePrefs.saveLastActivityMillis(System.currentTimeMillis());
            }

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}
            @Override
            public void onActivityStarted(@NonNull Activity activity) {}
            @Override
            public void onActivityPaused(@NonNull Activity activity) {}
            @Override
            public void onActivityStopped(@NonNull Activity activity) {}
            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {}
        });
    }
}
