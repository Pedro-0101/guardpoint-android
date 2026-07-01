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

    public String getTurnoId() {
        return turnoId;
    }

    public void setTurnoId(String turnoId) {
        this.turnoId = turnoId;
    }

    public String getPostoId() {
        return postoId;
    }

    public void setPostoId(String postoId) {
        this.postoId = postoId;
    }

    public String getPostoNome() {
        return postoNome;
    }

    public void setPostoNome(String postoNome) {
        this.postoNome = postoNome;
    }

    public int getIntervaloMinutos() {
        return intervaloMinutos;
    }

    public void setIntervaloMinutos(int intervaloMinutos) {
        this.intervaloMinutos = intervaloMinutos;
    }

    public String getTokenSessao() {
        return tokenSessao;
    }

    public void setTokenSessao(String tokenSessao) {
        this.tokenSessao = tokenSessao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
