package com.guardpoint.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class NetworkMonitor {

    private final ConnectivityManager connectivityManager;
    private final MutableLiveData<Boolean> isOnline = new MutableLiveData<>();
    private final ConnectivityManager.NetworkCallback networkCallback;

    @Inject
    public NetworkMonitor(@ApplicationContext Context context) {
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                isOnline.postValue(true);
            }

            @Override
            public void onLost(Network network) {
                isOnline.postValue(false);
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                boolean hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                isOnline.postValue(hasInternet);
            }
        };

        checkInitialState();
    }

    private void checkInitialState() {
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(network);
            if (caps != null) {
                isOnline.setValue(caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET));
            } else {
                isOnline.setValue(false);
            }
        } else {
            isOnline.setValue(false);
        }
    }

    public void startMonitoring() {
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    public void stopMonitoring() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public LiveData<Boolean> isOnline() {
        return isOnline;
    }

    public boolean isCurrentlyOnline() {
        Boolean value = isOnline.getValue();
        return value != null && value;
    }
}
