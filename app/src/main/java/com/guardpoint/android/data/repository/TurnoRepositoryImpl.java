package com.guardpoint.android.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.guardpoint.android.data.remote.api.GuardPointApi;
import com.guardpoint.android.data.remote.dto.TurnoListResponse;
import com.guardpoint.android.data.remote.dto.TurnoResponse;
import com.guardpoint.android.data.remote.dto.TurnoStatusResponse;
import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.model.Turno;
import com.guardpoint.android.domain.repository.TurnoRepository;

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
                System.currentTimeMillis()
        );
    }
}
