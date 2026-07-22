package com.guardpoint.android.util;

import android.os.Build;

public enum AppEnvironment {
    DEVELOPMENT,
    PRODUCTION;

    private static final AppEnvironment current = detect();

    public static AppEnvironment current() {
        return current;
    }

    private static AppEnvironment detect() {
        if (isEmulator()) {
            return DEVELOPMENT;
        }
        return PRODUCTION;
    }

    private static boolean isEmulator() {
        String fingerprint = Build.FINGERPRINT;
        String model = Build.MODEL;
        String manufacturer = Build.MANUFACTURER;
        String product = Build.PRODUCT;
        String hardware = Build.HARDWARE;

        return fingerprint.startsWith("generic")
                || fingerprint.startsWith("unknown")
                || model.contains("google_sdk")
                || model.contains("Emulator")
                || model.contains("Android SDK built for x86")
                || manufacturer.contains("Genymotion")
                || product.contains("sdk")
                || product.contains("vbox")
                || hardware.equals("vbox86");
    }
}
