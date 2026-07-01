package com.guardpoint.android.domain.usecase;

import androidx.lifecycle.LiveData;

import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.model.Turno;
import com.guardpoint.android.domain.repository.TurnoRepository;

import javax.inject.Inject;

public class IniciarTurnoUseCase {

    private final TurnoRepository turnoRepository;

    @Inject
    public IniciarTurnoUseCase(TurnoRepository turnoRepository) {
        this.turnoRepository = turnoRepository;
    }

    public LiveData<Resource<Turno>> executar(String postoId, String deviceId) {
        return turnoRepository.iniciarTurno(postoId, deviceId);
    }
}
