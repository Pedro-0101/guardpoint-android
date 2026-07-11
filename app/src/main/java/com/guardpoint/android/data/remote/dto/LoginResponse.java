package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("expires_in")
    private long expiresIn;

    @SerializedName("usuario")
    private UsuarioResponse usuario;

    public LoginResponse() {
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public long getExpiresIn() { return expiresIn; }
    public UsuarioResponse getUsuario() { return usuario; }
}
