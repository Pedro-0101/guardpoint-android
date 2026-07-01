package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class FinalizarTurnoRequest {

    @SerializedName("turno_id")
    private String turnoId;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("timestamp")
    private String timestamp;

    public FinalizarTurnoRequest() {
    }

    public FinalizarTurnoRequest(String turnoId, double latitude, double longitude, String timestamp) {
        this.turnoId = turnoId;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
