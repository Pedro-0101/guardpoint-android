package com.guardpoint.android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.guardpoint.android.R;
import com.guardpoint.android.util.Constants;
import com.guardpoint.android.util.NetworkMonitor;
import com.guardpoint.android.util.NotificationHelper;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

@AndroidEntryPoint
public class CheckinAlarmReceiver extends BroadcastReceiver {

    @Inject
    NotificationHelper notificationHelper;

    @Inject
    NetworkMonitor networkMonitor;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Constants.ACTION_CHECKIN_ALARM.equals(intent.getAction())) {
            return;
        }

        Timber.d("CheckinAlarmReceiver disparado: ação %s", intent.getAction());

        String postoNome = intent.getStringExtra(Constants.EXTRA_POSTO_NOME);

        boolean online = networkMonitor.isCurrentlyOnline();

        if (online) {
            notificationHelper.notifyAlert(
                    Constants.NOTIFICATION_ID_ALERT,
                    context.getString(R.string.notification_checkin_alert_title),
                    context.getString(R.string.notification_checkin_alert_message)
            );
        } else {
            notificationHelper.notifyAlert(
                    Constants.NOTIFICATION_ID_ALERT,
                    context.getString(R.string.notification_no_network_title),
                    context.getString(R.string.notification_no_network_message)
            );
        }
    }
}
