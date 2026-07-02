package com.guardpoint.android.ui.home;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.button.MaterialButton;
import com.guardpoint.android.R;
import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.model.Turno;
import com.guardpoint.android.ui.checkin.CheckinActivity;
import com.guardpoint.android.util.Constants;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {

    private HomeViewModel viewModel;

    private MapView mapView;
    private IMapController mapController;
    private Marker locationMarker;

    private TextView tvPostoNome;
    private TextView tvTimer;
    private TextView tvOfflineIndicator;
    private MaterialButton btnIniciarTurno;
    private MaterialButton btnCheckin;
    private MaterialButton btnFinalizarTurno;
    private View pbLoading;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean locationPermissionGranted = false;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fineGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                boolean coarseGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                if (fineGranted || coarseGranted) {
                    locationPermissionGranted = true;
                    startLocationUpdates();
                } else {
                    Toast.makeText(this, R.string.home_location_permission_required, Toast.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<Intent> checkinLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    boolean ok = result.getData().getBooleanExtra(CheckinActivity.EXTRA_CHECKIN_OK, false);
                    if (ok) {
                        viewModel.carregarTurnoAtivo();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_home);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        bindViews();
        setupMap();
        observeViewModel();

        btnIniciarTurno.setOnClickListener(v -> onIniciarTurnoClicked());
        btnCheckin.setOnClickListener(v -> onCheckinClicked());
        btnFinalizarTurno.setOnClickListener(v -> onFinalizarTurnoClicked());

        viewModel.carregarTurnoAtivo();
    }

    private void bindViews() {
        mapView = findViewById(R.id.mapView);
        tvPostoNome = findViewById(R.id.tvPostoNome);
        tvTimer = findViewById(R.id.tvTimer);
        tvOfflineIndicator = findViewById(R.id.tvOfflineIndicator);
        btnIniciarTurno = findViewById(R.id.btnIniciarTurno);
        btnCheckin = findViewById(R.id.btnCheckin);
        btnFinalizarTurno = findViewById(R.id.btnFinalizarTurno);
        pbLoading = findViewById(R.id.pbLoading);
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setTilesScaledToDpi(true);

        CompassOverlay compassOverlay = new CompassOverlay(this, mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        RotationGestureOverlay rotationOverlay = new RotationGestureOverlay(mapView);
        rotationOverlay.setEnabled(true);
        mapView.getOverlays().add(rotationOverlay);

        mapController = mapView.getController();
        mapController.setZoom(16.0);

        locationMarker = new Marker(mapView);
        locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        locationMarker.setTitle(getString(R.string.home_your_location));
        mapView.getOverlays().add(locationMarker);

        requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            startLocationUpdates();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @SuppressWarnings("MissingPermission")
    private void startLocationUpdates() {
        if (!locationPermissionGranted) return;

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                Constants.LOCATION_INTERVAL_MS)
                .setMinUpdateIntervalMillis(Constants.LOCATION_FASTEST_INTERVAL_MS)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    updateMapLocation(currentLatitude, currentLongitude);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                updateMapLocation(currentLatitude, currentLongitude);
            }
        });
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void updateMapLocation(double latitude, double longitude) {
        if (mapView == null) return;

        GeoPoint point = new GeoPoint(latitude, longitude);
        locationMarker.setPosition(point);
        mapController.animateTo(point);
    }

    private void onIniciarTurnoClicked() {
        setLoading(true);

        String postoId = viewModel.getPostoIdLiveData().getValue();
        if (postoId == null || postoId.isEmpty()) {
            postoId = "default";
        }

        viewModel.iniciarTurno(postoId);
    }

    private void onCheckinClicked() {
        Turno turno = viewModel.getTurnoAtual();
        if (turno == null) return;

        double lat = getServiceLatitude();
        double lon = getServiceLongitude();

        Intent intent = new Intent(this, CheckinActivity.class);
        intent.putExtra(CheckinActivity.EXTRA_TURNO_ID, turno.getTurnoId());
        intent.putExtra(CheckinActivity.EXTRA_LATITUDE, lat);
        intent.putExtra(CheckinActivity.EXTRA_LONGITUDE, lon);
        checkinLauncher.launch(intent);
    }

    private void onFinalizarTurnoClicked() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.home_finalize_title)
                .setMessage(R.string.home_finalize_message)
                .setPositiveButton(R.string.home_finalize_confirm, (dialog, which) -> {
                    viewModel.finalizarTurno(getServiceLatitude(), getServiceLongitude());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private double getServiceLatitude() {
        Double lat = viewModel.getLatitude().getValue();
        return lat != null ? lat : currentLatitude;
    }

    private double getServiceLongitude() {
        Double lon = viewModel.getLongitude().getValue();
        return lon != null ? lon : currentLongitude;
    }

    private void setLoading(boolean loading) {
        btnIniciarTurno.setVisibility(loading ? View.GONE : View.VISIBLE);
        pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void observeViewModel() {
        viewModel.getTurnoState().observe(this, resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    onTurnoIniciado(resource.getData());
                    break;
                case ERROR:
                    setLoading(false);
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        });

        viewModel.getTurnoAtivo().observe(this, ativo -> {
            if (Boolean.TRUE.equals(ativo)) {
                btnIniciarTurno.setVisibility(View.GONE);
                btnCheckin.setVisibility(View.VISIBLE);
                btnFinalizarTurno.setVisibility(View.VISIBLE);
                ensureServiceRunning();
            } else {
                btnIniciarTurno.setVisibility(View.VISIBLE);
                btnCheckin.setVisibility(View.GONE);
                btnFinalizarTurno.setVisibility(View.GONE);
            }
        });

        viewModel.getFinalizarState().observe(this, resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case SUCCESS:
                    onTurnoFinalizado();
                    break;
                case ERROR:
                    Toast.makeText(this, resource.getMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        });

        viewModel.getTempoRestante().observe(this, tempo -> {
            if (tempo != null) {
                tvTimer.setText(tempo);
            }
        });

        viewModel.getPostoNome().observe(this, nome -> {
            if (nome != null) {
                tvPostoNome.setText(nome);
            }
        });

        viewModel.getLatitude().observe(this, lat -> {
            if (lat != null && lat != 0.0) {
                currentLatitude = lat;
            }
        });

        viewModel.getLongitude().observe(this, lon -> {
            if (lon != null && lon != 0.0) {
                currentLongitude = lon;
            }
        });

        viewModel.getIsOnline().observe(this, online -> {
            tvOfflineIndicator.setVisibility(Boolean.TRUE.equals(online) ? View.GONE : View.VISIBLE);
        });

        viewModel.getPendentesCount().observe(this, count -> {
            if (count != null && count > 0) {
                tvOfflineIndicator.setText(getString(R.string.home_pending_sync, count));
            } else {
                tvOfflineIndicator.setText(R.string.home_offline_status);
            }
        });
    }

    private void onTurnoIniciado(Turno turno) {
        if (turno == null) return;

        startForegroundService();

        Toast.makeText(this, R.string.home_shift_started, Toast.LENGTH_SHORT).show();

        solicitarPermissaoAlarmeSeNecessario();
    }

    private void startForegroundService() {
        Intent serviceIntent = new Intent(this, com.guardpoint.android.service.GuardPointForegroundService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void ensureServiceRunning() {
        Boolean running = viewModel.getTurnoAtivo().getValue();
        if (Boolean.TRUE.equals(running)) {
            startForegroundService();
            solicitarPermissaoAlarmeSeNecessario();
        }
    }

    private void solicitarPermissaoAlarmeSeNecessario() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager == null) return;

        if (!alarmManager.canScheduleExactAlarms()) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(R.string.home_alarm_permission_title)
                    .setMessage(R.string.home_alarm_permission_message)
                    .setPositiveButton(R.string.home_alarm_permission_positive, (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton(R.string.home_alarm_permission_negative, null)
                    .show();
        }
    }

    private void onTurnoFinalizado() {
        stopLocationUpdates();

        Intent serviceIntent = new Intent(this, com.guardpoint.android.service.GuardPointForegroundService.class);
        stopService(serviceIntent);

        com.guardpoint.android.service.SyncWorker.cancelarSincronizacao(this);

        Toast.makeText(this, R.string.home_shift_ended, Toast.LENGTH_SHORT).show();

        btnIniciarTurno.setVisibility(View.VISIBLE);
        btnCheckin.setVisibility(View.GONE);
        btnFinalizarTurno.setVisibility(View.GONE);
        tvTimer.setText("--:--");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        if (mapView != null) {
            mapView.onDetach();
        }
    }
}
