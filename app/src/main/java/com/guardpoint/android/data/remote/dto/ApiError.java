package com.guardpoint.android.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ApiError {

    @SerializedName("error")
    private String error;

    private String message;

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
