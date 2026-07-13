package com.guardpoint.android.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.guardpoint.android.R;
import com.guardpoint.android.data.remote.dto.BiometricRegisterResponse;
import com.guardpoint.android.data.remote.dto.LoginResponse;
import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.ui.home.HomeActivity;

import java.util.concurrent.Executor;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;

    private ChipGroup cgLoginMode;
    private Chip chipAdmin;
    private Chip chipVigia;
    private TextInputLayout tilCodigoEmpresa;
    private TextInputLayout tilNome;
    private TextInputEditText etCodigoEmpresa;
    private TextInputEditText etNome;
    private TextInputLayout tilEmail;
    private TextInputLayout tilSenha;
    private TextInputEditText etEmail;
    private TextInputEditText etSenha;
    private MaterialButton btnLogin;
    private View pbLoading;
    private TextView tvError;

    private boolean isVigiaMode = false;
    private String deviceId;

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        bindViews();
        setupModeToggle();
        setupTextWatchers();
        setupBiometricPrompt();

        btnLogin.setOnClickListener(v -> onLoginClicked());
        checkExistingSession();
    }

    private void bindViews() {
        cgLoginMode = findViewById(R.id.cgLoginMode);
        chipAdmin = findViewById(R.id.chipAdmin);
        chipVigia = findViewById(R.id.chipVigia);
        tilCodigoEmpresa = findViewById(R.id.tilCodigoEmpresa);
        tilNome = findViewById(R.id.tilNome);
        etCodigoEmpresa = findViewById(R.id.etCodigoEmpresa);
        etNome = findViewById(R.id.etNome);
        tilEmail = findViewById(R.id.tilEmail);
        tilSenha = findViewById(R.id.tilSenha);
        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        btnLogin = findViewById(R.id.btnLogin);
        pbLoading = findViewById(R.id.pbLoading);
        tvError = findViewById(R.id.tvError);
    }

    private void setupModeToggle() {
        cgLoginMode.setOnCheckedStateChangeListener((group, checkedIds) -> {
            isVigiaMode = checkedIds.contains(chipVigia.getId());
            updateFieldsForMode();
        });
        updateFieldsForMode();
    }

    private void updateFieldsForMode() {
        int adminVisibility = isVigiaMode ? View.GONE : View.VISIBLE;
        int vigiaVisibility = isVigiaMode ? View.VISIBLE : View.GONE;

        tilEmail.setVisibility(adminVisibility);
        tilCodigoEmpresa.setVisibility(vigiaVisibility);
        tilNome.setVisibility(vigiaVisibility);

        if (isVigiaMode) {
            ((androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) tilSenha.getLayoutParams()).topToBottom = R.id.tilNome;
        } else {
            ((androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) tilSenha.getLayoutParams()).topToBottom = R.id.tilEmail;
        }
        tilSenha.requestLayout();

        tvError.setVisibility(View.GONE);
    }

    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                tvError.setVisibility(View.GONE);
            }
        };
        etEmail.addTextChangedListener(textWatcher);
        etCodigoEmpresa.addTextChangedListener(textWatcher);
        etNome.addTextChangedListener(textWatcher);
        etSenha.addTextChangedListener(textWatcher);
    }

    private void setupBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                if (viewModel.hasDeviceSecret() && viewModel.hasValidSession()) {
                    fazerLoginBiometrico();
                } else if (viewModel.hasValidSession()) {
                    navigateToHome();
                }
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_prompt_title))
                .setSubtitle(getString(R.string.biometric_prompt_subtitle))
                .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_WEAK |
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();
    }

    private void checkExistingSession() {
        if (viewModel.isBiometricEnabled()) {
            BiometricManager biometricManager = BiometricManager.from(this);
            int canAuth = biometricManager.canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK |
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL);

            if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
                biometricPrompt.authenticate(promptInfo);
            }
        }
    }

    private void fazerLoginBiometrico() {
        Timber.i("fazendo login biometrico...");
        viewModel.loginBiometric(deviceId).observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    Timber.i("Login biometrico OK, navegando para home");
                    navigateToHome();
                    break;
                case ERROR:
                    setLoading(false);
                    Timber.e("Login biometrico falhou: %s", resource.getMessage());
                    tvError.setText(resource.getMessage());
                    tvError.setVisibility(View.VISIBLE);
                    viewModel.logout();
                    break;
            }
        });
    }

    private void onLoginClicked() {
        String senha = etSenha.getText() != null ? etSenha.getText().toString() : "";

        if (isVigiaMode) {
            String codigoEmpresa = etCodigoEmpresa.getText() != null ? etCodigoEmpresa.getText().toString().trim() : "";
            String nome = etNome.getText() != null ? etNome.getText().toString().trim() : "";

            if (codigoEmpresa.isEmpty() || nome.isEmpty() || senha.isEmpty()) {
                tvError.setText(getString(R.string.login_error_empty_fields_vigia));
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            setLoading(true);

            viewModel.loginVigia(codigoEmpresa, nome, senha).observe(this, resource -> {
                if (resource == null) return;
                handleLoginResult(resource);
            });
        } else {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

            if (email.isEmpty() || senha.isEmpty()) {
                tvError.setText(getString(R.string.login_error_empty_fields));
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            setLoading(true);

            viewModel.login(email, senha).observe(this, resource -> {
                if (resource == null) return;
                handleLoginResult(resource);
            });
        }
    }

    private void handleLoginResult(Resource<LoginResponse> resource) {
        switch (resource.getStatus()) {
            case LOADING:
                setLoading(true);
                break;
            case SUCCESS:
                setLoading(false);
                registrarDispositivo();
                break;
            case ERROR:
                setLoading(false);
                tvError.setText(resource.getMessage());
                tvError.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void registrarDispositivo() {
        Timber.i("registrando dispositivo device_id=%s", deviceId);
        viewModel.registerDevice(deviceId).observe(this, resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    Timber.i("Dispositivo registrado com sucesso");
                    aposRegistroDispositivo();
                    break;
                case ERROR:
                    setLoading(false);
                    Timber.e("Falha ao registrar dispositivo: %s", resource.getMessage());
                    aposRegistroDispositivo();
                    break;
            }
        });
    }

    private void aposRegistroDispositivo() {
        if (!viewModel.isBiometricEnabled() && isBiometricAvailable()) {
            showBiometricEnrollmentDialog();
        } else {
            navigateToHome();
        }
    }

    private boolean isBiometricAvailable() {
        BiometricManager manager = BiometricManager.from(this);
        int result = manager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK |
                BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        return result == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private void showBiometricEnrollmentDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.biometric_enable_title)
                .setMessage(R.string.biometric_enable_message)
                .setPositiveButton(R.string.biometric_enable_positive, (dialog, which) -> {
                    viewModel.setBiometricEnabled(true);
                    navigateToHome();
                })
                .setNegativeButton(R.string.biometric_enable_negative, (dialog, which) -> {
                    navigateToHome();
                })
                .setCancelable(false)
                .show();
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
        finish();
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etCodigoEmpresa.setEnabled(!loading);
        etNome.setEnabled(!loading);
        etSenha.setEnabled(!loading);
        pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
