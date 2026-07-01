package com.guardpoint.android.domain.repository;

import androidx.lifecycle.LiveData;

import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.model.Turno;

public interface CheckinRepository {

    LiveData<Resource<Turno>> realizarCheckin(String turnoId, String tipoSenha,
                                              double latitude, double longitude);

    void salvarCheckinPendente(String turnoId, String tipoSenha,
                               double latitude, double longitude);
}
