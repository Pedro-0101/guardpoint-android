package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class BiometricRegisterRequest {

    @SerializedName("device_id")
    private final String deviceId;

    public BiometricRegisterRequest(String deviceId) {
        this.deviceId = deviceId;
    }
}
