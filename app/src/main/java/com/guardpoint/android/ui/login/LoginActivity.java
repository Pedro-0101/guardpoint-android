package com.guardpoint.android.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.guardpoint.android.R;
import com.guardpoint.android.data.remote.dto.LoginResponse;
import com.guardpoint.android.domain.model.Resource;

import java.util.concurrent.Executor;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;

    private TextInputEditText etEmail;
    private TextInputEditText etSenha;
    private TextInputLayout tilEmail;
    private TextInputLayout tilSenha;
    private MaterialButton btnLogin;
    private View pbLoading;
    private View ivBiometric;
    private android.widget.TextView tvError;

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private boolean biometricAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        bindViews();
        setupTextWatchers();
        setupBiometric();
        observeViewModel();

        btnLogin.setOnClickListener(v -> onLoginClicked());

        checkExistingSession();
    }

    private void bindViews() {
        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        tilEmail = findViewById(R.id.tilEmail);
        tilSenha = findViewById(R.id.tilSenha);
        btnLogin = findViewById(R.id.btnLogin);
        pbLoading = findViewById(R.id.pbLoading);
        ivBiometric = findViewById(R.id.ivBiometric);
        tvError = findViewById(R.id.tvError);
    }

    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                hideError();
            }
        };
        etEmail.addTextChangedListener(textWatcher);
        etSenha.addTextChangedListener(textWatcher);
    }

    private void setupBiometric() {
        BiometricManager biometricManager = BiometricManager.from(this);
        biometricAvailable = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS;

        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                onBiometricSuccess();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                showError(getString(R.string.login_biometric_error));
            }

            @Override
            public void onAuthenticationFailed() {
                showError(getString(R.string.login_biometric_error));
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.login_biometric_title))
                .setSubtitle(getString(R.string.login_biometric_subtitle))
                .setNegativeButtonText(getString(android.R.string.cancel))
                .build();

        if (biometricAvailable) {
            ivBiometric.setVisibility(View.VISIBLE);
            ivBiometric.setOnClickListener(v -> biometricPrompt.authenticate(promptInfo));
        }
    }

    private void checkExistingSession() {
        if (viewModel.isBiometricEnabled() && biometricAvailable) {
            biometricPrompt.authenticate(promptInfo);
        }
    }

    private void onBiometricSuccess() {
        setLoading(true);
        viewModel.authenticateWithBiometric().observe(this, resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    navigateToHome();
                    break;
                case ERROR:
                    setLoading(false);
                    showError(resource.getMessage());
                    break;
            }
        });
    }

    private void onLoginClicked() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String senha = etSenha.getText() != null ? etSenha.getText().toString() : "";

        if (email.isEmpty() || senha.isEmpty()) {
            showError(getString(R.string.login_error_empty_fields));
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(R.string.login_error_invalid_email));
            return;
        }

        setLoading(true);

        viewModel.login(email, senha).observe(this, resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    onLoginSuccess(resource.getData());
                    break;
                case ERROR:
                    setLoading(false);
                    showError(resource.getMessage());
                    break;
            }
        });
    }

    private void onLoginSuccess(LoginResponse loginResponse) {
        if (biometricAvailable && loginResponse != null) {
            viewModel.registerBiometric().observe(this, resource -> {
                setLoading(false);
                navigateToHome();
            });
        } else {
            setLoading(false);
            navigateToHome();
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, com.guardpoint.android.ui.home.HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etSenha.setEnabled(!loading);
        pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (message != null && !message.isEmpty()) {
            tvError.setText(message);
            tvError.setVisibility(View.VISIBLE);
        }
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }

    private void observeViewModel() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (biometricPrompt != null) {
            biometricPrompt.cancelAuthentication();
        }
    }
}
