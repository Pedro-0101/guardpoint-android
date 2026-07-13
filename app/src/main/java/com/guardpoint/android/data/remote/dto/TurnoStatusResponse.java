package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class TurnoStatusResponse {

    @SerializedName("turno")
    private TurnoResponse turno;

    @SerializedName("proximo_deadline")
    private String proximoDeadline;

    @SerializedName("checkins_hoje")
    private int checkinsHoje;

    @SerializedName("ultimo_checkin")
    private CheckinDTO ultimoCheckin;

    @SerializedName("tipo_proximo_deadline")
    private String tipoProximoDeadline;

    public TurnoStatusResponse() {
    }

    public TurnoResponse getTurno() { return turno; }
    public String getProximoDeadline() { return proximoDeadline; }
    public int getCheckinsHoje() { return checkinsHoje; }
    public CheckinDTO getUltimoCheckin() { return ultimoCheckin; }
    public String getTipoProximoDeadline() { return tipoProximoDeadline; }
}
