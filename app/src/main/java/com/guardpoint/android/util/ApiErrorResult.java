package com.guardpoint.android.util;

import java.util.Collections;
import java.util.Map;

public class ApiErrorResult {

    private final int statusCode;
    private final String rawError;
    private final String userMessage;
    private final Map<String, String> validationErrors;

    public ApiErrorResult(int statusCode, String rawError, String userMessage,
                          Map<String, String> validationErrors) {
        this.statusCode = statusCode;
        this.rawError = rawError;
        this.userMessage = userMessage;
        this.validationErrors = validationErrors != null
                ? Collections.unmodifiableMap(validationErrors)
                : Collections.emptyMap();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getRawError() {
        return rawError;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public boolean isAuthError() {
        return statusCode == 401;
    }

    public boolean isForbidden() {
        return statusCode == 403;
    }

    public boolean isNotFound() {
        return statusCode == 404;
    }

    public boolean isBadRequest() {
        return statusCode == 400;
    }

    public boolean isConflict() {
        return statusCode == 409;
    }

    public boolean isValidationError() {
        return statusCode == 422;
    }

    public boolean isServerError() {
        return statusCode == 500;
    }

    public boolean isTokenExpirado() {
        return statusCode == 401 && rawError != null && rawError.contains("token expirado");
    }

    public boolean isTokenInvalido() {
        return statusCode == 401 && rawError != null
                && (rawError.contains("token invalido") || rawError.contains("token nao fornecido"));
    }

    public boolean isCredenciaisInvalidas() {
        return statusCode == 401 && rawError != null && rawError.contains("email ou senha invalidos");
    }

    public boolean isDispositivoNaoReconhecido() {
        return statusCode == 401 && rawError != null && rawError.contains("dispositivo nao reconhecido");
    }

    public boolean isAcessoNegado() {
        return statusCode == 403 && rawError != null && rawError.contains("acesso negado");
    }

    public boolean isUsuarioInativo() {
        return statusCode == 403 && rawError != null && rawError.contains("usuario inativo");
    }

    public boolean isDispositivoNaoRegistrado() {
        return statusCode == 403 && rawError != null && rawError.contains("dispositivo nao registrado");
    }

    public boolean isSessaoRevogada() {
        return statusCode == 403 && rawError != null && rawError.contains("sessao revogada");
    }

    public boolean isPinInvalido() {
        return statusCode == 403 && rawError != null && rawError.contains("pin invalido");
    }

    public boolean isPinExpirado() {
        return statusCode == 403 && rawError != null && rawError.contains("pin expirado");
    }

    public boolean isTurnoNaoPertenceUsuario() {
        return statusCode == 403 && rawError != null && rawError.contains("turno nao pertence");
    }

    public boolean isTurnoAssociadoOutroDispositivo() {
        return statusCode == 403 && rawError != null && rawError.contains("turno associado a outro dispositivo");
    }

    public boolean isTurnoJaFinalizado() {
        return statusCode == 409 && rawError != null && rawError.contains("turno ja finalizado");
    }

    public boolean isTurnoEmAndamento() {
        return statusCode == 409 && rawError != null && rawError.contains("turno em andamento");
    }

    public boolean isEmailJaCadastrado() {
        return statusCode == 409 && rawError != null && rawError.contains("email ja cadastrado");
    }

    public boolean isNomeJaCadastrado() {
        return statusCode == 409 && rawError != null && rawError.contains("nome ja cadastrado");
    }

    public boolean isHorarioForaTolerancia() {
        return statusCode == 403 && rawError != null && rawError.contains("horario de inicio fora da tolerancia");
    }

    public boolean isNenhumaEscalaAtiva() {
        return statusCode == 403 && rawError != null && rawError.contains("nenhuma escala ativa");
    }

    public boolean hasValidationErrors() {
        return !validationErrors.isEmpty();
    }
}
