package com.guardpoint.android.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.guardpoint.android.R;
import com.guardpoint.android.ui.comum.SenhaVigiaCardView;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {

    private HomeViewModel viewModel;

    private TextView tvWelcome;
    private TextView tvRole;
    private TextView tvPostoNome;
    private TextView tvStatusTurno;
    private TextView tvOfflineIndicator;
    private TextView tvTimerLabel;

    private LinearLayout layoutTimer;
    private TextView tvTimer;

    private LinearLayout layoutInicio;
    private TextView tvInicio;

    private LinearLayout layoutAcoesTurno;
    private MaterialButton btnCheckin;
    private MaterialButton btnFinalizarTurno;

    private SenhaVigiaCardView senhaVigiaCard;
    private ProgressBar progressBar;

    private String deviceId;
    private FusedLocationProviderClient fusedLocationClient;

    private final ActivityResultLauncher<String> requestLocationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Timber.i("Permissao de localizacao concedida");
                } else {
                    Timber.w("Permissao de localizacao negada");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        bindViews();
        observeViewModel();
        setupActionButtons();
        setupPasswordCard();
        checkLocationPermission();
        viewModel.carregarTurnoAtivo();
    }

    private void bindViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvRole = findViewById(R.id.tvRole);
        tvPostoNome = findViewById(R.id.tvPostoNome);
        tvStatusTurno = findViewById(R.id.tvStatusTurno);
        tvOfflineIndicator = findViewById(R.id.tvOfflineIndicator);

        layoutTimer = findViewById(R.id.layoutTimer);
        tvTimerLabel = findViewById(R.id.tvTimerLabel);
        tvTimer = findViewById(R.id.tvTimer);

        layoutInicio = findViewById(R.id.layoutInicio);
        tvInicio = findViewById(R.id.tvInicio);

        layoutAcoesTurno = findViewById(R.id.layoutAcoesTurno);
        btnCheckin = findViewById(R.id.btnCheckin);
        btnFinalizarTurno = findViewById(R.id.btnFinalizarTurno);

        senhaVigiaCard = findViewById(R.id.senhaVigiaCard);
        progressBar = findViewById(R.id.progressBar);
    }

    private void observeViewModel() {
        viewModel.getTurnoState().observe(this, state -> {
            layoutTimer.setVisibility(View.GONE);
            layoutInicio.setVisibility(View.GONE);
            layoutAcoesTurno.setVisibility(View.GONE);
            senhaVigiaCard.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);

            if (state == null) return;

            switch (state) {
                case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    tvStatusTurno.setText(R.string.home_loading);
                    tvPostoNome.setText("");
                    break;

                case SCHEDULED_FUTURE:
                    tvStatusTurno.setText(R.string.home_turno_agendado);
                    layoutInicio.setVisibility(View.VISIBLE);
                    break;

                case SCHEDULED_READY:
                    tvStatusTurno.setText(R.string.home_turno_atrasado);
                    layoutInicio.setVisibility(View.VISIBLE);
                    viewModel.setAcaoType(HomeViewModel.AcaoType.INICIAR_TURNO);
                    senhaVigiaCard.setAcao(getString(R.string.acao_iniciar_turno));
                    senhaVigiaCard.setVisibility(View.VISIBLE);
                    break;

                case IN_PROGRESS:
                    tvStatusTurno.setText(R.string.home_turno_em_andamento);
                    layoutTimer.setVisibility(View.VISIBLE);
                    layoutAcoesTurno.setVisibility(View.VISIBLE);
                    tvTimerLabel.setText(R.string.home_tempo_proximo_checkin);
                    break;

                case NONE:
                    tvStatusTurno.setText(R.string.home_sem_turno);
                    tvPostoNome.setText("");
                    break;
            }
        });

        viewModel.getTempoRestante().observe(this, tempo -> {
            if (tempo != null) {
                tvTimer.setText(tempo);
            } else {
                tvTimer.setText("--:--");
            }
        });

        viewModel.getPostoNome().observe(this, nome -> {
            if (nome != null) {
                tvPostoNome.setText(nome);
            } else {
                tvPostoNome.setText(R.string.home_loading);
            }
        });

        viewModel.getInicioPrevisto().observe(this, inicio -> {
            if (inicio != null) tvInicio.setText(inicio);
        });

        viewModel.getUserNome().observe(this, nome -> {
            if (nome != null) tvWelcome.setText("Olá, " + nome);
        });

        viewModel.getUserRole().observe(this, role -> {
            if (role != null) tvRole.setText(role.toUpperCase());
        });

        viewModel.getIsOnline().observe(this, online -> {
            tvOfflineIndicator.setVisibility(Boolean.TRUE.equals(online) ? View.GONE : View.VISIBLE);
        });

        viewModel.getIsActionLoading().observe(this, loading -> {
            boolean isLoading = Boolean.TRUE.equals(loading);
            senhaVigiaCard.setLoading(isLoading);
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getAcaoMensagem().observe(this, mensagem -> {
            if (mensagem == null) return;
            if (mensagem.startsWith("home_")) {
                int resId = getResources().getIdentifier(mensagem, "string", getPackageName());
                if (resId != 0) {
                    Snackbar.make(findViewById(android.R.id.content), resId, Snackbar.LENGTH_SHORT).show();
                }
                senhaVigiaCard.setVisibility(View.GONE);
                senhaVigiaCard.resetSenha();
            } else {
                Snackbar.make(findViewById(android.R.id.content), mensagem, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getIsProximoFinalizar().observe(this, isFinalizar -> {
            if (isFinalizar != null && isFinalizar) {
                tvTimerLabel.setText(R.string.home_tempo_finalizar_turno);
                btnCheckin.setVisibility(View.GONE);
                btnFinalizarTurno.setVisibility(View.VISIBLE);
            } else {
                tvTimerLabel.setText(R.string.home_tempo_proximo_checkin);
                btnCheckin.setVisibility(View.VISIBLE);
                btnFinalizarTurno.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getAcaoType().observe(this, type -> {
            if (type == null) return;
            switch (type) {
                case INICIAR_TURNO:
                    senhaVigiaCard.setAcao(getString(R.string.acao_iniciar_turno));
                    break;
                case ENVIAR_CHECKIN:
                    senhaVigiaCard.setAcao(getString(R.string.acao_enviar_checkin));
                    break;
                case FINALIZAR_TURNO:
                    senhaVigiaCard.setAcao(getString(R.string.acao_finalizar_turno));
                    break;
            }
        });
    }

    private void setupActionButtons() {
        btnCheckin.setOnClickListener(v -> {
            viewModel.setAcaoType(HomeViewModel.AcaoType.ENVIAR_CHECKIN);
            senhaVigiaCard.setVisibility(View.VISIBLE);
            senhaVigiaCard.resetSenha();
            senhaVigiaCard.requestFocus();
        });

        btnFinalizarTurno.setOnClickListener(v -> {
            viewModel.setAcaoType(HomeViewModel.AcaoType.FINALIZAR_TURNO);
            senhaVigiaCard.setVisibility(View.VISIBLE);
            senhaVigiaCard.resetSenha();
            senhaVigiaCard.requestFocus();
        });
    }

    private void setupPasswordCard() {
        senhaVigiaCard.setOnEnviarListener(v -> {
            String senha = senhaVigiaCard.getSenha();
            senhaVigiaCard.setEnabled(false);
            obterLocalizacaoEExecutar(senha);
        });
    }

    private void obterLocalizacaoEExecutar(String senha) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Timber.w("Localizacao sem permissao, executando sem coordenadas");
            viewModel.executarAcao(deviceId, 0.0, 0.0, senha);
            senhaVigiaCard.resetSenha();
            senhaVigiaCard.setEnabled(true);
            return;
        }

        fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    senhaVigiaCard.setEnabled(true);
                    if (location != null && isLocalizacaoValida(location.getLatitude(), location.getLongitude())) {
                        Timber.i("Localizacao obtida: lat=%.6f, lon=%.6f", location.getLatitude(), location.getLongitude());
                        viewModel.executarAcao(deviceId, location.getLatitude(), location.getLongitude(), senha);
                    } else {
                        Timber.w("Localizacao invalida ou nula: lat=%.6f, lon=%.6f",
                                location != null ? location.getLatitude() : 0.0,
                                location != null ? location.getLongitude() : 0.0);
                        viewModel.executarAcao(deviceId, 0.0, 0.0, senha);
                    }
                })
                .addOnFailureListener(e -> {
                    senhaVigiaCard.setEnabled(true);
                    Timber.e(e, "Falha ao obter localizacao atual");
                    viewModel.executarAcao(deviceId, 0.0, 0.0, senha);
                });
    }

    private boolean isLocalizacaoValida(double latitude, double longitude) {
        return Math.abs(latitude) > 0.001 || Math.abs(longitude) > 0.001;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_left);
    }
}
