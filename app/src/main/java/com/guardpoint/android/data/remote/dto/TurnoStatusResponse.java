package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class TurnoStatusResponse {

    @SerializedName("turno")
    private TurnoResponse turno;

    public TurnoStatusResponse() {
    }

    public TurnoResponse getTurno() { return turno; }
}
