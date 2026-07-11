package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TurnoListResponse {

    @SerializedName("data")
    private List<TurnoResponse> data;

    @SerializedName("total")
    private int total;

    public TurnoListResponse() {
    }

    public List<TurnoResponse> getData() { return data; }
    public int getTotal() { return total; }
}
