package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("senha")
    private String senha;

    @SerializedName("codigo_empresa")
    private String codigoEmpresa;

    @SerializedName("nome")
    private String nome;

    public LoginRequest(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    public LoginRequest(String codigoEmpresa, String nome, String senha) {
        this.codigoEmpresa = codigoEmpresa;
        this.nome = nome;
        this.senha = senha;
    }
}
