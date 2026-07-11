package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class UsuarioResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("empresa_id")
    private String empresaId;

    @SerializedName("nome")
    private String nome;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    public UsuarioResponse() {
    }

    public String getId() { return id; }
    public String getEmpresaId() { return empresaId; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
