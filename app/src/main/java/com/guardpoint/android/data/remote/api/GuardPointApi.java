package com.guardpoint.android.data.remote.api;

import com.guardpoint.android.data.remote.dto.GenericResponse;
import com.guardpoint.android.data.remote.dto.LoginRequest;
import com.guardpoint.android.data.remote.dto.LoginResponse;
import com.guardpoint.android.data.remote.dto.RefreshRequest;
import com.guardpoint.android.data.remote.dto.RefreshResponse;
import com.guardpoint.android.data.remote.dto.TurnoListResponse;
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
}
