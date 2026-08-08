package com.guardpoint.android.util;

import com.google.gson.Gson;
import com.guardpoint.android.data.remote.dto.ApiError;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Response;
import timber.log.Timber;

public final class ErrorParser {

    private static final Gson gson = new Gson();
    private static final Pattern VALIDATION_PATTERN = Pattern.compile("(\\w+)\\s*\\(([^)]+)\\)");

    private ErrorParser() {
    }

    public static ApiErrorResult parse(Response<?> response) {
        int statusCode = response.code();
        String rawError = null;
        String rawBody = null;

        if (response.errorBody() != null) {
            try {
                rawBody = response.errorBody().string();
                ApiError apiError = gson.fromJson(rawBody, ApiError.class);
                rawError = apiError.getDisplayMessage();
            } catch (IOException e) {
                Timber.w(e, "ErrorParser: falha ao ler errorBody");
            } catch (com.google.gson.JsonSyntaxException e) {
                Timber.w(e, "ErrorParser: corpo de erro nao e um JSON valido");
            }
        }

        if (rawError == null) {
            rawError = "Erro " + statusCode;
        }

        return buildResult(statusCode, rawError, rawBody);
    }

    private static ApiErrorResult buildResult(int statusCode, String rawError, String rawBody) {
        String userMessage = resolveUserMessage(statusCode, rawError);
        Map<String, String> validationErrors = statusCode == 422
                ? parseValidationErrors(rawError, rawBody)
                : Collections.emptyMap();

        return new ApiErrorResult(statusCode, rawError, userMessage, validationErrors);
    }

    private static String resolveUserMessage(int statusCode, String rawError) {
        switch (statusCode) {
            case 401:
                return resolveAuthMessage(rawError);
            case 403:
                return resolveForbiddenMessage(rawError);
            case 404:
                return resolveNotFoundMessage(rawError);
            case 409:
                return resolveConflictMessage(rawError);
            case 422:
                return "Dados invalidos.";
            case 500:
                return "Erro inesperado. Tente novamente.";
            default:
                return capitalize(rawError);
        }
    }

    private static String resolveAuthMessage(String rawError) {
        if (rawError == null) {
            return "Erro de autenticacao";
        }
        if (rawError.contains("token expirado")) {
            return "Sessao expirada. Faca login novamente.";
        }
        if (rawError.contains("token invalido") || rawError.contains("token nao fornecido")) {
            return "Sessao invalida. Faca login novamente.";
        }
        if (rawError.contains("autenticacao necessaria")) {
            return "Autenticacao necessaria. Faca login.";
        }
        if (rawError.contains("email ou senha invalidos")) {
            return "Email ou senha invalidos.";
        }
        if (rawError.contains("refresh token invalido ou expirado")) {
            return "Sessao expirada. Faca login novamente.";
        }
        if (rawError.contains("dispositivo nao reconhecido")) {
            return "Dispositivo nao reconhecido. Registre este dispositivo.";
        }
        return capitalize(rawError);
    }

    private static String resolveForbiddenMessage(String rawError) {
        if (rawError == null) {
            return "Acesso negado";
        }
        if (rawError.contains("usuario inativo")) {
            return "Conta de usuario inativa.";
        }
        if (rawError.contains("dispositivo nao registrado")) {
            return "Dispositivo nao registrado. Faca login biometrico primeiro.";
        }
        if (rawError.contains("sessao revogada")) {
            return "Sessao revogada. Reassocie o turno com o PIN.";
        }
        if (rawError.contains("pin expirado")) {
            return "PIN expirado. Solicite nova revogacao.";
        }
        if (rawError.contains("pin invalido")) {
            return "PIN invalido.";
        }
        if (rawError.contains("turno nao pertence a este usuario")) {
            return "Este turno nao pertence ao seu usuario.";
        }
        if (rawError.contains("turno associado a outro dispositivo")) {
            return "Este turno esta associado a outro dispositivo.";
        }
        if (rawError.contains("turno ja finalizado")) {
            return "Este turno ja foi finalizado.";
        }
        if (rawError.contains("horario de inicio fora da tolerancia")) {
            return "Fora do horario permitido para iniciar o turno.";
        }
        if (rawError.contains("nenhuma escala ativa encontrada")) {
            return "Nenhuma escala ativa para este usuario, posto e horario.";
        }
        if (rawError.contains("acesso negado")) {
            return "Acesso negado.";
        }
        return capitalize(rawError);
    }

    private static String resolveNotFoundMessage(String rawError) {
        if (rawError == null) {
            return "Nao encontrado";
        }
        if (rawError.contains("usuario nao encontrado")) {
            return "Usuario nao encontrado.";
        }
        if (rawError.contains("posto nao encontrado")) {
            return "Posto nao encontrado ou inativo.";
        }
        if (rawError.contains("turno nao encontrado") || rawError.contains("nenhum turno ativo encontrado")) {
            return "Nenhum turno ativo encontrado.";
        }
        if (rawError.contains("escala nao encontrada")) {
            return "Escala nao encontrada.";
        }
        if (rawError.contains("alerta nao encontrado")) {
            return "Alerta nao encontrado.";
        }
        if (rawError.contains("config nao encontrada")) {
            return "Configuracao nao encontrada.";
        }
        if (rawError.contains("substituicao nao encontrada")) {
            return "Substituicao nao encontrada.";
        }
        if (rawError.contains("senha nao encontrada")) {
            return "Senha nao encontrada.";
        }
        return "Nao encontrado.";
    }

    private static String resolveConflictMessage(String rawError) {
        if (rawError == null) {
            return "Conflito";
        }
        if (rawError.contains("usuario ja possui um turno em andamento")) {
            return "Voce ja possui um turno em andamento.";
        }
        if (rawError.contains("email ja cadastrado")) {
            return "Este email ja esta cadastrado.";
        }
        if (rawError.contains("nome ja cadastrado")) {
            return "Este nome ja esta cadastrado.";
        }
        return capitalize(rawError);
    }

    public static Map<String, String> parseValidationErrors(String message) {
        return parseValidationErrors(message, null);
    }

    public static Map<String, String> parseValidationErrors(String message, String rawBody) {
        if (message == null && rawBody == null) {
            return Collections.emptyMap();
        }

        Map<String, String> errors = new LinkedHashMap<>();

        if (message != null) {
            Matcher matcher = VALIDATION_PATTERN.matcher(message);
            while (matcher.find()) {
                String campo = matcher.group(1);
                String regra = matcher.group(2);
                errors.put(campo, regra.contains("=") ? regra.substring(0, regra.indexOf('=')) : regra);
            }
        }

        if (errors.isEmpty() && rawBody != null) {
            try {
                com.google.gson.JsonObject json = gson.fromJson(rawBody, com.google.gson.JsonObject.class);
                if (json != null && json.has("errors")) {
                    com.google.gson.reflect.TypeToken<Map<String, java.util.List<String>>> typeToken =
                            new com.google.gson.reflect.TypeToken<Map<String, java.util.List<String>>>() {};
                    Map<String, java.util.List<String>> bodyErrors =
                            gson.fromJson(json.get("errors"), typeToken.getType());
                    if (bodyErrors != null) {
                        for (Map.Entry<String, java.util.List<String>> entry : bodyErrors.entrySet()) {
                            java.util.List<String> msgs = entry.getValue();
                            if (msgs != null && !msgs.isEmpty()) {
                                errors.put(entry.getKey(), msgs.get(0));
                            }
                        }
                    }
                }
            } catch (com.google.gson.JsonSyntaxException ignored) {
                Timber.d("ErrorParser: rawBody nao e um mapa de erros por campo");
            }
        }

        return errors;
    }

    public static boolean isTokenExpirado(String errorMessage) {
        if (errorMessage == null) {
            return false;
        }
        return errorMessage.toLowerCase().contains("token expirado");
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
