package com.guardpoint.android;

import com.guardpoint.android.util.ThemeManager;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class GuardPointApp extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.apply(this);
    }
}
