package com.guardpoint.android.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.guardpoint.android.data.remote.dto.CheckinResponse;
import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.model.Turno;
import com.guardpoint.android.domain.repository.TurnoRepository;
import com.guardpoint.android.util.NetworkMonitor;

import timber.log.Timber;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
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

    public enum AcaoType {
        INICIAR_TURNO,
        ENVIAR_CHECKIN,
        FINALIZAR_TURNO
    }

    private final TurnoRepository turnoRepository;
    private final NetworkMonitor networkMonitor;
    private final com.guardpoint.android.data.local.prefs.SecurePrefs securePrefs;

    private final MutableLiveData<TurnoState> turnoState = new MutableLiveData<>(TurnoState.LOADING);
    private final MutableLiveData<String> tempoRestante = new MutableLiveData<>();
    private final MutableLiveData<String> postoNome = new MutableLiveData<>();
    private final MutableLiveData<String> inicioPrevisto = new MutableLiveData<>();
    private final MutableLiveData<String> dataPrevista = new MutableLiveData<>();
    private final MutableLiveData<String> userNome = new MutableLiveData<>();
    private final MutableLiveData<String> userRole = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isActionLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> acaoMensagem = new MutableLiveData<>();
    private final MutableLiveData<AcaoType> acaoType = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> isProximoFinalizar = new MutableLiveData<>(false);

    private Turno currentTurno;
    private LiveData<Resource<Turno>> turnoAtivoObservable;
    private Observer<Resource<Turno>> onTurnoAtivoLoaded;
    private Timer timer;

    @Inject
    public HomeViewModel(TurnoRepository turnoRepository,
                         NetworkMonitor networkMonitor,
                         com.guardpoint.android.data.local.prefs.SecurePrefs securePrefs) {
        this.turnoRepository = turnoRepository;
        this.networkMonitor = networkMonitor;
        this.securePrefs = securePrefs;

        userNome.setValue(securePrefs.getUserNome());
        userRole.setValue(securePrefs.getUserRole());
        postoNome.setValue(securePrefs.getPostoNome());

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
        String nomePosto = turno.getPostoNome();
        if (nomePosto != null) {
            postoNome.postValue(nomePosto);
            securePrefs.savePostoNome(nomePosto);
        }

        Timber.i("Turno aplicado: id=%s, status=%s, posto=%s, intervalo=%d, deadline=%d, tipo=%s",
                turno.getTurnoId(), turno.getStatus(), turno.getPostoNome(),
                turno.getIntervaloMinutos(), turno.getDeadlineMillis(),
                turno.getTipoProximoDeadline());

        if ("em_andamento".equals(turno.getStatus())) {
            turnoState.postValue(TurnoState.IN_PROGRESS);
            isProximoFinalizar.postValue(turno.isProximoFinalizar());
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
                dataPrevista.postValue(formatarData(inicioPrev));
            }
        }
    }

    private void iniciarTimer(Turno turno) {
        if (timer != null) timer.cancel();
        Timber.i("Timer iniciado: deadline=%d, restante=%dms",
                turno.getDeadlineMillis(), turno.getTempoRestanteMillis());
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
                if (horas > 0) {
                    tempoRestante.postValue(String.format("%02d:%02d", horas, minutos));
                } else {
                    long segundos = (resto / 1000) % 60;
                    tempoRestante.postValue(String.format("%02d:%02d", minutos, segundos));
                }
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
        if (currentTurno == null) {
            Timber.w("executarAcao ignorado: currentTurno nulo");
            return;
        }
        if (isActionLoading.getValue() == Boolean.TRUE) {
            Timber.w("executarAcao ignorado: ja carregando (isActionLoading=true)");
            return;
        }

        AcaoType action = acaoType.getValue();
        if (action == null) {
            Timber.w("executarAcao ignorado: acaoType nulo");
            return;
        }

        Timber.i("executarAcao: %s, deviceId=%s, turnoId=%s", action, deviceId, currentTurno.getTurnoId());
        isActionLoading.setValue(true);

        switch (action) {
            case INICIAR_TURNO:
                iniciarTurno(deviceId, latitude, longitude, senha);
                break;
            case ENVIAR_CHECKIN:
                enviarCheckin(deviceId, latitude, longitude, senha);
                break;
            case FINALIZAR_TURNO:
                finalizarTurno(deviceId, latitude, longitude, senha);
                break;
        }
    }

    private void iniciarTurno(String deviceId, double latitude, double longitude, String senha) {
        Timber.i("iniciarTurno: postoId=%s", currentTurno.getPostoId());
        LiveData<Resource<Turno>> iniciarResult = turnoRepository.iniciarTurno(
                deviceId, currentTurno.getPostoId(), senha, latitude, longitude, currentTurno.getIntervaloMinutos());
        iniciarResult.observeForever(new Observer<Resource<Turno>>() {
            @Override
            public void onChanged(Resource<Turno> resource) {
                if (resource == null || resource.getStatus() == Resource.Status.LOADING) return;
                iniciarResult.removeObserver(this);
                isActionLoading.setValue(false);
                if (resource.isSuccess() && resource.getData() != null) {
                    Timber.i("iniciarTurno sucesso: status=%s", resource.getData().getStatus());
                    aplicarTurno(resource.getData());
                    acaoMensagem.postValue("home_turno_iniciado_sucesso");
                } else if (resource.isError()) {
                    Timber.e("iniciarTurno erro: %s", resource.getMessage());
                    acaoMensagem.postValue(resource.getMessage());
                }
            }
        });
    }

    private void enviarCheckin(String deviceId, double latitude, double longitude, String senha) {
        Timber.i("enviarCheckin: turnoId=%s", currentTurno.getTurnoId());
        LiveData<Resource<CheckinResponse>> checkinResult = turnoRepository.realizarCheckin(
                deviceId, currentTurno.getTurnoId(), senha, latitude, longitude);
        checkinResult.observeForever(new Observer<Resource<CheckinResponse>>() {
            @Override
            public void onChanged(Resource<CheckinResponse> resource) {
                if (resource == null || resource.getStatus() == Resource.Status.LOADING) return;
                checkinResult.removeObserver(this);
                isActionLoading.setValue(false);
                if (resource.isSuccess()) {
                    CheckinResponse response = resource.getData();
                    if (response != null) {
                        long novoDeadline = parseIso8601(response.getProximoDeadline());
                        String tipoProximo = response.getTipoProximoDeadline();
                        Timber.i("enviarCheckin sucesso: deadline=%d, tipo=%s, atrasado=%s",
                                novoDeadline, tipoProximo, response.isAtrasado());
                        if (novoDeadline > 0) {
                            currentTurno = new Turno(
                                    currentTurno.getTurnoId(),
                                    currentTurno.getPostoId(),
                                    currentTurno.getPostoNome(),
                                    currentTurno.getIntervaloMinutos(),
                                    currentTurno.getTokenSessao(),
                                    currentTurno.getStatus(),
                                    System.currentTimeMillis(),
                                    currentTurno.getInicioPrevistoMillis(),
                                    novoDeadline,
                                    tipoProximo
                            );
                            isProximoFinalizar.postValue(currentTurno.isProximoFinalizar());
                            iniciarTimer(currentTurno);
                        }
                        acaoMensagem.postValue("home_checkin_sucesso");
                    }
                } else if (resource.isError()) {
                    Timber.e("enviarCheckin erro: %s", resource.getMessage());
                    acaoMensagem.postValue(resource.getMessage());
                }
            }
        });
    }

    private void finalizarTurno(String deviceId, double latitude, double longitude, String senha) {
        Timber.i("finalizarTurno: turnoId=%s", currentTurno.getTurnoId());
        LiveData<Resource<Turno>> finalizarResult = turnoRepository.finalizarTurno(
                deviceId, currentTurno.getTurnoId(), senha, latitude, longitude);
        finalizarResult.observeForever(new Observer<Resource<Turno>>() {
            @Override
            public void onChanged(Resource<Turno> resource) {
                if (resource == null || resource.getStatus() == Resource.Status.LOADING) return;
                finalizarResult.removeObserver(this);
                isActionLoading.setValue(false);
                if (resource.isSuccess()) {
                    Timber.i("finalizarTurno sucesso");
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                    currentTurno = null;
                    tempoRestante.postValue(null);
                    isProximoFinalizar.postValue(false);
                    turnoState.postValue(TurnoState.NONE);
                    acaoMensagem.postValue("home_turno_finalizado_sucesso");
                } else if (resource.isError()) {
                    Timber.e("finalizarTurno erro: %s", resource.getMessage());
                    acaoMensagem.postValue(resource.getMessage());
                }
            }
        });
    }

    public void setAcaoType(AcaoType type) {
        acaoType.setValue(type);
    }

    public LiveData<TurnoState> getTurnoState() { return turnoState; }
    public LiveData<String> getTempoRestante() { return tempoRestante; }
    public LiveData<String> getPostoNome() { return postoNome; }
    public LiveData<String> getInicioPrevisto() { return inicioPrevisto; }
    public LiveData<String> getDataPrevista() { return dataPrevista; }
    public LiveData<String> getUserNome() { return userNome; }
    public LiveData<String> getUserRole() { return userRole; }
    public LiveData<Boolean> getIsOnline() { return networkMonitor.isOnline(); }
    public LiveData<Boolean> getIsActionLoading() { return isActionLoading; }
    public LiveData<String> getAcaoMensagem() { return acaoMensagem; }
    public LiveData<AcaoType> getAcaoType() { return acaoType; }
    public LiveData<Boolean> getIsProximoFinalizar() { return isProximoFinalizar; }
    public Turno getCurrentTurno() { return currentTurno; }

    private String formatarData(long millis) {
        Calendar hoje = Calendar.getInstance();
        Calendar data = Calendar.getInstance();
        data.setTimeInMillis(millis);

        if (hoje.get(Calendar.YEAR) == data.get(Calendar.YEAR)
                && hoje.get(Calendar.DAY_OF_YEAR) == data.get(Calendar.DAY_OF_YEAR)) {
            return "Hoje";
        }

        Calendar amanha = Calendar.getInstance();
        amanha.add(Calendar.DAY_OF_YEAR, 1);
        if (amanha.get(Calendar.YEAR) == data.get(Calendar.YEAR)
                && amanha.get(Calendar.DAY_OF_YEAR) == data.get(Calendar.DAY_OF_YEAR)) {
            return "Amanh\u00e3";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    private long parseIso8601(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return 0L;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(dateStr.replace("Z", "").replaceAll("[+-]\\d{2}:\\d{2}$", ""));
            return date != null ? date.getTime() : 0L;
        } catch (ParseException e) {
            return 0L;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) timer.cancel();
        if (turnoAtivoObservable != null) {
            turnoAtivoObservable.removeObserver(onTurnoAtivoLoaded);
        }
    }
}
