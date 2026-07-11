package com.guardpoint.android.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.guardpoint.android.R;
import com.guardpoint.android.ui.home.HomeActivity;

import dagger.hilt.android.AndroidEntryPoint;

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
    private android.widget.TextView tvError;

    private boolean isVigiaMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        bindViews();
        setupModeToggle();
        setupTextWatchers();

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
            tilEmail.setLayoutParams(tilEmail.getLayoutParams());
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

    private void checkExistingSession() {
        if (viewModel.hasValidSession()) {
            navigateToHome();
        }
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

    private void handleLoginResult(com.guardpoint.android.domain.model.Resource<com.guardpoint.android.data.remote.dto.LoginResponse> resource) {
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
                tvError.setText(resource.getMessage());
                tvError.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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
