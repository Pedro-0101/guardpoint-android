package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoteCheckinRequest {

    @SerializedName("checkins")
    private List<CheckinRequest> checkins;

    public LoteCheckinRequest() {
    }

    public LoteCheckinRequest(List<CheckinRequest> checkins) {
        this.checkins = checkins;
    }

    public List<CheckinRequest> getCheckins() {
        return checkins;
    }

    public void setCheckins(List<CheckinRequest> checkins) {
        this.checkins = checkins;
    }
}
