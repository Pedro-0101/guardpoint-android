package com.guardpoint.android.worker;

import android.content.Context;
import android.content.Intent;
import android.app.ForegroundServiceStartNotAllowedException;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.guardpoint.android.data.local.db.dao.TurnoDao;
import com.guardpoint.android.data.local.db.entity.TurnoAtivo;
import com.guardpoint.android.service.GuardPointForegroundService;

import javax.inject.Inject;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@RequiresApi(api = Build.VERSION_CODES.S)
@HiltWorker
public class BootRestartWorker extends Worker {

    private final TurnoDao turnoDao;

    @AssistedInject
    public BootRestartWorker(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters params,
            TurnoDao turnoDao) {
        super(context, params);
        this.turnoDao = turnoDao;
    }

    @NonNull
    @Override
    public Result doWork() {
        TurnoAtivo turno = turnoDao.getTurnoAtivo();
        if (turno != null) {
            try {
                Intent intent = new Intent(getApplicationContext(), GuardPointForegroundService.class);
                ContextCompat.startForegroundService(getApplicationContext(), intent);
                return Result.success();
            } catch (ForegroundServiceStartNotAllowedException e) {
                return Result.retry();
            }
        }
        return Result.success();
    }
}
