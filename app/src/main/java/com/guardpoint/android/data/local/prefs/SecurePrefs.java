package com.guardpoint.android.data.local.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.guardpoint.android.util.Constants;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SecurePrefs {

    private final SharedPreferences prefs;

    public SecurePrefs(Context context) {
        String masterKeyAlias;
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            prefs = EncryptedSharedPreferences.create(
                    Constants.SHARED_PREFS_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to initialize EncryptedSharedPreferences", e);
        }
    }

    public void saveAccessToken(String token) {
        prefs.edit().putString(Constants.KEY_JWT_TOKEN, token).apply();
    }

    public String getAccessToken() {
        return prefs.getString(Constants.KEY_JWT_TOKEN, null);
    }

    public void saveRefreshToken(String token) {
        prefs.edit().putString(Constants.KEY_REFRESH_TOKEN, token).apply();
    }

    public String getRefreshToken() {
        return prefs.getString(Constants.KEY_REFRESH_TOKEN, null);
    }

    public void saveUserId(String userId) {
        prefs.edit().putString(Constants.KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return prefs.getString(Constants.KEY_USER_ID, null);
    }

    public void saveCompanyId(String companyId) {
        prefs.edit().putString(Constants.KEY_COMPANY_ID, companyId).apply();
    }

    public String getCompanyId() {
        return prefs.getString(Constants.KEY_COMPANY_ID, null);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
