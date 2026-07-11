package com.guardpoint.android.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.guardpoint.android.R;
import com.guardpoint.android.ui.home.HomeActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;

    private TextInputEditText etEmail;
    private TextInputEditText etSenha;
    private MaterialButton btnLogin;
    private View pbLoading;
    private android.widget.TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        bindViews();
        setupTextWatchers();

        btnLogin.setOnClickListener(v -> onLoginClicked());
        checkExistingSession();
    }

    private void bindViews() {
        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        btnLogin = findViewById(R.id.btnLogin);
        pbLoading = findViewById(R.id.pbLoading);
        tvError = findViewById(R.id.tvError);
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
        etSenha.addTextChangedListener(textWatcher);
    }

    private void checkExistingSession() {
        if (viewModel.hasValidSession()) {
            navigateToHome();
        }
    }

    private void onLoginClicked() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String senha = etSenha.getText() != null ? etSenha.getText().toString() : "";

        if (email.isEmpty() || senha.isEmpty()) {
            tvError.setText(getString(R.string.login_error_empty_fields));
            tvError.setVisibility(View.VISIBLE);
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
                    setLoading(false);
                    navigateToHome();
                    break;
                case ERROR:
                    setLoading(false);
                    tvError.setText(resource.getMessage());
                    tvError.setVisibility(View.VISIBLE);
                    break;
            }
        });
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
        etSenha.setEnabled(!loading);
        pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
