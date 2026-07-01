package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class SabotagemRequest {

    @SerializedName("turno_id")
    private String turnoId;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("motivo")
    private String motivo;

    @SerializedName("timestamp")
    private String timestamp;

    public SabotagemRequest() {
    }

    public SabotagemRequest(String turnoId, double latitude, double longitude, String motivo, String timestamp) {
        this.turnoId = turnoId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.motivo = motivo;
        this.timestamp = timestamp;
    }

    public String getTurnoId() {
        return turnoId;
    }

    public void setTurnoId(String turnoId) {
        this.turnoId = turnoId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
