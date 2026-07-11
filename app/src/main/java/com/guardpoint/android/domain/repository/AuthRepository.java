package com.guardpoint.android.domain.repository;

import androidx.lifecycle.LiveData;

import com.guardpoint.android.data.remote.dto.LoginResponse;
import com.guardpoint.android.domain.model.Resource;

public interface AuthRepository {

    LiveData<Resource<LoginResponse>> login(String email, String senha);

    boolean hasValidSession();

    void logout();
}
