package com.guardpoint.android.data.remote.api;

import com.guardpoint.android.data.remote.dto.BiometricRequest;
import com.guardpoint.android.data.remote.dto.LoginRequest;
import com.guardpoint.android.data.remote.dto.LoginResponse;
import com.guardpoint.android.data.remote.dto.RefreshRequest;
import com.guardpoint.android.data.remote.dto.RefreshResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GuardPointApi {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/biometric/register")
    Call<Void> registerBiometric(@Body BiometricRequest request);

    @POST("auth/biometric/login")
    Call<LoginResponse> biometricLogin(@Body BiometricRequest request);

    @POST("auth/refresh")
    Call<RefreshResponse> refreshToken(@Body RefreshRequest request);

    @POST("auth/logout")
    Call<Void> logout(@Body BiometricRequest request);
}
