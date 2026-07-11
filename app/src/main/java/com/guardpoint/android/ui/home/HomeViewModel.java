package com.guardpoint.android.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.guardpoint.android.data.local.prefs.SecurePrefs;
import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.model.Turno;
import com.guardpoint.android.domain.repository.TurnoRepository;
import com.guardpoint.android.util.NetworkMonitor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    public enum TurnoState {
        LOADING,
        SCHEDULED_FUTURE,
        SCHEDULED_READY,
        IN_PROGRESS,
        NONE
    }

    private final TurnoRepository turnoRepository;
    private final NetworkMonitor networkMonitor;

    private final MutableLiveData<TurnoState> turnoState = new MutableLiveData<>(TurnoState.LOADING);
    private final MutableLiveData<String> tempoRestante = new MutableLiveData<>();
    private final MutableLiveData<String> postoNome = new MutableLiveData<>();
    private final MutableLiveData<String> inicioPrevisto = new MutableLiveData<>();
    private final MutableLiveData<String> userNome = new MutableLiveData<>();
    private final MutableLiveData<String> userRole = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isActionLoading = new MutableLiveData<>(false);

    private Turno currentTurno;
    private LiveData<Resource<Turno>> turnoAtivoObservable;
    private Observer<Resource<Turno>> onTurnoAtivoLoaded;
    private Timer timer;

    @Inject
    public HomeViewModel(TurnoRepository turnoRepository,
                         NetworkMonitor networkMonitor,
                         SecurePrefs securePrefs) {
        this.turnoRepository = turnoRepository;
        this.networkMonitor = networkMonitor;

        userNome.setValue(securePrefs.getUserNome());
        userRole.setValue(securePrefs.getUserRole());

        onTurnoAtivoLoaded = resource -> {
            if (resource == null) return;
            if (resource.isSuccess() && resource.getData() != null) {
                aplicarTurno(resource.getData());
                if (turnoAtivoObservable != null) {
                    turnoAtivoObservable.removeObserver(onTurnoAtivoLoaded);
                }
            } else if (resource.isError()) {
                turnoState.postValue(TurnoState.NONE);
                if (turnoAtivoObservable != null) {
                    turnoAtivoObservable.removeObserver(onTurnoAtivoLoaded);
                }
            }
        };
    }

    private void aplicarTurno(Turno turno) {
        currentTurno = turno;
        postoNome.postValue(turno.getPostoNome());

        if ("em_andamento".equals(turno.getStatus())) {
            turnoState.postValue(TurnoState.IN_PROGRESS);
            iniciarTimer(turno);
        } else {
            long inicioPrev = turno.getInicioPrevistoMillis();
            long agora = System.currentTimeMillis();

            if (inicioPrev > 0 && inicioPrev <= agora) {
                turnoState.postValue(TurnoState.SCHEDULED_READY);
            } else {
                turnoState.postValue(TurnoState.SCHEDULED_FUTURE);
            }

            if (inicioPrev > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                inicioPrevisto.postValue(sdf.format(new Date(inicioPrev)));
            }
        }
    }

    private void iniciarTimer(Turno turno) {
        if (timer != null) timer.cancel();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long resto = turno.getTempoRestanteMillis();
                if (resto <= 0) {
                    tempoRestante.postValue("00:00");
                    return;
                }
                long minutos = (resto / 60000) % 60;
                long horas = resto / 3600000;
                tempoRestante.postValue(String.format("%02d:%02d", horas, minutos));
            }
        }, 0, 1000);
    }

    public void carregarTurnoAtivo() {
        if (turnoAtivoObservable != null) {
            turnoAtivoObservable.removeObserver(onTurnoAtivoLoaded);
        }
        turnoState.setValue(TurnoState.LOADING);
        turnoAtivoObservable = turnoRepository.getTurnoAtivo();
        turnoAtivoObservable.observeForever(onTurnoAtivoLoaded);
    }

    public void executarAcao(String deviceId, double latitude, double longitude, String senha) {
        if (currentTurno == null || isActionLoading.getValue() == Boolean.TRUE) return;

        isActionLoading.setValue(true);

        if (TurnoState.SCHEDULED_READY.equals(turnoState.getValue())) {
            LiveData<Resource<Turno>> iniciarResult = turnoRepository.iniciarTurno(
                    deviceId, currentTurno.getPostoId(), senha, latitude, longitude);
            iniciarResult.observeForever(new Observer<Resource<Turno>>() {
                @Override
                public void onChanged(Resource<Turno> resource) {
                    iniciarResult.removeObserver(this);
                    isActionLoading.setValue(false);
                    if (resource != null && resource.isSuccess()) {
                        if (resource.getData() != null) {
                            aplicarTurno(resource.getData());
                        }
                    }
                }
            });
        }
    }

    public LiveData<TurnoState> getTurnoState() { return turnoState; }
    public LiveData<String> getTempoRestante() { return tempoRestante; }
    public LiveData<String> getPostoNome() { return postoNome; }
    public LiveData<String> getInicioPrevisto() { return inicioPrevisto; }
    public LiveData<String> getUserNome() { return userNome; }
    public LiveData<String> getUserRole() { return userRole; }
    public LiveData<Boolean> getIsOnline() { return networkMonitor.isOnline(); }
    public LiveData<Boolean> getIsActionLoading() { return isActionLoading; }
    public Turno getCurrentTurno() { return currentTurno; }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) timer.cancel();
        if (turnoAtivoObservable != null) {
            turnoAtivoObservable.removeObserver(onTurnoAtivoLoaded);
        }
    }
}
