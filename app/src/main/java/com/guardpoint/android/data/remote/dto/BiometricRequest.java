package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class BiometricRequest {

    @SerializedName("empresa_id")
    private String empresaId;

    @SerializedName("device_id")
    private String deviceId;

    public BiometricRequest() {
    }

    public BiometricRequest(String empresaId, String deviceId) {
        this.empresaId = empresaId;
        this.deviceId = deviceId;
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(String empresaId) {
        this.empresaId = empresaId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
