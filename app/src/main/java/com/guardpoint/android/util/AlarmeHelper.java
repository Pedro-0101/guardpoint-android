package com.guardpoint.android.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.guardpoint.android.data.local.db.entity.TurnoAtivo;
import com.guardpoint.android.service.CheckinAlarmReceiver;

import timber.log.Timber;

public final class AlarmeHelper {

    private AlarmeHelper() {
    }

    public static void agendarAlarme(Context context, TurnoAtivo turnoAtivo) {
        if (turnoAtivo == null) return;

        long deadline = turnoAtivo.ultimoCheckinMillis
                + (turnoAtivo.intervaloMinutos * 60L * 1000L);
        long alarmTime = deadline - (Constants.ALARM_THRESHOLD_MINUTES * 60L * 1000L);

        if (alarmTime <= System.currentTimeMillis()) {
            Timber.d("AlarmeHelper: deadline muito próximo, alarme não agendado. Restam %d ms",
                    deadline - System.currentTimeMillis());
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, CheckinAlarmReceiver.class);
        intent.setAction(Constants.ACTION_CHECKIN_ALARM);
        intent.putExtra(Constants.EXTRA_TURNO_ID, turnoAtivo.turnoId);
        intent.putExtra(Constants.EXTRA_POSTO_NOME, turnoAtivo.postoNome);
        intent.putExtra(Constants.EXTRA_INTERVALO_MINUTOS, turnoAtivo.intervaloMinutos);
        intent.putExtra(Constants.EXTRA_ULTIMO_CHECKIN, turnoAtivo.ultimoCheckinMillis);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                Constants.ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        boolean exato = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                exato = true;
            }
        } else {
            exato = true;
        }

        try {
            if (exato) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime,
                        pendingIntent
                );
                Timber.d("AlarmeHelper: alarme exato agendado para %d ms (%d min antes do deadline)",
                        alarmTime, Constants.ALARM_THRESHOLD_MINUTES);
            } else {
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime,
                        pendingIntent
                );
                Timber.w("AlarmeHelper: alarme INEXATO agendado (sem SCHEDULE_EXACT_ALARM) para %d ms",
                        alarmTime);
            }
        } catch (SecurityException e) {
            Timber.w(e, "AlarmeHelper: SecurityException ao agendar alarme exato, "
                    + "tentando fallback inexato");
            try {
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime,
                        pendingIntent
                );
                Timber.w("AlarmeHelper: alarme INEXATO agendado após SecurityException para %d ms",
                        alarmTime);
            } catch (SecurityException e2) {
                Timber.e(e2, "AlarmeHelper: falha total ao agendar alarme");
            }
        }
    }

    public static void cancelarAlarme(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, CheckinAlarmReceiver.class);
        intent.setAction(Constants.ACTION_CHECKIN_ALARM);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                Constants.ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Timber.d("AlarmeHelper: alarme cancelado");
        }
    }

    public static boolean isAlarmeAgendado(Context context) {
        Intent intent = new Intent(context, CheckinAlarmReceiver.class);
        intent.setAction(Constants.ACTION_CHECKIN_ALARM);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                Constants.ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        return pendingIntent != null;
    }
}
