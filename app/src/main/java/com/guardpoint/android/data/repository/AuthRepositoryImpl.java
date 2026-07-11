package com.guardpoint.android.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.guardpoint.android.data.local.prefs.SecurePrefs;
import com.guardpoint.android.data.remote.api.GuardPointApi;
import com.guardpoint.android.data.remote.dto.LoginRequest;
import com.guardpoint.android.data.remote.dto.GenericResponse;
import com.guardpoint.android.data.remote.dto.LoginResponse;
import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.repository.AuthRepository;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class AuthRepositoryImpl implements AuthRepository {

    private final GuardPointApi api;
    private final SecurePrefs securePrefs;

    @Inject
    public AuthRepositoryImpl(GuardPointApi api, SecurePrefs securePrefs) {
        this.api = api;
        this.securePrefs = securePrefs;
    }

    @Override
    public LiveData<Resource<LoginResponse>> login(String email, String senha) {
        return executeLogin(new LoginRequest(email, senha));
    }

    @Override
    public LiveData<Resource<LoginResponse>> loginVigia(String codigoEmpresa, String nome, String senha) {
        return executeLogin(new LoginRequest(codigoEmpresa, nome, senha));
    }

    private LiveData<Resource<LoginResponse>> executeLogin(LoginRequest request) {
        MutableLiveData<Resource<LoginResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        api.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();
                    securePrefs.saveAccessToken(body.getAccessToken());
                    securePrefs.saveRefreshToken(body.getRefreshToken());
                    if (body.getUsuario() != null) {
                        securePrefs.saveUserId(body.getUsuario().getId());
                        securePrefs.saveCompanyId(body.getUsuario().getEmpresaId());
                        securePrefs.saveUserNome(body.getUsuario().getNome());
                        securePrefs.saveUserRole(body.getUsuario().getRole());
                    }
                    result.setValue(Resource.success(body));
                } else {
                    result.setValue(Resource.error(parseError(response)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error(t.getMessage() != null ? t.getMessage() : "Falha na conexão"));
            }
        });

        return result;
    }

    @Override
    public boolean hasValidSession() {
        String token = securePrefs.getAccessToken();
        if (token == null || token.isEmpty()) return false;
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return false;
            String payload = new String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT));
            long exp = new com.google.gson.Gson().fromJson(payload, JwtPayload.class).exp;
            return (exp * 1000L) > System.currentTimeMillis();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void logout() {
        api.logout().enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
            }
            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
            }
        });
        securePrefs.clear();
    }

    private String parseError(Response<?> response) {
        try {
            if (response.errorBody() != null) return response.errorBody().string();
        } catch (IOException e) {
            return "Erro desconhecido";
        }
        return "Erro " + response.code();
    }

    private static class JwtPayload {
        long exp;
    }
}
