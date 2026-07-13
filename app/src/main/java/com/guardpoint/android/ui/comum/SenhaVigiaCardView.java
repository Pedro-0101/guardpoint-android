package com.guardpoint.android.ui.comum;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.guardpoint.android.R;

public class SenhaVigiaCardView extends LinearLayout {

    private TextInputLayout inputLayoutSenha;
    private TextInputEditText etSenha;
    private MaterialButton btnEnviar;
    private String acaoOriginal;

    public SenhaVigiaCardView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public SenhaVigiaCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SenhaVigiaCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_senha_vigia_card, this, true);

        inputLayoutSenha = findViewById(R.id.inputLayoutSenha);
        etSenha = findViewById(R.id.etSenha);
        btnEnviar = findViewById(R.id.btnEnviar);

        etSenha.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                inputLayoutSenha.setError(null);
            }
        });
    }

    public void setAcao(String acao) {
        acaoOriginal = acao;
        btnEnviar.setText(acao);
    }

    public void setOnEnviarListener(OnClickListener listener) {
        btnEnviar.setOnClickListener(v -> {
            String senha = etSenha.getText() != null ? etSenha.getText().toString().trim() : "";
            if (TextUtils.isEmpty(senha)) {
                inputLayoutSenha.setError(getContext().getString(R.string.senha_vigia_erro_vazio));
                return;
            }
            if (listener != null) {
                listener.onClick(v);
            }
        });
    }

    public void setLoading(boolean loading) {
        btnEnviar.setEnabled(!loading);
        if (loading && acaoOriginal != null) {
            btnEnviar.setText(acaoOriginal + "...");
        } else if (!loading && acaoOriginal != null) {
            btnEnviar.setText(acaoOriginal);
        }
    }

    public String getSenha() {
        return etSenha.getText() != null ? etSenha.getText().toString().trim() : "";
    }

    public void mostrarSucesso() {
        Snackbar.make(this, R.string.senha_vigia_sucesso, Snackbar.LENGTH_SHORT).show();
        etSenha.setText("");
        etSenha.clearFocus();
    }

    public void mostrarErro(String mensagem) {
        Snackbar.make(this, mensagem, Snackbar.LENGTH_LONG).show();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        etSenha.setEnabled(enabled);
        btnEnviar.setEnabled(enabled);
    }

    public void resetSenha() {
        etSenha.setText("");
        etSenha.clearFocus();
        inputLayoutSenha.setError(null);
    }
}
