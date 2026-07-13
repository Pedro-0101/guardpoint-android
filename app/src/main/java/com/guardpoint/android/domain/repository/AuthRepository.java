package com.guardpoint.android.domain.repository;

import androidx.lifecycle.LiveData;

import com.guardpoint.android.data.remote.dto.BiometricRegisterResponse;
import com.guardpoint.android.data.remote.dto.LoginResponse;
import com.guardpoint.android.domain.model.Resource;

public interface AuthRepository {

    LiveData<Resource<LoginResponse>> login(String email, String senha);

    LiveData<Resource<LoginResponse>> loginVigia(String codigoEmpresa, String nome, String senha);

    LiveData<Resource<BiometricRegisterResponse>> registerDevice(String deviceId);

    LiveData<Resource<LoginResponse>> loginBiometric(String deviceId, String deviceSecret, String empresaId);

    boolean hasValidSession();

    boolean hasDeviceSecret();

    void logout();
}
