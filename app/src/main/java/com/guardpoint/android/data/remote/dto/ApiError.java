package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ApiError {

    @SerializedName("error")
    private String error;

    private String message;

    @SerializedName("status")
    private String status;

    public ApiError() {
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDisplayMessage() {
        if (error != null && !error.isEmpty()) {
            return error;
        }
        if (message != null && !message.isEmpty()) {
            return message;
        }
        return null;
    }
}
