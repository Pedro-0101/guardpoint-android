package com.guardpoint.android.data.repository;

import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.guardpoint.android.data.local.db.dao.TurnoDao;
import com.guardpoint.android.data.local.db.entity.TurnoAtivo;
import com.guardpoint.android.data.remote.api.GuardPointApi;
import com.guardpoint.android.data.remote.dto.FinalizarTurnoRequest;
import com.guardpoint.android.data.remote.dto.GenericResponse;
import com.guardpoint.android.data.remote.dto.SabotagemRequest;
import com.guardpoint.android.data.remote.dto.TurnoIniciarRequest;
import com.guardpoint.android.data.remote.dto.TurnoResponse;
import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.model.Turno;
import com.guardpoint.android.domain.repository.TurnoRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class TurnoRepositoryImpl implements TurnoRepository {

    private final GuardPointApi api;
    private final TurnoDao turnoDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public TurnoRepositoryImpl(GuardPointApi api, TurnoDao turnoDao) {
        this.api = api;
        this.turnoDao = turnoDao;
    }

    @Override
    public LiveData<Resource<Turno>> iniciarTurno(String postoId, String deviceId) {
        MutableLiveData<Resource<Turno>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        TurnoIniciarRequest request = new TurnoIniciarRequest(postoId, deviceId);
        api.iniciarTurno(request).enqueue(new Callback<TurnoResponse>() {
            @Override
            public void onResponse(@NonNull Call<TurnoResponse> call, @NonNull Response<TurnoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TurnoResponse body = response.body();
                    Turno turno = mapToDomain(body);

                    executor.execute(() -> {
                        TurnoAtivo entity = mapToEntity(body);
                        turnoDao.insert(entity);
                    });

                    result.setValue(Resource.success(turno));
                } else {
                    result.setValue(Resource.error("Falha ao iniciar turno"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<TurnoResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error(t.getMessage() != null ? t.getMessage() : "Erro de conexão"));
            }
        });

        return result;
    }

    @Override
    public LiveData<Resource<Turno>> getTurnoAtivo() {
        MutableLiveData<Resource<Turno>> result = new MutableLiveData<>();

        executor.execute(() -> {
            TurnoAtivo entity = turnoDao.getTurnoAtivo();
            if (entity != null) {
                Turno turno = mapToDomain(entity);
                result.postValue(Resource.success(turno));
            } else {
                result.postValue(Resource.error("Nenhum turno ativo"));
            }
        });

        return result;
    }

    @Override
    public boolean hasTurnoAtivo() {
        return turnoDao.getTurnoAtivo() != null;
    }

    @Override
    public void atualizarUltimoCheckin(long timestampMillis) {
        executor.execute(() -> {
            TurnoAtivo entity = turnoDao.getTurnoAtivo();
            if (entity != null) {
                entity.ultimoCheckinMillis = timestampMillis;
                turnoDao.update(entity);
            }
        });
    }

    @Override
    public LiveData<Resource<Void>> finalizarTurno(String turnoId, double latitude, double longitude, String timestamp) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        FinalizarTurnoRequest request = new FinalizarTurnoRequest(turnoId, latitude, longitude, timestamp);
        api.finalizarTurno(request).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                executor.execute(() -> {
                    turnoDao.deleteByTurnoId(turnoId);
                });

                if (response.isSuccessful()) {
                    result.setValue(Resource.success(null));
                } else {
                    result.setValue(Resource.error("Falha ao finalizar turno"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                executor.execute(() -> {
                    turnoDao.deleteByTurnoId(turnoId);
                });
                result.setValue(Resource.error(t.getMessage() != null ? t.getMessage() : "Erro de conexão"));
            }
        });

        return result;
    }

    @Override
    public LiveData<Resource<Void>> reportarSabotagem(String turnoId, double latitude, double longitude,
                                                      String motivo, String timestamp) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        SabotagemRequest request = new SabotagemRequest(turnoId, latitude, longitude, motivo, timestamp);
        api.sabotagem(request).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                if (response.isSuccessful()) {
                    result.setValue(Resource.success(null));
                } else {
                    result.setValue(Resource.error("Falha ao reportar sabotagem"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error(t.getMessage() != null ? t.getMessage() : "Erro de conexão"));
            }
        });

        return result;
    }

    private Turno mapToDomain(TurnoResponse response) {
        return new Turno(
                response.getTurnoId(),
                response.getPostoId(),
                response.getPostoNome(),
                response.getIntervaloMinutos(),
                response.getTokenSessao(),
                response.getStatus(),
                System.currentTimeMillis()
        );
    }

    private Turno mapToDomain(TurnoAtivo entity) {
        return new Turno(
                entity.turnoId,
                entity.postoId,
                entity.postoNome,
                entity.intervaloMinutos,
                entity.tokenSessao,
                entity.status != null ? entity.status : "em_andamento",
                entity.ultimoCheckinMillis
        );
    }

    private TurnoAtivo mapToEntity(TurnoResponse response) {
        TurnoAtivo entity = new TurnoAtivo();
        entity.turnoId = response.getTurnoId();
        entity.postoId = response.getPostoId();
        entity.postoNome = response.getPostoNome();
        entity.intervaloMinutos = response.getIntervaloMinutos();
        entity.tokenSessao = response.getTokenSessao();
        entity.status = response.getStatus();
        entity.ultimoCheckinMillis = System.currentTimeMillis();
        return entity;
    }
}
