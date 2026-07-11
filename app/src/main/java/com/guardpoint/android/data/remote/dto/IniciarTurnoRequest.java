package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class IniciarTurnoRequest {

    @SerializedName("device_id")
    private final String deviceId;

    @SerializedName("latitude")
    private final double latitude;

    @SerializedName("longitude")
    private final double longitude;

    @SerializedName("posto_id")
    private final String postoId;

    @SerializedName("senha")
    private final String senha;

    @SerializedName("intervalo_min")
    private final Integer intervaloMin;

    public IniciarTurnoRequest(String deviceId, double latitude, double longitude,
                               String postoId, String senha, Integer intervaloMin) {
        this.deviceId = deviceId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.postoId = postoId;
        this.senha = senha;
        this.intervaloMin = intervaloMin;
    }
}
