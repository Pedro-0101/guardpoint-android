package com.guardpoint.android.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.guardpoint.android.R;
import com.guardpoint.android.ui.home.HomeActivity;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class NotificationHelper {

    private final Context context;
    private final NotificationManager notificationManager;

    @Inject
    public NotificationHelper(@ApplicationContext Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        criarCanais();
    }

    private void criarCanais() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    Constants.CHANNEL_SERVICE,
                    context.getString(R.string.notification_channel_service),
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription(context.getString(R.string.notification_channel_service_desc));
            serviceChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(serviceChannel);

            NotificationChannel alertsChannel = new NotificationChannel(
                    Constants.CHANNEL_ALERTS,
                    context.getString(R.string.notification_channel_alerts),
                    NotificationManager.IMPORTANCE_HIGH
            );
            alertsChannel.setDescription(context.getString(R.string.notification_channel_alerts_desc));
            alertsChannel.enableVibration(true);
            notificationManager.createNotificationChannel(alertsChannel);
        }
    }

    public Notification buildForegroundNotification(String tempoRestante) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String contentText = context.getString(R.string.notification_service_active, tempoRestante);

        return new NotificationCompat.Builder(context, Constants.CHANNEL_SERVICE)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .build();
    }

    public Notification buildAlertNotification(String title, String message) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(context, Constants.CHANNEL_ALERTS)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
    }

    public void notifyAlert(int id, String title, String message) {
        Notification notification = buildAlertNotification(title, message);
        notificationManager.notify(id, notification);
    }

    public void updateForegroundNotification(int id, Notification notification) {
        notificationManager.notify(id, notification);
    }

    public void cancelNotification(int id) {
        notificationManager.cancel(id);
    }
}
