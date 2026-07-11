package com.guardpoint.android.data.remote.api;

import com.guardpoint.android.data.remote.dto.CheckinRequest;
import com.guardpoint.android.data.remote.dto.CheckinResponse;
import com.guardpoint.android.data.remote.dto.FinalizarTurnoRequest;
import com.guardpoint.android.data.remote.dto.GenericResponse;
import com.guardpoint.android.data.remote.dto.IniciarTurnoRequest;
import com.guardpoint.android.data.remote.dto.LoginRequest;
import com.guardpoint.android.data.remote.dto.LoginResponse;
import com.guardpoint.android.data.remote.dto.RefreshRequest;
import com.guardpoint.android.data.remote.dto.RefreshResponse;
import com.guardpoint.android.data.remote.dto.TurnoListResponse;
import com.guardpoint.android.data.remote.dto.TurnoResponse;
import com.guardpoint.android.data.remote.dto.TurnoStatusResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GuardPointApi {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/refresh")
    Call<RefreshResponse> refreshToken(@Body RefreshRequest request);

    @POST("auth/logout")
    Call<GenericResponse> logout();

    @GET("turnos/status")
    Call<TurnoStatusResponse> getStatusTurno();

    @GET("turnos")
    Call<TurnoListResponse> getTurnos(@Query("status") String status);

    @POST("turnos/iniciar")
    Call<TurnoResponse> iniciarTurno(@Body IniciarTurnoRequest request);

    @POST("turnos/checkin")
    Call<CheckinResponse> realizarCheckin(@Body CheckinRequest request);

    @POST("turnos/finalizar")
    Call<TurnoResponse> finalizarTurno(@Body FinalizarTurnoRequest request);
}
