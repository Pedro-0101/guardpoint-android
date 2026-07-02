package com.guardpoint.android.util;

public final class Constants {

    private Constants() {
    }

    public static final String BASE_URL = "https://api.guardpoint.com/";

    public static final String SHARED_PREFS_NAME = "guardpoint_secure_prefs";

    public static final String KEY_JWT_TOKEN = "jwt_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_COMPANY_ID = "company_id";
    public static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";

    public static final String CHANNEL_SERVICE = "guardpoint_service";
    public static final String CHANNEL_ALERTS = "guardpoint_alerts";
    public static final int NOTIFICATION_ID_SERVICE = 1001;
    public static final int NOTIFICATION_ID_ALERT = 2001;

    public static final int LOCATION_INTERVAL_MS = 30000;
    public static final int LOCATION_FASTEST_INTERVAL_MS = 15000;

    public static final String WORK_TAG_SYNC = "sync";

    public static final int MAX_RETRY_SYNC = 5;

    public static final String ACTION_CHECKIN_ALARM = "com.guardpoint.android.ACTION_CHECKIN_ALARM";
    public static final String EXTRA_TURNO_ID = "extra_turno_id";
    public static final String EXTRA_POSTO_NOME = "extra_posto_nome";
    public static final String EXTRA_INTERVALO_MINUTOS = "extra_intervalo_minutos";
    public static final String EXTRA_ULTIMO_CHECKIN = "extra_ultimo_checkin";

    public static final int ALARM_THRESHOLD_MINUTES = 5;
    public static final int ALARM_REQUEST_CODE = 3001;
}
