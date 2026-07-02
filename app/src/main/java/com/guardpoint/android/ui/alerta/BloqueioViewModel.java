package com.guardpoint.android.ui.alerta;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.guardpoint.android.data.local.db.dao.TurnoDao;
import com.guardpoint.android.data.local.db.entity.TurnoAtivo;
import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.usecase.ReportarSabotagemUseCase;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BloqueioViewModel extends ViewModel {

    private final ReportarSabotagemUseCase reportarSabotagemUseCase;
    private final TurnoDao turnoDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<String> postoNome = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> sabotagemState = new MutableLiveData<>();
    private final MutableLiveData<Double> ultimaLatitude = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> ultimaLongitude = new MutableLiveData<>(0.0);

    @Inject
    public BloqueioViewModel(ReportarSabotagemUseCase reportarSabotagemUseCase, TurnoDao turnoDao) {
        this.reportarSabotagemUseCase = reportarSabotagemUseCase;
        this.turnoDao = turnoDao;
    }

    public LiveData<String> getPostoNome() {
        return postoNome;
    }

    public LiveData<Resource<Void>> getSabotagemState() {
        return sabotagemState;
    }

    public LiveData<Double> getUltimaLatitude() {
        return ultimaLatitude;
    }

    public LiveData<Double> getUltimaLongitude() {
        return ultimaLongitude;
    }

    public void carregarDadosTurno(String turnoId) {
        executor.execute(() -> {
            TurnoAtivo turno = turnoDao.getTurnoAtivo();
            if (turno != null && turno.turnoId.equals(turnoId)) {
                postoNome.postValue(turno.postoNome);
            }
        });
    }

    public void reportarSabotagem(String turnoId, double latitude, double longitude) {
        sabotagemState.setValue(Resource.loading());

        String timestamp = Instant.now().toString();
        String motivo = "GPS_DESATIVADO";

        LiveData<Resource<Void>> result = reportarSabotagemUseCase.executar(
                turnoId, latitude, longitude, motivo, timestamp);

        final Observer<Resource<Void>>[] holder = new Observer[1];
        holder[0] = resource -> {
            sabotagemState.postValue(resource);
            result.removeObserver(holder[0]);
        };
        result.observeForever(holder[0]);
    }

    public void atualizarLocalizacao(double latitude, double longitude) {
        ultimaLatitude.postValue(latitude);
        ultimaLongitude.postValue(longitude);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
