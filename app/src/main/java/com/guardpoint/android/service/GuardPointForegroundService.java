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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.guardpoint.android.data.local.db.dao.TurnoDao;
import com.guardpoint.android.data.local.db.entity.TurnoAtivo;
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

    @Override
    public void onCreate() {
        super.onCreate();
        ioExecutor = Executors.newSingleThreadExecutor();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        networkMonitor.startMonitoring();
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

                if (restante > 0) {
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };

        timerHandler.post(timerRunnable);
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

        networkMonitor.stopMonitoring();
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
