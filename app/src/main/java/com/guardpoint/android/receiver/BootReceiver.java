package com.guardpoint.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.guardpoint.android.data.local.db.dao.TurnoDao;
import com.guardpoint.android.data.local.db.entity.TurnoAtivo;
import com.guardpoint.android.service.GuardPointForegroundService;
import com.guardpoint.android.worker.BootRestartWorker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BootReceiver extends BroadcastReceiver {

    @Inject
    TurnoDao turnoDao;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            PendingResult pendingResult = goAsync();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    TurnoAtivo turno = turnoDao.getTurnoAtivo();
                    if (turno != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(BootRestartWorker.class)
                                    .build();
                            WorkManager.getInstance(context).enqueue(workRequest);
                        } else {
                            Intent serviceIntent = new Intent(context, GuardPointForegroundService.class);
                            ContextCompat.startForegroundService(context, serviceIntent);
                        }
                    }
                } finally {
                    pendingResult.finish();
                    executor.shutdown();
                }
            });
        }
    }
}
