package com.guardpoint.android.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.guardpoint.android.R;
import com.guardpoint.android.data.local.db.dao.TurnoDao;
import com.guardpoint.android.data.local.db.entity.TurnoAtivo;
import com.guardpoint.android.util.AlarmeHelper;
import com.guardpoint.android.util.Constants;
import com.guardpoint.android.util.NetworkMonitor;
import com.guardpoint.android.util.NotificationHelper;
import com.guardpoint.android.util.ServiceStateManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GuardPointForegroundService extends Service {

    @Inject
    TurnoDao turnoDao;

    @Inject
    ServiceStateManager serviceStateManager;

    @Inject
    NotificationHelper notificationHelper;

    @Inject
    NetworkMonitor networkMonitor;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private TurnoAtivo turnoAtivo;
    private ExecutorService ioExecutor;
    private boolean alertaEnviado = false;

    private final Observer<Long> ultimoCheckinObserver = novoMillis -> {
        if (novoMillis != null && turnoAtivo != null
                && novoMillis > turnoAtivo.ultimoCheckinMillis) {
            turnoAtivo.ultimoCheckinMillis = novoMillis;
            alertaEnviado = false;
            notificationHelper.cancelNotification(Constants.NOTIFICATION_ID_ALERT);
            AlarmeHelper.cancelarAlarme(GuardPointForegroundService.this);
            AlarmeHelper.agendarAlarme(GuardPointForegroundService.this, turnoAtivo);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        ioExecutor = Executors.newSingleThreadExecutor();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        networkMonitor.startMonitoring();
        serviceStateManager.getUltimoCheckinMillis().observeForever(ultimoCheckinObserver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification placeholder = notificationHelper.buildForegroundNotification("--:--");
        startForeground(Constants.NOTIFICATION_ID_SERVICE, placeholder);

        serviceStateManager.setServiceRunning(true);
        startLocationUpdates();

        ioExecutor.execute(() -> {
            TurnoAtivo t = turnoDao.getTurnoAtivo();
            timerHandler.post(() -> {
                this.turnoAtivo = t;
                if (t != null) {
                    Notification notification = notificationHelper.buildForegroundNotification(
                            formatarTempo(calcularTempoRestante()));
                    notificationHelper.updateForegroundNotification(
                            Constants.NOTIFICATION_ID_SERVICE, notification);
                    AlarmeHelper.agendarAlarme(GuardPointForegroundService.this, t);
                    iniciarTimer();
                } else {
                    stopSelf();
                }
            });
        });

        return START_STICKY;
    }

    private long calcularTempoRestante() {
        if (turnoAtivo == null) return 0;
        long deadline = turnoAtivo.ultimoCheckinMillis + (turnoAtivo.intervaloMinutos * 60L * 1000L);
        return Math.max(0, deadline - System.currentTimeMillis());
    }

    @SuppressWarnings("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                Constants.LOCATION_INTERVAL_MS)
                .setMinUpdateIntervalMillis(Constants.LOCATION_FASTEST_INTERVAL_MS)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    serviceStateManager.updateLocation(location.getLatitude(), location.getLongitude());
                    serviceStateManager.setGpsEnabled(true);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        );
    }

    private void iniciarTimer() {
        pararTimer();

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (turnoAtivo == null) {
                    return;
                }

                long restante = calcularTempoRestante();
                String tempoFormatado = formatarTempo(restante);
                serviceStateManager.updateTempoRestante(tempoFormatado);

                Notification notification = notificationHelper.buildForegroundNotification(tempoFormatado);
                notificationHelper.updateForegroundNotification(Constants.NOTIFICATION_ID_SERVICE, notification);

                long cincoMinutosMs = Constants.ALARM_THRESHOLD_MINUTES * 60L * 1000L;

                if (restante > 0 && restante <= cincoMinutosMs && !alertaEnviado) {
                    alertaEnviado = true;
                    dispararAlertaCheckin(restante);
                }

                if (restante > 0) {
                    timerHandler.postDelayed(this, 1000);
                } else {
                    dispararAlertaDeadlinePassado();
                }
            }
        };

        timerHandler.post(timerRunnable);
    }

    private void dispararAlertaCheckin(long restante) {
        boolean online = networkMonitor.isCurrentlyOnline();

        if (online) {
            notificationHelper.notifyAlert(
                    Constants.NOTIFICATION_ID_ALERT,
                    getString(R.string.notification_checkin_alert_title),
                    getString(R.string.notification_checkin_alert_message)
            );
        } else {
            notificationHelper.notifyAlert(
                    Constants.NOTIFICATION_ID_ALERT,
                    getString(R.string.notification_no_network_title),
                    getString(R.string.notification_no_network_message)
            );
        }
    }

    private void dispararAlertaDeadlinePassado() {
        notificationHelper.notifyAlert(
                Constants.NOTIFICATION_ID_ALERT,
                getString(R.string.notification_deadline_passed_title),
                getString(R.string.notification_deadline_passed_message)
        );
    }

    private void pararTimer() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
            timerRunnable = null;
        }
    }

    private String formatarTempo(long millis) {
        long totalSegundos = millis / 1000;
        long horas = totalSegundos / 3600;
        long minutos = (totalSegundos % 3600) / 60;
        long segundos = totalSegundos % 60;

        if (horas > 0) {
            return String.format("%02d:%02d:%02d", horas, minutos, segundos);
        }
        return String.format("%02d:%02d", minutos, segundos);
    }

    @Override
    public void onDestroy() {
        pararTimer();

        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        if (ioExecutor != null) {
            ioExecutor.shutdown();
        }

        AlarmeHelper.cancelarAlarme(this);
        notificationHelper.cancelNotification(Constants.NOTIFICATION_ID_ALERT);
        networkMonitor.stopMonitoring();
        serviceStateManager.getUltimoCheckinMillis().removeObserver(ultimoCheckinObserver);
        serviceStateManager.setServiceRunning(false);
        serviceStateManager.setGpsEnabled(false);
        serviceStateManager.updateTempoRestante("--:--");

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
