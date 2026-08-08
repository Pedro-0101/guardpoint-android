package com.guardpoint.android.domain.model;

import java.util.Collections;
import java.util.Map;

public class Resource<T> {

    public enum Status { LOADING, SUCCESS, ERROR }

    private final Status status;
    private final T data;
    private final String message;
    private final int errorCode;
    private final Map<String, String> fieldErrors;

    private Resource(Status status, T data, String message, int errorCode,
                     Map<String, String> fieldErrors) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
        this.fieldErrors = fieldErrors != null
                ? Collections.unmodifiableMap(fieldErrors)
                : Collections.emptyMap();
    }

    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null, 0, null);
    }

    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data, null, 0, null);
    }

    public static <T> Resource<T> error(String message) {
        return new Resource<>(Status.ERROR, null, message, 0, Collections.emptyMap());
    }

    public static <T> Resource<T> error(String message, int errorCode,
                                        Map<String, String> fieldErrors) {
        return new Resource<>(Status.ERROR, null, message, errorCode, fieldErrors);
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
}
