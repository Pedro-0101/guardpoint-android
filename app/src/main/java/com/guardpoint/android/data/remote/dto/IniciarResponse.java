package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class IniciarResponse {

    @SerializedName("atrasado")
    private boolean atrasado;

    @SerializedName("proximo_deadline")
    private String proximoDeadline;

    @SerializedName("tipo_proximo_deadline")
    private String tipoProximoDeadline;

    @SerializedName("turno")
    private TurnoResponse turno;

    public IniciarResponse() {
    }

    public boolean isAtrasado() { return atrasado; }
    public String getProximoDeadline() { return proximoDeadline; }
    public String getTipoProximoDeadline() { return tipoProximoDeadline; }
    public TurnoResponse getTurno() { return turno; }
}
