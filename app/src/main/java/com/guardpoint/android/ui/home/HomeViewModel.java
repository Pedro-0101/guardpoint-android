package com.guardpoint.android.ui.home;

import android.provider.Settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.model.Turno;
import com.guardpoint.android.domain.repository.CheckinRepository;
import com.guardpoint.android.domain.repository.TurnoRepository;
import com.guardpoint.android.domain.usecase.IniciarTurnoUseCase;
import com.guardpoint.android.util.NetworkMonitor;
import com.guardpoint.android.util.ServiceStateManager;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final TurnoRepository turnoRepository;
    private final IniciarTurnoUseCase iniciarTurnoUseCase;
    private final ServiceStateManager serviceStateManager;
    private final CheckinRepository checkinRepository;
    private final NetworkMonitor networkMonitor;

    private final MutableLiveData<Resource<Turno>> turnoState = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> finalizarState = new MutableLiveData<>();
    private final MutableLiveData<String> tempoRestante = new MutableLiveData<>();
    private final MutableLiveData<Boolean> turnoAtivo = new MutableLiveData<>(false);
    private final MutableLiveData<String> postoNome = new MutableLiveData<>();
    private final MutableLiveData<String> statusTurno = new MutableLiveData<>();
    private final MutableLiveData<String> postoIdLiveData = new MutableLiveData<>();
    private final MutableLiveData<Double> latitude = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> longitude = new MutableLiveData<>(0.0);

    private Turno turnoAtual;

    private LiveData<Resource<Turno>> turnoAtivoObservable;
    private LiveData<Resource<Turno>> iniciarTurnoObservable;

    private Observer<Resource<Turno>> onTurnoAtivoLoaded;
    private Observer<Resource<Turno>> onTurnoIniciado;

    private final Observer<String> onTempoRestante;
    private final Observer<Double> onLatitude;
    private final Observer<Double> onLongitude;

    @Inject
    public HomeViewModel(TurnoRepository turnoRepository, IniciarTurnoUseCase iniciarTurnoUseCase,
                         ServiceStateManager serviceStateManager,
                         CheckinRepository checkinRepository,
                         NetworkMonitor networkMonitor) {
        this.turnoRepository = turnoRepository;
        this.iniciarTurnoUseCase = iniciarTurnoUseCase;
        this.serviceStateManager = serviceStateManager;
        this.checkinRepository = checkinRepository;
        this.networkMonitor = networkMonitor;

        onTempoRestante = tempo -> {
            if (tempo != null) {
                tempoRestante.postValue(tempo);
            }
        };

        onLatitude = lat -> {
            if (lat != null) {
                latitude.postValue(lat);
            }
        };

        onLongitude = lon -> {
            if (lon != null) {
                longitude.postValue(lon);
            }
        };

        serviceStateManager.getTempoRestante().observeForever(onTempoRestante);
        serviceStateManager.getCurrentLatitude().observeForever(onLatitude);
        serviceStateManager.getCurrentLongitude().observeForever(onLongitude);

        onTurnoAtivoLoaded = resource -> {
            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                Turno turno = resource.getData();
                aplicarTurnoAtivo(turno);
            }
            if (turnoAtivoObservable != null) {
                turnoAtivoObservable.removeObserver(onTurnoAtivoLoaded);
            }
        };

        onTurnoIniciado = resource -> {
            turnoState.postValue(resource);

            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                Turno turno = resource.getData();
                aplicarTurnoAtivo(turno);
            }
            if (iniciarTurnoObservable != null) {
                iniciarTurnoObservable.removeObserver(onTurnoIniciado);
            }
        };
    }

    private void aplicarTurnoAtivo(Turno turno) {
        turnoAtual = turno;
        turnoAtivo.postValue(true);
        postoNome.postValue(turno.getPostoNome());
        statusTurno.postValue(turno.getStatus());
        postoIdLiveData.postValue(turno.getPostoId());
        serviceStateManager.setUltimoCheckinMillis(turno.getUltimoCheckinMillis());
    }

    public LiveData<Resource<Turno>> getTurnoState() {
        return turnoState;
    }

    public LiveData<Resource<Void>> getFinalizarState() {
        return finalizarState;
    }

    public LiveData<String> getTempoRestante() {
        return tempoRestante;
    }

    public LiveData<Boolean> getTurnoAtivo() {
        return turnoAtivo;
    }

    public LiveData<String> getPostoNome() {
        return postoNome;
    }

    public LiveData<String> getStatusTurno() {
        return statusTurno;
    }

    public LiveData<String> getPostoIdLiveData() {
        return postoIdLiveData;
    }

    public LiveData<Double> getLatitude() {
        return latitude;
    }

    public LiveData<Double> getLongitude() {
        return longitude;
    }

    public LiveData<Boolean> getIsOnline() {
        return networkMonitor.isOnline();
    }

    public LiveData<Integer> getPendentesCount() {
        return checkinRepository.getPendentesCount();
    }

    public Turno getTurnoAtual() {
        return turnoAtual;
    }

    public void carregarTurnoAtivo() {
        if (turnoRepository.hasTurnoAtivo()) {
            if (turnoAtivoObservable != null) {
                turnoAtivoObservable.removeObserver(onTurnoAtivoLoaded);
            }
            turnoAtivoObservable = turnoRepository.getTurnoAtivo();
            turnoAtivoObservable.observeForever(onTurnoAtivoLoaded);
        }
    }

    public void iniciarTurno(String postoId) {
        String deviceId = Settings.Secure.ANDROID_ID;
        turnoState.setValue(Resource.loading());

        if (iniciarTurnoObservable != null) {
            iniciarTurnoObservable.removeObserver(onTurnoIniciado);
        }
        iniciarTurnoObservable = iniciarTurnoUseCase.executar(postoId, deviceId);
        iniciarTurnoObservable.observeForever(onTurnoIniciado);
    }

    public void finalizarTurno(double latitude, double longitude) {
        if (turnoAtual == null) return;

        String timestamp = java.time.Instant.now().toString();
        String turnoId = turnoAtual.getTurnoId();
        finalizarState.setValue(Resource.loading());

        LiveData<Resource<Void>> result = turnoRepository.finalizarTurno(turnoId, latitude, longitude, timestamp);
        final Observer<Resource<Void>>[] holder = new Observer[1];
        holder[0] = resource -> {
            finalizarState.postValue(resource);
            if (resource != null && resource.isSuccess()) {
                turnoAtivo.postValue(false);
                turnoAtual = null;
                postoNome.postValue(null);
                statusTurno.postValue(null);
                postoIdLiveData.postValue(null);
            }
            result.removeObserver(holder[0]);
        };
        result.observeForever(holder[0]);
    }

    public void atualizarTurnoAposCheckin(Turno turno, long novoUltimoCheckin) {
        aplicarTurnoAtivo(turno);
        turnoRepository.atualizarUltimoCheckin(novoUltimoCheckin);
        serviceStateManager.setUltimoCheckinMillis(novoUltimoCheckin);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        serviceStateManager.getTempoRestante().removeObserver(onTempoRestante);
        serviceStateManager.getCurrentLatitude().removeObserver(onLatitude);
        serviceStateManager.getCurrentLongitude().removeObserver(onLongitude);
        if (turnoAtivoObservable != null) {
            turnoAtivoObservable.removeObserver(onTurnoAtivoLoaded);
        }
        if (iniciarTurnoObservable != null) {
            iniciarTurnoObservable.removeObserver(onTurnoIniciado);
        }
    }
}
