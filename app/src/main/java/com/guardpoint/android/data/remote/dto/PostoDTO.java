package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class PostoDTO {

    @SerializedName("id")
    private String id;

    @SerializedName("nome")
    private String nome;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("raio_m")
    private int raioM;

    @SerializedName("ativo")
    private boolean ativo;

    public PostoDTO() {
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getRaioM() { return raioM; }
    public boolean isAtivo() { return ativo; }
}
