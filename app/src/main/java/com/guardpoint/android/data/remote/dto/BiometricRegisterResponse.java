package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class BiometricRegisterResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("device_id")
    private String deviceId;

    @SerializedName("device_secret")
    private String deviceSecret;

    @SerializedName("usuario_id")
    private String usuarioId;

    @SerializedName("empresa_id")
    private String empresaId;

    @SerializedName("criado_em")
    private String criadoEm;

    public String getDeviceSecret() { return deviceSecret; }
    public String getDeviceId() { return deviceId; }
    public String getEmpresaId() { return empresaId; }
}
