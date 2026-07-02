package com.guardpoint.android.domain.usecase;

import androidx.lifecycle.LiveData;

import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.model.Turno;
import com.guardpoint.android.domain.repository.CheckinRepository;

import javax.inject.Inject;

public class RealizarCheckinUseCase {

    private final CheckinRepository checkinRepository;

    @Inject
    public RealizarCheckinUseCase(CheckinRepository checkinRepository) {
        this.checkinRepository = checkinRepository;
    }

    public LiveData<Resource<Turno>> executar(String turnoId, String senha, String tipoSenha,
                                              double latitude, double longitude) {
        return checkinRepository.realizarCheckin(turnoId, senha, tipoSenha, latitude, longitude);
    }
}
