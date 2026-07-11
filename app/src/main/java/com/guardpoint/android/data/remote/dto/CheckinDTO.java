package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CheckinDTO {

    @SerializedName("id")
    private String id;

    @SerializedName("cliente_checkin_id")
    private String clienteCheckinId;

    @SerializedName("evento")
    private String evento;

    @SerializedName("timestamp_criacao")
    private String timestampCriacao;

    @SerializedName("tipo_senha")
    private String tipoSenha;

    @SerializedName("turno_id")
    private String turnoId;

    public CheckinDTO() {
    }

    public String getId() { return id; }
    public String getClienteCheckinId() { return clienteCheckinId; }
    public String getEvento() { return evento; }
    public String getTimestampCriacao() { return timestampCriacao; }
    public String getTipoSenha() { return tipoSenha; }
    public String getTurnoId() { return turnoId; }
}
