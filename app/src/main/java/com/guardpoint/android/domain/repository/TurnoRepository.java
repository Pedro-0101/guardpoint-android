package com.guardpoint.android.domain.repository;

import androidx.lifecycle.LiveData;

import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.model.Turno;

public interface TurnoRepository {

    LiveData<Resource<Turno>> iniciarTurno(String postoId, String deviceId);

    LiveData<Resource<Turno>> getTurnoAtivo();

    boolean hasTurnoAtivo();

    void atualizarUltimoCheckin(long timestampMillis);

    LiveData<Resource<Void>> finalizarTurno(String turnoId, double latitude, double longitude, String timestamp);
}
