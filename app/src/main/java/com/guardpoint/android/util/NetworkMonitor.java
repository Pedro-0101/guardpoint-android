package com.guardpoint.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class NetworkMonitor {

    private final ConnectivityManager connectivityManager;
    private final MutableLiveData<Boolean> isOnline = new MutableLiveData<>(true);

    public NetworkMonitor(Context context) {
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                isOnline.postValue(true);
            }

            @Override
            public void onLost(@NonNull Network network) {
                isOnline.postValue(false);
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities capabilities) {
                isOnline.postValue(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET));
            }
        });
    }

    public LiveData<Boolean> isOnline() {
        return isOnline;
    }
}
