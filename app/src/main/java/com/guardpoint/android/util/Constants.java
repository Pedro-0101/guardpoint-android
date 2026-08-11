package com.guardpoint.android.util;

public class Constants {
    private static final String URL_LOCALHOST = "http://10.0.2.2:8080/api/v1/";
    private static final String URL_PRODUCTION = "https://guardpoint-server-production.up.railway.app/api/v1/";

    public static final String BASE_URL = AppEnvironment.current() == AppEnvironment.DEVELOPMENT
            ? URL_LOCALHOST
            : URL_PRODUCTION;
    public static final String SHARED_PREFS_NAME = "guardpoint_secure_prefs";
    public static final String KEY_JWT_TOKEN = "jwt_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_COMPANY_ID = "company_id";
    public static final String KEY_USER_NOME = "user_nome";
    public static final String KEY_USER_ROLE = "user_role";
    public static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    public static final String KEY_DEVICE_SECRET = "device_secret";
    public static final String KEY_POSTO_NOME = "posto_nome";
    public static final String KEY_LAST_ACTIVITY_MILLIS = "last_activity_millis";
    public static final long SESSION_INACTIVITY_TIMEOUT_MILLIS = 5 * 60 * 1000;
}
