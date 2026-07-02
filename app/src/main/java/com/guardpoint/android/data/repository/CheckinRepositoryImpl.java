package com.guardpoint.android.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.guardpoint.android.data.local.db.dao.CheckinDao;
import com.guardpoint.android.data.local.db.dao.TurnoDao;
import com.guardpoint.android.data.local.db.entity.CheckinPendente;
import com.guardpoint.android.data.local.db.entity.TurnoAtivo;
import com.guardpoint.android.data.remote.api.GuardPointApi;
import com.guardpoint.android.data.remote.dto.CheckinRequest;
import com.guardpoint.android.data.remote.dto.TurnoResponse;
import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.model.Turno;
import com.guardpoint.android.domain.repository.CheckinRepository;
import com.guardpoint.android.service.SyncWorker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class CheckinRepositoryImpl implements CheckinRepository {

    private final GuardPointApi api;
    private final CheckinDao checkinDao;
    private final TurnoDao turnoDao;
    private final Context appContext;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public CheckinRepositoryImpl(GuardPointApi api, CheckinDao checkinDao, TurnoDao turnoDao,
                                 @ApplicationContext Context appContext) {
        this.api = api;
        this.checkinDao = checkinDao;
        this.turnoDao = turnoDao;
        this.appContext = appContext;
    }

    @Override
    public LiveData<Resource<Turno>> realizarCheckin(String turnoId, String senha, String tipoSenha,
                                                     double latitude, double longitude) {
        MutableLiveData<Resource<Turno>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        String timestamp = java.time.Instant.now().toString();
        CheckinRequest request = new CheckinRequest(turnoId, latitude, longitude, senha, tipoSenha, timestamp);

        api.checkin(request).enqueue(new Callback<TurnoResponse>() {
            @Override
            public void onResponse(@NonNull Call<TurnoResponse> call, @NonNull Response<TurnoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TurnoResponse body = response.body();
                    Turno turno = new Turno(
                            body.getTurnoId(),
                            body.getPostoId(),
                            body.getPostoNome(),
                            body.getIntervaloMinutos(),
                            body.getTokenSessao(),
                            body.getStatus(),
                            System.currentTimeMillis()
                    );

                    executor.execute(() -> {
                        TurnoAtivo entity = turnoDao.getTurnoAtivo();
                        if (entity != null) {
                            entity.ultimoCheckinMillis = System.currentTimeMillis();
                            entity.status = body.getStatus();
                            turnoDao.update(entity);
                        }
                    });

                    result.setValue(Resource.success(turno));
                } else {
                    result.setValue(Resource.error("Falha no check-in"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<TurnoResponse> call, @NonNull Throwable t) {
                salvarCheckinPendente(turnoId, senha, tipoSenha, latitude, longitude);
                SyncWorker.agendarSincronizacao(appContext);
                result.setValue(Resource.offlineSaved());
            }
        });

        return result;
    }

    @Override
    public void salvarCheckinPendente(String turnoId, String senha, String tipoSenha,
                                      double latitude, double longitude) {
        executor.execute(() -> {
            CheckinPendente pendente = new CheckinPendente();
            pendente.turnoId = turnoId;
            pendente.latitude = latitude;
            pendente.longitude = longitude;
            pendente.timestampCriacao = java.time.Instant.now().toString();
            pendente.senha = senha;
            pendente.tipoSenha = tipoSenha;
            pendente.tentativasEnvio = 0;
            checkinDao.insert(pendente);
        });
    }
}
