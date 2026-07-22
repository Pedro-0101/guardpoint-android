package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class VigiaProximoTurno {

    @SerializedName("posto")
    private PostoDTO posto;

    @SerializedName("inicio_previsto")
    private String inicioPrevisto;

    @SerializedName("fim_previsto")
    private String fimPrevisto;

    @SerializedName("data")
    private String data;

    @SerializedName("hora_inicio")
    private String horaInicio;

    @SerializedName("hora_fim")
    private String horaFim;

    public VigiaProximoTurno() {
    }

    public PostoDTO getPosto() { return posto; }
    public String getInicioPrevisto() { return inicioPrevisto; }
    public String getFimPrevisto() { return fimPrevisto; }
    public String getData() { return data; }
    public String getHoraInicio() { return horaInicio; }
    public String getHoraFim() { return horaFim; }
}
