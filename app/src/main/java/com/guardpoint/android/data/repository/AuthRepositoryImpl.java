package com.guardpoint.android.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.guardpoint.android.data.local.prefs.SecurePrefs;
import com.guardpoint.android.data.remote.api.GuardPointApi;
import com.guardpoint.android.data.remote.dto.BiometricRequest;
import com.guardpoint.android.data.remote.dto.LoginRequest;
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
        MutableLiveData<Resource<LoginResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        LoginRequest request = new LoginRequest(email, senha);
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
                    }
                    result.setValue(Resource.success(body));
                } else {
                    String errorMsg = parseError(response);
                    result.setValue(Resource.error(errorMsg));
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
    public LiveData<Resource<Void>> registerBiometric() {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        String deviceId = android.provider.Settings.Secure.ANDROID_ID;
        String companyId = securePrefs.getCompanyId();
        BiometricRequest request = new BiometricRequest(companyId, deviceId);
        api.registerBiometric(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    securePrefs.saveBiometricEnabled(true);
                    result.setValue(Resource.success(null));
                } else {
                    result.setValue(Resource.error("Falha ao registrar biometria"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                result.setValue(Resource.error(t.getMessage() != null ? t.getMessage() : "Erro de conexão"));
            }
        });

        return result;
    }

    @Override
    public LiveData<Resource<LoginResponse>> authenticateWithBiometric() {
        MutableLiveData<Resource<LoginResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        String deviceId = android.provider.Settings.Secure.ANDROID_ID;
        String companyId = securePrefs.getCompanyId();
        BiometricRequest request = new BiometricRequest(companyId, deviceId);
        api.biometricLogin(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();
                    securePrefs.saveAccessToken(body.getAccessToken());
                    securePrefs.saveRefreshToken(body.getRefreshToken());
                    if (body.getUsuario() != null) {
                        securePrefs.saveUserId(body.getUsuario().getId());
                        securePrefs.saveCompanyId(body.getUsuario().getEmpresaId());
                    }
                    result.setValue(Resource.success(body));
                } else {
                    String errorMsg = parseError(response);
                    result.setValue(Resource.error(errorMsg));
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
        if (token == null || token.isEmpty()) {
            return false;
        }
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return false;
            }
            String payload = new String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT));
            long exp = new com.google.gson.Gson().fromJson(payload, JwtPayload.class).exp;
            return (exp * 1000L) > System.currentTimeMillis();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isBiometricEnabled() {
        return securePrefs.isBiometricEnabled();
    }

    @Override
    public void logout() {
        String deviceId = android.provider.Settings.Secure.ANDROID_ID;
        String companyId = securePrefs.getCompanyId();
        BiometricRequest request = new BiometricRequest(companyId, deviceId);
        api.logout(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
            }
        });
        securePrefs.clear();
    }

    private String parseError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                return response.errorBody().string();
            }
        } catch (IOException e) {
            return "Erro desconhecido";
        }
        return "Erro " + response.code();
    }

    private static class JwtPayload {
        long exp;
    }
}
