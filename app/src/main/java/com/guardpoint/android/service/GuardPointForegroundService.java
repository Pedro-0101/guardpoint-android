package com.guardpoint.android.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.guardpoint.android.util.Constants;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GuardPointForegroundService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        criarCanalNotificacao();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = buildNotification();
        startForeground(Constants.NOTIFICATION_ID_SERVICE, notification);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void criarCanalNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Constants.CHANNEL_SERVICE,
                    "GuardPoint Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Canal para o serviço de primeiro plano do GuardPoint");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, Constants.CHANNEL_SERVICE)
                .setContentTitle("GuardPoint")
                .setContentText("GuardPoint ativo")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .build();
    }
}
