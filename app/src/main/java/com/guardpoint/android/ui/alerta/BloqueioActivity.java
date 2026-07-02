package com.guardpoint.android.ui.alerta;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.guardpoint.android.R;
import com.guardpoint.android.service.GuardPointForegroundService;
import com.guardpoint.android.util.ServiceStateManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

@AndroidEntryPoint
public class BloqueioActivity extends AppCompatActivity {

    public static final String EXTRA_TURNO_ID = "extra_turno_id";
    public static final String EXTRA_POSTO_NOME = "extra_posto_nome";
    private static final int LOCATION_SETTINGS_REQUEST = 9001;

    @Inject
    ServiceStateManager serviceStateManager;

    private BloqueioViewModel viewModel;
    private TextView tvTitulo;
    private TextView tvMensagem;
    private TextView tvPostoNome;
    private MaterialButton btnAtivarGps;
    private View pbLoading;

    private String turnoId;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    private FusedLocationProviderClient fusedLocationClient;
    private GpsRestoredReceiver gpsRestoredReceiver;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean sabotagemEnviada = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }

        setContentView(R.layout.activity_bloqueio);

        viewModel = new ViewModelProvider(this).get(BloqueioViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        bindViews();
        processIntent();
        observeViewModel();
        setupGpsRestoredReceiver();

        btnAtivarGps.setOnClickListener(v -> abrirConfiguracoesLocalizacao());

        getUltimaLocalizacao();
    }

    private void bindViews() {
        tvTitulo = findViewById(R.id.tvBloqueioTitulo);
        tvMensagem = findViewById(R.id.tvBloqueioMensagem);
        tvPostoNome = findViewById(R.id.tvBloqueioPostoNome);
        btnAtivarGps = findViewById(R.id.btnAtivarGps);
        pbLoading = findViewById(R.id.pbBloqueioLoading);
    }

    private void processIntent() {
        turnoId = getIntent().getStringExtra(EXTRA_TURNO_ID);
        String postoNome = getIntent().getStringExtra(EXTRA_POSTO_NOME);

        if (postoNome != null && !postoNome.isEmpty()) {
            tvPostoNome.setText(postoNome);
            tvPostoNome.setVisibility(View.VISIBLE);
        }

        if (turnoId != null) {
            viewModel.carregarDadosTurno(turnoId);
        }

        Double lat = serviceStateManager.getCurrentLatitude().getValue();
        Double lon = serviceStateManager.getCurrentLongitude().getValue();
        if (lat != null) currentLatitude = lat;
        if (lon != null) currentLongitude = lon;
    }

    private void observeViewModel() {
        viewModel.getPostoNome().observe(this, nome -> {
            if (nome != null && !nome.isEmpty()) {
                tvPostoNome.setText(nome);
                tvPostoNome.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getSabotagemState().observe(this, resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    pbLoading.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    pbLoading.setVisibility(View.GONE);
                    sabotagemEnviada = true;
                    Timber.d("BloqueioActivity: sabotagem reportada com sucesso");
                    break;
                case ERROR:
                    pbLoading.setVisibility(View.GONE);
                    Timber.w("BloqueioActivity: falha ao reportar sabotagem - %s", resource.getMessage());
                    agendarRetrySabotagem();
                    break;
            }
        });
    }

    @SuppressWarnings("MissingPermission")
    private void getUltimaLocalizacao() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            enviarSabotagem();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                viewModel.atualizarLocalizacao(currentLatitude, currentLongitude);
            }
            enviarSabotagem();
        }).addOnFailureListener(this, e -> {
            Timber.w(e, "BloqueioActivity: falha ao obter última localização");
            enviarSabotagem();
        });
    }

    private void enviarSabotagem() {
        if (turnoId == null || sabotagemEnviada) return;

        viewModel.reportarSabotagem(turnoId, currentLatitude, currentLongitude);
    }

    private void agendarRetrySabotagem() {
        handler.postDelayed(() -> {
            if (!sabotagemEnviada && turnoId != null) {
                Timber.d("BloqueioActivity: retentando envio de sabotagem");
                viewModel.reportarSabotagem(turnoId, currentLatitude, currentLongitude);
            }
        }, 10000);
    }

    private void setupGpsRestoredReceiver() {
        gpsRestoredReceiver = new GpsRestoredReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            filter.addAction(LocationManager.MODE_CHANGED_ACTION);
        }

        registerReceiver(gpsRestoredReceiver, filter);
    }

    private void abrirConfiguracoesLocalizacao() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, LOCATION_SETTINGS_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_SETTINGS_REQUEST) {
            verificarGpsRestaurado();
        }
    }

    private void verificarGpsRestaurado() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) return;

        boolean gpsAtivo;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            gpsAtivo = lm.isLocationEnabled();
        } else {
            int mode = Settings.Secure.getInt(
                    getContentResolver(),
                    Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF
            );
            gpsAtivo = mode != Settings.Secure.LOCATION_MODE_OFF;
        }

        if (gpsAtivo) {
            Toast.makeText(this, R.string.bloqueio_gps_restaurado, Toast.LENGTH_LONG).show();
            handler.postDelayed(this::finalizarBloqueio, 1500);
        }
    }

    private void finalizarBloqueio() {
        if (gpsRestoredReceiver != null) {
            try {
                unregisterReceiver(gpsRestoredReceiver);
            } catch (IllegalArgumentException ignored) {
            }
            gpsRestoredReceiver = null;
        }

        Intent serviceIntent = new Intent(this, GuardPointForegroundService.class);
        ContextCompat.startForegroundService(this, serviceIntent);

        finish();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        verificarGpsRestaurado();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);

        if (gpsRestoredReceiver != null) {
            try {
                unregisterReceiver(gpsRestoredReceiver);
            } catch (IllegalArgumentException ignored) {
            }
            gpsRestoredReceiver = null;
        }
    }

    private class GpsRestoredReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;

            if (!LocationManager.PROVIDERS_CHANGED_ACTION.equals(action)
                    && !LocationManager.MODE_CHANGED_ACTION.equals(action)) {
                return;
            }

            verificarGpsRestaurado();
        }
    }
}
