package com.guardpoint.android.data.remote.api;

import com.guardpoint.android.data.remote.dto.BiometricRequest;
import com.guardpoint.android.data.remote.dto.CheckinRequest;
import com.guardpoint.android.data.remote.dto.FinalizarTurnoRequest;
import com.guardpoint.android.data.remote.dto.GenericResponse;
import com.guardpoint.android.data.remote.dto.LoginRequest;
import com.guardpoint.android.data.remote.dto.LoginResponse;
import com.guardpoint.android.data.remote.dto.RefreshRequest;
import com.guardpoint.android.data.remote.dto.RefreshResponse;
import com.guardpoint.android.data.remote.dto.SabotagemRequest;
import com.guardpoint.android.data.remote.dto.TurnoIniciarRequest;
import com.guardpoint.android.data.remote.dto.TurnoResponse;

import com.guardpoint.android.data.remote.dto.LoteCheckinRequest;

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

    @POST("turnos/iniciar")
    Call<TurnoResponse> iniciarTurno(@Body TurnoIniciarRequest request);

    @POST("turnos/checkin")
    Call<TurnoResponse> checkin(@Body CheckinRequest request);

    @POST("turnos/finalizar")
    Call<GenericResponse> finalizarTurno(@Body FinalizarTurnoRequest request);

    @POST("turnos/sabotagem")
    Call<GenericResponse> sabotagem(@Body SabotagemRequest request);

    @POST("checkins/lote")
    Call<GenericResponse> enviarLote(@Body LoteCheckinRequest checkins);
}
