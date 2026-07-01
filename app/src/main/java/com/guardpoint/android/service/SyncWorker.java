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
import com.guardpoint.android.util.Constants;

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

    @AssistedInject
    public SyncWorker(@Assisted @NonNull Context context,
                      @Assisted @NonNull WorkerParameters workerParams,
                      GuardPointApi api,
                      AppDatabase database) {
        super(context, workerParams);
        this.api = api;
        this.database = database;
    }

    @NonNull
    @Override
    public Result doWork() {
        List<CheckinPendente> pendentes = database.checkinDao().getAllPendentes();
        if (pendentes.isEmpty()) return Result.success();

        List<CheckinRequest> requests = new ArrayList<>();
        List<Long> ids = new ArrayList<>();

        for (CheckinPendente p : pendentes) {
            CheckinRequest request = new CheckinRequest(
                    p.turnoId, p.latitude, p.longitude,
                    p.tipoSenha, p.timestampCriacao
            );
            requests.add(request);
            ids.add(p.id);
        }

        try {
            Response<GenericResponse> response = api.enviarLote(requests).execute();

            if (response.isSuccessful()) {
                database.checkinDao().deleteByIds(ids);
                return Result.success();
            }

            if (response.code() >= 400 && response.code() < 500) {
                database.checkinDao().deleteByIds(ids);
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
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .addTag(Constants.WORK_TAG_SYNC)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(Constants.WORK_TAG_SYNC, ExistingWorkPolicy.REPLACE, workRequest);
    }

    public static void cancelarSincronizacao(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(Constants.WORK_TAG_SYNC);
    }
}
