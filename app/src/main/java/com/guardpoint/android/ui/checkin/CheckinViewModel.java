package com.guardpoint.android.ui.checkin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.model.Turno;
import com.guardpoint.android.domain.usecase.RealizarCheckinUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CheckinViewModel extends ViewModel {

    private final RealizarCheckinUseCase realizarCheckinUseCase;

    private final MutableLiveData<Resource<Turno>> checkinState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> biometricSuccess = new MutableLiveData<>(false);

    private String turnoId;
    private double latitude;
    private double longitude;

    @Inject
    public CheckinViewModel(RealizarCheckinUseCase realizarCheckinUseCase) {
        this.realizarCheckinUseCase = realizarCheckinUseCase;
    }

    public LiveData<Resource<Turno>> getCheckinState() {
        return checkinState;
    }

    public LiveData<Boolean> getBiometricSuccess() {
        return biometricSuccess;
    }

    public void setDadosTurno(String turnoId, double latitude, double longitude) {
        this.turnoId = turnoId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void onBiometricPassed() {
        biometricSuccess.setValue(true);
    }

    public void realizarCheckin(String tipoSenha) {
        if (turnoId == null) return;

        checkinState.setValue(Resource.loading());

        realizarCheckinUseCase.executar(turnoId, tipoSenha, latitude, longitude)
                .observeForever(resource -> {
                    checkinState.postValue(resource);
                });
    }
}
