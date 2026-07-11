package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CheckinRequest {

    @SerializedName("device_id")
    private final String deviceId;

    @SerializedName("latitude")
    private final double latitude;

    @SerializedName("longitude")
    private final double longitude;

    @SerializedName("senha")
    private final String senha;

    @SerializedName("timestamp")
    private final String timestamp;

    @SerializedName("turno_id")
    private final String turnoId;

    @SerializedName("cliente_checkin_id")
    private final String clienteCheckinId;

    public CheckinRequest(String deviceId, double latitude, double longitude,
                          String senha, String timestamp, String turnoId,
                          String clienteCheckinId) {
        this.deviceId = deviceId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.senha = senha;
        this.timestamp = timestamp;
        this.turnoId = turnoId;
        this.clienteCheckinId = clienteCheckinId;
    }
}
