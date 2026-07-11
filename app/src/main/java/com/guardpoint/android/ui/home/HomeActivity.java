package com.guardpoint.android.ui.home;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.guardpoint.android.R;
import com.guardpoint.android.util.ThemeManager;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {

    private HomeViewModel viewModel;

    private TextView tvWelcome;
    private TextView tvRole;
    private TextView tvPostoNome;
    private TextView tvStatusTurno;
    private TextView tvTimer;
    private TextView tvOfflineIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        bindViews();
        observeViewModel();
        viewModel.carregarTurnoAtivo();
    }

    private void bindViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvRole = findViewById(R.id.tvRole);
        tvPostoNome = findViewById(R.id.tvPostoNome);
        tvStatusTurno = findViewById(R.id.tvStatusTurno);
        tvTimer = findViewById(R.id.tvTimer);
        tvOfflineIndicator = findViewById(R.id.tvOfflineIndicator);
    }

    private void observeViewModel() {
        viewModel.getTurnoAtivo().observe(this, ativo -> {
            if (Boolean.TRUE.equals(ativo)) {
                tvStatusTurno.setText("TURNO EM ANDAMENTO");
            } else {
                tvStatusTurno.setText("PRÓXIMO TURNO");
                tvTimer.setText("--:--");
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

        viewModel.getUserNome().observe(this, nome -> {
            if (nome != null) tvWelcome.setText("Olá, " + nome);
        });

        viewModel.getUserRole().observe(this, role -> {
            if (role != null) tvRole.setText(role.toUpperCase());
        });

        viewModel.getIsOnline().observe(this, online -> {
            tvOfflineIndicator.setVisibility(Boolean.TRUE.equals(online) ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_left);
    }
}
