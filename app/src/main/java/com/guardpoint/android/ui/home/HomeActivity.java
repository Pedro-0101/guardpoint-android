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

import com.guardpoint.android.R;
import com.guardpoint.android.ui.comum.SenhaVigiaCardView;
import com.guardpoint.android.util.ThemeManager;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {

    private HomeViewModel viewModel;

    private TextView tvWelcome;
    private TextView tvRole;
    private TextView tvPostoNome;
    private TextView tvStatusTurno;
    private TextView tvOfflineIndicator;

    private LinearLayout layoutTimer;
    private TextView tvTimerLabel;
    private TextView tvTimer;

    private LinearLayout layoutInicio;
    private TextView tvInicio;

    private SenhaVigiaCardView senhaVigiaCard;
    private ProgressBar progressBar;

    private String deviceId;

    private final ActivityResultLauncher<String> requestLocationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        bindViews();
        observeViewModel();
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

        senhaVigiaCard = findViewById(R.id.senhaVigiaCard);
        progressBar = findViewById(R.id.progressBar);
    }

    private void observeViewModel() {
        viewModel.getTurnoState().observe(this, state -> {
            layoutTimer.setVisibility(View.GONE);
            layoutInicio.setVisibility(View.GONE);
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
                    senhaVigiaCard.setAcao(getString(R.string.acao_iniciar_turno));
                    senhaVigiaCard.setVisibility(View.VISIBLE);
                    break;

                case IN_PROGRESS:
                    tvStatusTurno.setText(R.string.home_turno_em_andamento);
                    layoutTimer.setVisibility(View.VISIBLE);
                    break;

                case NONE:
                    tvStatusTurno.setText(R.string.home_sem_turno);
                    tvPostoNome.setText("");
                    break;
            }
        });

        viewModel.getTempoRestante().observe(this, tempo -> {
            if (tempo != null) tvTimer.setText(tempo);
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
            senhaVigiaCard.setEnabled(!Boolean.TRUE.equals(loading));
            progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
        });
    }

    private void setupPasswordCard() {
        senhaVigiaCard.setOnEnviarListener(v -> {
            String senha = senhaVigiaCard.getSenha();
            viewModel.executarAcao(deviceId, 0.0, 0.0, senha);
            senhaVigiaCard.mostrarSucesso();
        });
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
