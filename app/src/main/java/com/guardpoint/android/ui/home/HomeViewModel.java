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

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final TurnoRepository turnoRepository;
    private final NetworkMonitor networkMonitor;

    private final MutableLiveData<String> tempoRestante = new MutableLiveData<>();
    private final MutableLiveData<Boolean> turnoAtivo = new MutableLiveData<>(false);
    private final MutableLiveData<String> postoNome = new MutableLiveData<>();
    private final MutableLiveData<String> userNome = new MutableLiveData<>();
    private final MutableLiveData<String> userRole = new MutableLiveData<>();

    private LiveData<Resource<Turno>> turnoAtivoObservable;
    private Observer<Resource<Turno>> onTurnoAtivoLoaded;

    private java.util.Timer timer;

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
                aplicarTurnoAtivo(resource.getData());
                if (turnoAtivoObservable != null) {
                    turnoAtivoObservable.removeObserver(onTurnoAtivoLoaded);
                }
            } else if (resource.isError()) {
                if ("Nenhum turno agendado".equals(resource.getMessage())) {
                    postoNome.postValue("Nenhum turno agendado");
                }
                if (turnoAtivoObservable != null) {
                    turnoAtivoObservable.removeObserver(onTurnoAtivoLoaded);
                }
            }
        };
    }

    private void aplicarTurnoAtivo(Turno turno) {
        turnoAtivo.postValue(true);
        postoNome.postValue(turno.getPostoNome());
        iniciarTimer(turno);
    }

    private void iniciarTimer(Turno turno) {
        if (timer != null) timer.cancel();
        timer = new java.util.Timer();
        timer.scheduleAtFixedRate(new java.util.TimerTask() {
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
        turnoAtivoObservable = turnoRepository.getTurnoAtivo();
        turnoAtivoObservable.observeForever(onTurnoAtivoLoaded);
    }

    public LiveData<String> getTempoRestante() { return tempoRestante; }
    public LiveData<Boolean> getTurnoAtivo() { return turnoAtivo; }
    public LiveData<String> getPostoNome() { return postoNome; }
    public LiveData<String> getUserNome() { return userNome; }
    public LiveData<String> getUserRole() { return userRole; }
    public LiveData<Boolean> getIsOnline() { return networkMonitor.isOnline(); }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) timer.cancel();
        if (turnoAtivoObservable != null) {
            turnoAtivoObservable.removeObserver(onTurnoAtivoLoaded);
        }
    }
}
