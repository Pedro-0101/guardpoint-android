package com.guardpoint.android.ui.login;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.guardpoint.android.data.remote.dto.LoginResponse;
import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.repository.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final AuthRepository authRepository;

    @Inject
    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LiveData<Resource<LoginResponse>> login(String email, String senha) {
        return authRepository.login(email, senha);
    }

    public LiveData<Resource<Void>> registerBiometric() {
        return authRepository.registerBiometric();
    }

    public LiveData<Resource<LoginResponse>> authenticateWithBiometric() {
        return authRepository.authenticateWithBiometric();
    }

    public boolean hasValidSession() {
        return authRepository.hasValidSession();
    }

    public boolean isBiometricEnabled() {
        return authRepository.isBiometricEnabled();
    }

    public void logout() {
        authRepository.logout();
    }
}
