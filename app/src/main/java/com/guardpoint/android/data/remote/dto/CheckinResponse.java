package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CheckinResponse {

    @SerializedName("atrasado")
    private boolean atrasado;

    @SerializedName("checkin")
    private CheckinDTO checkin;

    @SerializedName("posto_nome")
    private String postNome;

    @SerializedName("proximo_deadline")
    private String proximoDeadline;

    @SerializedName("status")
    private String status;

    public CheckinResponse() {
    }

    public boolean isAtrasado() { return atrasado; }
    public CheckinDTO getCheckin() { return checkin; }
    public String getPostNome() { return postNome; }
    public String getProximoDeadline() { return proximoDeadline; }
    public String getStatus() { return status; }
}
