package com.guardpoint.android.ui.home;

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.model.Turno;
import com.guardpoint.android.domain.repository.TurnoRepository;
import com.guardpoint.android.domain.usecase.IniciarTurnoUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final TurnoRepository turnoRepository;
    private final IniciarTurnoUseCase iniciarTurnoUseCase;

    private final MutableLiveData<Resource<Turno>> turnoState = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> finalizarState = new MutableLiveData<>();
    private final MutableLiveData<String> tempoRestante = new MutableLiveData<>();
    private final MutableLiveData<Boolean> turnoAtivo = new MutableLiveData<>(false);
    private final MutableLiveData<String> postoNome = new MutableLiveData<>();
    private final MutableLiveData<String> statusTurno = new MutableLiveData<>();
    private final MutableLiveData<String> postoIdLiveData = new MutableLiveData<>();

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private Turno turnoAtual;

    private LiveData<Resource<Turno>> turnoAtivoObservable;
    private LiveData<Resource<Turno>> iniciarTurnoObservable;

    private Observer<Resource<Turno>> onTurnoAtivoLoaded;
    private Observer<Resource<Turno>> onTurnoIniciado;

    @Inject
    public HomeViewModel(TurnoRepository turnoRepository, IniciarTurnoUseCase iniciarTurnoUseCase) {
        this.turnoRepository = turnoRepository;
        this.iniciarTurnoUseCase = iniciarTurnoUseCase;

        onTurnoAtivoLoaded = resource -> {
            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                Turno turno = resource.getData();
                turnoAtual = turno;
                turnoAtivo.postValue(true);
                postoNome.postValue(turno.getPostoNome());
                statusTurno.postValue(turno.getStatus());
                postoIdLiveData.postValue(turno.getPostoId());
                iniciarTimer();
            }
            if (turnoAtivoObservable != null) {
                turnoAtivoObservable.removeObserver(onTurnoAtivoLoaded);
            }
        };

        onTurnoIniciado = resource -> {
            turnoState.postValue(resource);

            if (resource != null && resource.isSuccess() && resource.getData() != null) {
                Turno turno = resource.getData();
                turnoAtual = turno;
                turnoAtivo.postValue(true);
                postoNome.postValue(turno.getPostoNome());
                statusTurno.postValue(turno.getStatus());
                postoIdLiveData.postValue(turno.getPostoId());
                iniciarTimer();
            }
            if (iniciarTurnoObservable != null) {
                iniciarTurnoObservable.removeObserver(onTurnoIniciado);
            }
        };
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
                pararTimer();
            }
            result.removeObserver(holder[0]);
        };
        result.observeForever(holder[0]);
    }

    public void atualizarTurnoAposCheckin(Turno turno, long novoUltimoCheckin) {
        this.turnoAtual = turno;
        turnoAtivo.postValue(true);
        postoNome.postValue(turno.getPostoNome());
        statusTurno.postValue(turno.getStatus());
        postoIdLiveData.postValue(turno.getPostoId());
        turnoRepository.atualizarUltimoCheckin(novoUltimoCheckin);
        iniciarTimer();
    }

    private void iniciarTimer() {
        pararTimer();

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (turnoAtual == null) {
                    return;
                }

                long restante = turnoAtual.getTempoRestanteMillis();
                tempoRestante.postValue(formatarTempo(restante));

                if (restante > 0) {
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };

        timerHandler.post(timerRunnable);
    }

    private void pararTimer() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    private String formatarTempo(long millis) {
        long totalSegundos = millis / 1000;
        long horas = totalSegundos / 3600;
        long minutos = (totalSegundos % 3600) / 60;
        long segundos = totalSegundos % 60;

        if (horas > 0) {
            return String.format("%02d:%02d:%02d", horas, minutos, segundos);
        }
        return String.format("%02d:%02d", minutos, segundos);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        pararTimer();
        if (turnoAtivoObservable != null) {
            turnoAtivoObservable.removeObserver(onTurnoAtivoLoaded);
        }
        if (iniciarTurnoObservable != null) {
            iniciarTurnoObservable.removeObserver(onTurnoIniciado);
        }
    }
}
