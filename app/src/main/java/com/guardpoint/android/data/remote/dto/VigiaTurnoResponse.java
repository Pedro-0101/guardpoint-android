package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class VigiaTurnoResponse {

    @SerializedName("tem_turno_ativo")
    private boolean temTurnoAtivo;

    @SerializedName("mensagem")
    private String mensagem;

    @SerializedName("turno")
    private VigiaTurnoInfo turno;

    @SerializedName("proximo_turno")
    private VigiaProximoTurno proximoTurno;

    public VigiaTurnoResponse() {
    }

    public boolean isTemTurnoAtivo() { return temTurnoAtivo; }
    public String getMensagem() { return mensagem; }
    public VigiaTurnoInfo getTurno() { return turno; }
    public VigiaProximoTurno getProximoTurno() { return proximoTurno; }
}
