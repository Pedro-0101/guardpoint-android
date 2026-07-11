package com.guardpoint.android.domain.repository;

import androidx.lifecycle.LiveData;

import com.guardpoint.android.data.remote.dto.CheckinResponse;
import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.model.Turno;

public interface TurnoRepository {

    LiveData<Resource<Turno>> getTurnoAtivo();

    LiveData<Resource<Turno>> iniciarTurno(String deviceId, String postoId, String senha,
                                            double latitude, double longitude);

    LiveData<Resource<CheckinResponse>> realizarCheckin(String deviceId, String turnoId, String senha,
                                                        double latitude, double longitude);

    LiveData<Resource<Turno>> finalizarTurno(String deviceId, String turnoId, String senha,
                                              double latitude, double longitude);
}
