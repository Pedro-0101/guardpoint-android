package com.guardpoint.android.util;

public class Constants {
    private static final String URL_LOCAL_NETWORK = "http://172.28.32.1:8080/api/v1/";
    private static final String URL_LOCALHOST = "http://10.0.2.2:8080/api/v1/";

    public static final String BASE_URL = AppEnvironment.current() == AppEnvironment.DEVELOPMENT
            ? URL_LOCALHOST
            : URL_LOCAL_NETWORK;
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
}
