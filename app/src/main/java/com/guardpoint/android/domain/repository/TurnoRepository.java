package com.guardpoint.android.domain.repository;

import androidx.lifecycle.LiveData;

import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.model.Turno;

public interface TurnoRepository {

    LiveData<Resource<Turno>> getTurnoAtivo();
}
