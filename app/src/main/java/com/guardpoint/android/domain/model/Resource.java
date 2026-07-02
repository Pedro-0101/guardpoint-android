package com.guardpoint.android.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Resource<T> {

    @NonNull
    private final Status status;

    @Nullable
    private final T data;

    @Nullable
    private final String message;

    private Resource(@NonNull Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null);
    }

    public static <T> Resource<T> success(@NonNull T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(@NonNull String message) {
        return new Resource<>(Status.ERROR, null, message);
    }

    public static <T> Resource<T> error(@NonNull String message, @Nullable T data) {
        return new Resource<>(Status.ERROR, data, message);
    }

    public static <T> Resource<T> offlineSaved() {
        return new Resource<>(Status.OFFLINE_SAVED, null, null);
    }

    @NonNull
    public Status getStatus() {
        return status;
    }

    @Nullable
    public T getData() {
        return data;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }

    public boolean isOfflineSaved() {
        return status == Status.OFFLINE_SAVED;
    }

    public enum Status {
        LOADING,
        SUCCESS,
        ERROR,
        OFFLINE_SAVED
    }
}
