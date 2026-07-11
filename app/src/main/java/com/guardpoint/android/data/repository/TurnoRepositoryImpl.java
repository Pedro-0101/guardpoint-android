package com.guardpoint.android.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.guardpoint.android.data.remote.api.GuardPointApi;
import com.guardpoint.android.data.remote.dto.CheckinRequest;
import com.guardpoint.android.data.remote.dto.CheckinResponse;
import com.guardpoint.android.data.remote.dto.FinalizarTurnoRequest;
import com.guardpoint.android.data.remote.dto.IniciarTurnoRequest;
import com.guardpoint.android.data.remote.dto.TurnoListResponse;
import com.guardpoint.android.data.remote.dto.TurnoResponse;
import com.guardpoint.android.data.remote.dto.TurnoStatusResponse;
import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.model.Turno;
import com.guardpoint.android.domain.repository.TurnoRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class TurnoRepositoryImpl implements TurnoRepository {

    private final GuardPointApi api;

    @Inject
    public TurnoRepositoryImpl(GuardPointApi api) {
        this.api = api;
    }

    @Override
    public LiveData<Resource<Turno>> getTurnoAtivo() {
        MutableLiveData<Resource<Turno>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        api.getStatusTurno().enqueue(new Callback<TurnoStatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<TurnoStatusResponse> call, @NonNull Response<TurnoStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getTurno() != null) {
                    result.setValue(Resource.success(mapToDomain(response.body().getTurno())));
                } else {
                    buscarProximoTurno(result);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TurnoStatusResponse> call, @NonNull Throwable t) {
                buscarProximoTurno(result);
            }
        });

        return result;
    }

    @Override
    public LiveData<Resource<Turno>> iniciarTurno(String deviceId, String postoId, String senha,
                                                   double latitude, double longitude) {
        MutableLiveData<Resource<Turno>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        IniciarTurnoRequest request = new IniciarTurnoRequest(deviceId, latitude, longitude, postoId, senha, null);
        api.iniciarTurno(request).enqueue(new Callback<TurnoResponse>() {
            @Override
            public void onResponse(@NonNull Call<TurnoResponse> call, @NonNull Response<TurnoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(mapToDomain(response.body())));
                } else {
                    result.setValue(Resource.error("Erro ao iniciar turno"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<TurnoResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Erro de conexão ao iniciar turno"));
            }
        });

        return result;
    }

    @Override
    public LiveData<Resource<CheckinResponse>> realizarCheckin(String deviceId, String turnoId, String senha,
                                                                double latitude, double longitude) {
        MutableLiveData<Resource<CheckinResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date());
        CheckinRequest request = new CheckinRequest(deviceId, latitude, longitude, senha, timestamp, turnoId, null);
        api.realizarCheckin(request).enqueue(new Callback<CheckinResponse>() {
            @Override
            public void onResponse(@NonNull Call<CheckinResponse> call, @NonNull Response<CheckinResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    result.setValue(Resource.error("Erro ao enviar check-in"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<CheckinResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Erro de conexão ao enviar check-in"));
            }
        });

        return result;
    }

    @Override
    public LiveData<Resource<Turno>> finalizarTurno(String deviceId, String turnoId, String senha,
                                                     double latitude, double longitude) {
        MutableLiveData<Resource<Turno>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date());
        FinalizarTurnoRequest request = new FinalizarTurnoRequest(deviceId, latitude, longitude, senha, timestamp, turnoId);
        api.finalizarTurno(request).enqueue(new Callback<TurnoResponse>() {
            @Override
            public void onResponse(@NonNull Call<TurnoResponse> call, @NonNull Response<TurnoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(mapToDomain(response.body())));
                } else {
                    result.setValue(Resource.error("Erro ao finalizar turno"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<TurnoResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Erro de conexão ao finalizar turno"));
            }
        });

        return result;
    }

    private void buscarProximoTurno(MutableLiveData<Resource<Turno>> result) {
        api.getTurnos("agendado").enqueue(new Callback<TurnoListResponse>() {
            @Override
            public void onResponse(@NonNull Call<TurnoListResponse> call, @NonNull Response<TurnoListResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    result.setValue(Resource.success(mapToDomain(response.body().getData().get(0))));
                } else {
                    result.setValue(Resource.error("Nenhum turno agendado"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<TurnoListResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Erro ao buscar turnos"));
            }
        });
    }

    private Turno mapToDomain(TurnoResponse response) {
        return new Turno(
                response.getTurnoId(),
                response.getPostoId(),
                response.getPostoNome(),
                response.getIntervaloMinutos(),
                response.getTokenSessao(),
                response.getStatus(),
                System.currentTimeMillis(),
                parseIso8601(response.getInicioPrevisto())
        );
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
}
