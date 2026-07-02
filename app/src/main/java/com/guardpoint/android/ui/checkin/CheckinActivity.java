package com.guardpoint.android.ui.checkin;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.guardpoint.android.R;
import com.guardpoint.android.domain.model.Turno;

import java.util.concurrent.Executor;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CheckinActivity extends AppCompatActivity {

    public static final String EXTRA_TURNO_ID = "turno_id";
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    public static final String EXTRA_TURNO_RESULT = "turno_result";
    public static final String EXTRA_CHECKIN_OK = "checkin_ok";

    private CheckinViewModel viewModel;

    private TextInputEditText etSenha;
    private TextInputEditText etCoacao;
    private MaterialButton btnConfirmar;
    private View pbLoading;
    private TextView tvError;
    private TextView tvSuccess;
    private View cardCheckin;

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private boolean biometricAvailable;
    private boolean biometricDone = false;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);

        viewModel = new ViewModelProvider(this).get(CheckinViewModel.class);

        String turnoId = getIntent().getStringExtra(EXTRA_TURNO_ID);
        double lat = getIntent().getDoubleExtra(EXTRA_LATITUDE, 0.0);
        double lon = getIntent().getDoubleExtra(EXTRA_LONGITUDE, 0.0);
        viewModel.setDadosTurno(turnoId, lat, lon);

        bindViews();
        observeViewModel();
        setupBiometric();

        btnConfirmar.setOnClickListener(v -> onConfirmarClicked());
        btnConfirmar.setEnabled(false);

        promptBiometric();
    }

    private void bindViews() {
        etSenha = findViewById(R.id.etSenha);
        etCoacao = findViewById(R.id.etCoacao);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        pbLoading = findViewById(R.id.pbLoading);
        tvError = findViewById(R.id.tvError);
        tvSuccess = findViewById(R.id.tvSuccess);
        cardCheckin = findViewById(R.id.cardCheckin);
    }

    private void setupBiometric() {
        BiometricManager biometricManager = BiometricManager.from(this);
        biometricAvailable = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG
                | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                == BiometricManager.BIOMETRIC_SUCCESS;

        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                onBiometricSuccess();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                tvError.setText(errString);
                tvError.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAuthenticationFailed() {
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.checkin_biometric_title))
                .setSubtitle(getString(R.string.checkin_biometric_subtitle))
                .setNegativeButtonText(getString(android.R.string.cancel))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG
                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();
    }

    private void promptBiometric() {
        if (biometricDone) return;

        if (biometricAvailable) {
            biometricPrompt.authenticate(promptInfo);
        } else {
            tvError.setText(R.string.checkin_no_authenticator);
            tvError.setVisibility(View.VISIBLE);
            tvError.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                startActivity(intent);
            });
        }
    }

    private void onBiometricSuccess() {
        biometricDone = true;
        btnConfirmar.setEnabled(true);
        viewModel.onBiometricPassed();
    }

    private void onConfirmarClicked() {
        String senha = etSenha.getText() != null ? etSenha.getText().toString() : "";
        String coacao = etCoacao.getText() != null ? etCoacao.getText().toString() : "";

        String senhaFinal;
        String tipoSenha;

        if (!coacao.isEmpty()) {
            senhaFinal = coacao;
            tipoSenha = "coacao";
        } else if (!senha.isEmpty()) {
            senhaFinal = senha;
            tipoSenha = "padrao";
        } else {
            tvError.setText(R.string.checkin_error_empty);
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        setLoading(true);
        viewModel.realizarCheckin(senhaFinal, tipoSenha);
    }

    private void setLoading(boolean loading) {
        btnConfirmar.setEnabled(!loading);
        etSenha.setEnabled(!loading);
        etCoacao.setEnabled(!loading);
        pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void observeViewModel() {
        viewModel.getCheckinState().observe(this, resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    onCheckinSuccess(resource.getData());
                    break;
                case OFFLINE_SAVED:
                    setLoading(false);
                    onCheckinSuccess(null);
                    break;
                case ERROR:
                    setLoading(false);
                    tvError.setText(resource.getMessage());
                    tvError.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private void onCheckinSuccess(Turno turno) {
        cardCheckin.setVisibility(View.INVISIBLE);
        tvSuccess.setVisibility(View.VISIBLE);

        handler.postDelayed(() -> {
            android.content.Intent resultIntent = new android.content.Intent();
            resultIntent.putExtra(EXTRA_CHECKIN_OK, true);
            resultIntent.putExtra("tipo_senha", "padrao");
            setResult(RESULT_OK, resultIntent);
            finish();
        }, 1500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (biometricPrompt != null) {
            biometricPrompt.cancelAuthentication();
        }
    }
}
