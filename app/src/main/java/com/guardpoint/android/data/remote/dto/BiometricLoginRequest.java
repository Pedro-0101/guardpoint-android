package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class BiometricLoginRequest {

    @SerializedName("device_id")
    private final String deviceId;

    @SerializedName("device_secret")
    private final String deviceSecret;

    @SerializedName("empresa_id")
    private final String empresaId;

    public BiometricLoginRequest(String deviceId, String deviceSecret, String empresaId) {
        this.deviceId = deviceId;
        this.deviceSecret = deviceSecret;
        this.empresaId = empresaId;
    }
}
