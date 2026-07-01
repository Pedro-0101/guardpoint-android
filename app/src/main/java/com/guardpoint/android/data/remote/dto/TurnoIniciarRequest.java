package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class TurnoIniciarRequest {

    @SerializedName("posto_id")
    private String postoId;

    @SerializedName("device_id")
    private String deviceId;

    public TurnoIniciarRequest() {
    }

    public TurnoIniciarRequest(String postoId, String deviceId) {
        this.postoId = postoId;
        this.deviceId = deviceId;
    }

    public String getPostoId() {
        return postoId;
    }

    public void setPostoId(String postoId) {
        this.postoId = postoId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
