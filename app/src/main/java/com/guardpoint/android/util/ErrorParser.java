package com.guardpoint.android.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.guardpoint.android.data.remote.dto.ApiError;

import java.io.IOException;

import retrofit2.Response;
import timber.log.Timber;

public final class ErrorParser {

    private static final Gson gson = new Gson();

    private ErrorParser() {
    }

    public static String parse(Response<?> response) {
        if (response.errorBody() != null) {
            try {
                String rawBody = response.errorBody().string();
                ApiError apiError = gson.fromJson(rawBody, ApiError.class);
                String displayMessage = apiError.getDisplayMessage();
                if (displayMessage != null && !displayMessage.isEmpty()) {
                    return capitalize(displayMessage);
                }
            } catch (JsonSyntaxException e) {
                Timber.w(e, "ErrorParser: corpo de erro nao e um JSON valido");
            } catch (IOException e) {
                Timber.w(e, "ErrorParser: falha ao ler errorBody");
            }
        }

        return "Erro " + response.code();
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
