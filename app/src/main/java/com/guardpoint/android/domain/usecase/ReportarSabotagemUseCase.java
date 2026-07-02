package com.guardpoint.android.domain.usecase;

import androidx.lifecycle.LiveData;

import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.repository.TurnoRepository;

import javax.inject.Inject;

public class ReportarSabotagemUseCase {

    private final TurnoRepository turnoRepository;

    @Inject
    public ReportarSabotagemUseCase(TurnoRepository turnoRepository) {
        this.turnoRepository = turnoRepository;
    }

    public LiveData<Resource<Void>> executar(String turnoId, double latitude, double longitude,
                                             String motivo, String timestamp) {
        return turnoRepository.reportarSabotagem(turnoId, latitude, longitude, motivo, timestamp);
    }
}
