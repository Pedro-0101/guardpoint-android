package com.guardpoint.android.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.guardpoint.android.data.local.db.AppDatabase;
import com.guardpoint.android.data.local.db.entity.CheckinPendente;
import com.guardpoint.android.data.remote.api.GuardPointApi;
import com.guardpoint.android.data.remote.dto.CheckinRequest;
import com.guardpoint.android.data.remote.dto.GenericResponse;
import com.guardpoint.android.data.remote.dto.LoteCheckinRequest;
import com.guardpoint.android.util.Constants;
import com.guardpoint.android.util.NetworkMonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import retrofit2.Response;

@HiltWorker
public class SyncWorker extends Worker {

    private final GuardPointApi api;
    private final AppDatabase database;
    private final NetworkMonitor networkMonitor;

    @AssistedInject
    public SyncWorker(@Assisted @NonNull Context context,
                      @Assisted @NonNull WorkerParameters workerParams,
                      GuardPointApi api,
                      AppDatabase database,
                      NetworkMonitor networkMonitor) {
        super(context, workerParams);
        this.api = api;
        this.database = database;
        this.networkMonitor = networkMonitor;
    }

    @NonNull
    @Override
    public Result doWork() {
        if (!networkMonitor.isCurrentlyOnline()) {
            return Result.retry();
        }

        List<CheckinPendente> pendentes = database.checkinDao().getAllPendentes();
        if (pendentes.isEmpty()) return Result.success();

        List<CheckinRequest> requests = new ArrayList<>();
        List<Long> ids = new ArrayList<>();

        for (CheckinPendente p : pendentes) {
            CheckinRequest request = new CheckinRequest(
                    p.turnoId, p.latitude, p.longitude,
                    p.senha, p.tipoSenha, p.timestampCriacao,
                    p.clienteCheckinId
            );
            requests.add(request);
            ids.add(p.id);
        }

        for (long id : ids) {
            database.checkinDao().incrementTentativas(id);
        }

        try {
            LoteCheckinRequest lote = new LoteCheckinRequest(requests);
            Response<GenericResponse> response = api.enviarLote(lote).execute();

            if (response.isSuccessful()) {
                database.checkinDao().deleteByIds(ids);
                return Result.success();
            }

            if (response.code() == 401 || response.code() == 403) {
                return Result.retry();
            }

            if (response.code() >= 400 && response.code() < 500) {
                List<Long> idsToDelete = new ArrayList<>();
                for (CheckinPendente p : pendentes) {
                    if (p.isCritical()) {
                        database.checkinDao().updateStatus(p.id, CheckinPendente.STATUS_ERRO);
                    } else {
                        idsToDelete.add(p.id);
                    }
                }
                if (!idsToDelete.isEmpty()) {
                    database.checkinDao().deleteByIds(idsToDelete);
                }
                return Result.success();
            }

            return Result.retry();
        } catch (IOException e) {
            if (isStopped()) return Result.failure();
            return Result.retry();
        }
    }

    public static void agendarSincronizacao(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL,
                        Constants.LOCATION_INTERVAL_MS, TimeUnit.MILLISECONDS)
                .addTag(Constants.WORK_TAG_SYNC)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(Constants.WORK_TAG_SYNC, ExistingWorkPolicy.REPLACE, workRequest);
    }

    public static void cancelarSincronizacao(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(Constants.WORK_TAG_SYNC);
    }
}
