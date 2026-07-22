package com.guardpoint.android.data.remote.api;

import com.guardpoint.android.data.remote.dto.BiometricLoginRequest;
import com.guardpoint.android.data.remote.dto.BiometricRegisterRequest;
import com.guardpoint.android.data.remote.dto.BiometricRegisterResponse;
import com.guardpoint.android.data.remote.dto.CheckinRequest;
import com.guardpoint.android.data.remote.dto.CheckinResponse;
import com.guardpoint.android.data.remote.dto.FinalizarTurnoRequest;
import com.guardpoint.android.data.remote.dto.GenericResponse;
import com.guardpoint.android.data.remote.dto.IniciarResponse;
import com.guardpoint.android.data.remote.dto.IniciarTurnoRequest;
import com.guardpoint.android.data.remote.dto.LoginRequest;
import com.guardpoint.android.data.remote.dto.LoginResponse;
import com.guardpoint.android.data.remote.dto.RefreshRequest;
import com.guardpoint.android.data.remote.dto.TurnoResponse;
import com.guardpoint.android.data.remote.dto.VigiaTurnoResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface GuardPointApi {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/refresh")
    Call<LoginResponse> refreshToken(@Body RefreshRequest request);

    @POST("auth/biometric/register")
    Call<BiometricRegisterResponse> registerBiometric(@Body BiometricRegisterRequest request);

    @POST("auth/biometric/login")
    Call<LoginResponse> loginBiometric(@Body BiometricLoginRequest request);

    @POST("auth/logout")
    Call<GenericResponse> logout();

    @GET("vigia/turno")
    Call<VigiaTurnoResponse> getVigiaTurno();

    @POST("turnos/iniciar")
    Call<IniciarResponse> iniciarTurno(@Body IniciarTurnoRequest request);

    @POST("turnos/checkin")
    Call<CheckinResponse> realizarCheckin(@Body CheckinRequest request);

    @POST("turnos/finalizar")
    Call<TurnoResponse> finalizarTurno(@Body FinalizarTurnoRequest request);
}
