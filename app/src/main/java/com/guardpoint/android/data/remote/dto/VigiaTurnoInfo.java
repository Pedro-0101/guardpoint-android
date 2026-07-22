package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class VigiaTurnoInfo {

    @SerializedName("id")
    private String id;

    @SerializedName("status")
    private String status;

    @SerializedName("posto")
    private PostoDTO posto;

    @SerializedName("posto_nome")
    private String postoNome;

    @SerializedName("token_sessao")
    private String tokenSessao;

    @SerializedName("inicio_previsto")
    private String inicioPrevisto;

    @SerializedName("fim_previsto")
    private String fimPrevisto;

    @SerializedName("inicio_real")
    private String inicioReal;

    @SerializedName("intervalo_min")
    private int intervaloMin;

    @SerializedName("proximo_deadline")
    private String proximoDeadline;

    @SerializedName("tipo_proximo_deadline")
    private String tipoProximoDeadline;

    @SerializedName("atrasado")
    private boolean atrasado;

    @SerializedName("checkins_hoje")
    private int checkinsHoje;

    @SerializedName("ultimo_checkin")
    private CheckinDTO ultimoCheckin;

    public VigiaTurnoInfo() {
    }

    public String getId() { return id; }
    public String getStatus() { return status; }
    public PostoDTO getPosto() { return posto; }
    public String getPostoNome() { return postoNome; }
    public String getTokenSessao() { return tokenSessao; }
    public String getInicioPrevisto() { return inicioPrevisto; }
    public String getFimPrevisto() { return fimPrevisto; }
    public String getInicioReal() { return inicioReal; }
    public int getIntervaloMin() { return intervaloMin; }
    public String getProximoDeadline() { return proximoDeadline; }
    public String getTipoProximoDeadline() { return tipoProximoDeadline; }
    public boolean isAtrasado() { return atrasado; }
    public int getCheckinsHoje() { return checkinsHoje; }
    public CheckinDTO getUltimoCheckin() { return ultimoCheckin; }
}
