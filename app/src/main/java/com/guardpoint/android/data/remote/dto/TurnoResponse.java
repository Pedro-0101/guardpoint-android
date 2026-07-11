package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class TurnoResponse {

    @SerializedName("id")
    private String turnoId;

    @SerializedName("posto_id")
    private String postoId;

    @SerializedName("posto_nome")
    private String postoNome;

    @SerializedName("intervalo_min")
    private int intervaloMinutos;

    @SerializedName("token_sessao")
    private String tokenSessao;

    @SerializedName("status")
    private String status;

    public TurnoResponse() {
    }

    public String getTurnoId() { return turnoId; }
    public String getPostoId() { return postoId; }
    public String getPostoNome() { return postoNome; }
    public int getIntervaloMinutos() { return intervaloMinutos; }
    public String getTokenSessao() { return tokenSessao; }
    public String getStatus() { return status; }
}
