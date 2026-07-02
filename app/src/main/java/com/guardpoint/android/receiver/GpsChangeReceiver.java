package com.guardpoint.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

import com.guardpoint.android.data.local.db.dao.TurnoDao;
import com.guardpoint.android.data.local.db.entity.TurnoAtivo;
import com.guardpoint.android.ui.alerta.BloqueioActivity;
import com.guardpoint.android.util.NotificationHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class GpsChangeReceiver extends BroadcastReceiver {

    private final TurnoDao turnoDao;
    private final NotificationHelper notificationHelper;

    public GpsChangeReceiver(TurnoDao turnoDao, NotificationHelper notificationHelper) {
        this.turnoDao = turnoDao;
        this.notificationHelper = notificationHelper;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        if (!LocationManager.PROVIDERS_CHANGED_ACTION.equals(action)
                && !LocationManager.MODE_CHANGED_ACTION.equals(action)) {
            return;
        }

        Timber.d("GpsChangeReceiver: recebido %s", action);

        PendingResult pendingResult = goAsync();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                TurnoAtivo turno = turnoDao.getTurnoAtivo();
                if (turno == null) {
                    Timber.d("GpsChangeReceiver: sem turno ativo, ignorando");
                    return;
                }

                boolean gpsAtivo = isGpsEnabled(context);
                if (gpsAtivo) {
                    Timber.d("GpsChangeReceiver: GPS foi reativado");
                    return;
                }

                Timber.w("GpsChangeReceiver: GPS desligado durante turno ativo! Abrindo BloqueioActivity");

                notificationHelper.notifySabotagem();

                Intent bloqueioIntent = new Intent(context, BloqueioActivity.class);
                bloqueioIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                bloqueioIntent.putExtra(BloqueioActivity.EXTRA_TURNO_ID, turno.turnoId);
                bloqueioIntent.putExtra(BloqueioActivity.EXTRA_POSTO_NOME, turno.postoNome);
                context.startActivity(bloqueioIntent);

            } finally {
                pendingResult.finish();
                executor.shutdown();
            }
        });
    }

    private boolean isGpsEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (lm != null) {
                return lm.isLocationEnabled();
            }
        } else {
            int mode = Settings.Secure.getInt(
                    context.getContentResolver(),
                    Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF
            );
            return mode != Settings.Secure.LOCATION_MODE_OFF;
        }
        return true;
    }
}
