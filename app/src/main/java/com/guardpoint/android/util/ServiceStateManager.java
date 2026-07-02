package com.guardpoint.android.util;

import androidx.lifecycle.MutableLiveData;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ServiceStateManager {

    private final MutableLiveData<Double> currentLatitude = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> currentLongitude = new MutableLiveData<>(0.0);
    private final MutableLiveData<String> tempoRestante = new MutableLiveData<>("--:--");
    private final MutableLiveData<Boolean> serviceRunning = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> gpsEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<Long> ultimoCheckinMillis = new MutableLiveData<>(0L);

    @Inject
    public ServiceStateManager() {
    }

    public MutableLiveData<Double> getCurrentLatitude() {
        return currentLatitude;
    }

    public MutableLiveData<Double> getCurrentLongitude() {
        return currentLongitude;
    }

    public MutableLiveData<String> getTempoRestante() {
        return tempoRestante;
    }

    public MutableLiveData<Boolean> getServiceRunning() {
        return serviceRunning;
    }

    public MutableLiveData<Boolean> getGpsEnabled() {
        return gpsEnabled;
    }

    public MutableLiveData<Long> getUltimoCheckinMillis() {
        return ultimoCheckinMillis;
    }

    public void updateLocation(double latitude, double longitude) {
        currentLatitude.postValue(latitude);
        currentLongitude.postValue(longitude);
    }

    public void updateTempoRestante(String tempo) {
        tempoRestante.postValue(tempo);
    }

    public void setServiceRunning(boolean running) {
        serviceRunning.postValue(running);
    }

    public void setGpsEnabled(boolean enabled) {
        gpsEnabled.postValue(enabled);
    }

    public void setUltimoCheckinMillis(long millis) {
        ultimoCheckinMillis.postValue(millis);
    }
}
