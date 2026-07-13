package com.guardpoint.android.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.guardpoint.android.data.local.prefs.SecurePrefs;
import com.guardpoint.android.data.remote.dto.BiometricRegisterResponse;
import com.guardpoint.android.data.remote.dto.LoginResponse;
import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.repository.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final SecurePrefs securePrefs;

    @Inject
    public LoginViewModel(AuthRepository authRepository, SecurePrefs securePrefs) {
        this.authRepository = authRepository;
        this.securePrefs = securePrefs;
    }

    public LiveData<Resource<LoginResponse>> login(String email, String senha) {
        return authRepository.login(email, senha);
    }

    public LiveData<Resource<LoginResponse>> loginVigia(String codigoEmpresa, String nome, String senha) {
        return authRepository.loginVigia(codigoEmpresa, nome, senha);
    }

    public LiveData<Resource<BiometricRegisterResponse>> registerDevice(String deviceId) {
        return authRepository.registerDevice(deviceId);
    }

    public LiveData<Resource<LoginResponse>> loginBiometric(String deviceId) {
        String deviceSecret = securePrefs.getDeviceSecret();
        String empresaId = securePrefs.getCompanyId();
        if (deviceSecret == null || empresaId == null) {
            MutableLiveData<Resource<LoginResponse>> error = new MutableLiveData<>();
            error.setValue(Resource.error("Dispositivo nao registrado"));
            return error;
        }
        return authRepository.loginBiometric(deviceId, deviceSecret, empresaId);
    }

    public boolean hasValidSession() {
        return authRepository.hasValidSession();
    }

    public boolean hasDeviceSecret() {
        return authRepository.hasDeviceSecret();
    }

    public void logout() {
        authRepository.logout();
    }

    public boolean isBiometricEnabled() {
        return securePrefs.isBiometricEnabled();
    }

    public void setBiometricEnabled(boolean enabled) {
        securePrefs.setBiometricEnabled(enabled);
    }
}
