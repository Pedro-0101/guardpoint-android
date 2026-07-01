package com.guardpoint.android.data.remote.api;

import com.guardpoint.android.data.remote.dto.LoginRequest;
import com.guardpoint.android.data.remote.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GuardPointApi {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}
