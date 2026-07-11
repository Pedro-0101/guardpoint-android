package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("senha")
    private String senha;

    public LoginRequest(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }
}
