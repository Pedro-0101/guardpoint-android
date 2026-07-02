package com.guardpoint.android.data.local.db.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "checkins_pendentes")
public class CheckinPendente {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String turnoId;

    public double latitude;
    public double longitude;

    @NonNull
    public String timestampCriacao;

    @NonNull
    public String senha;

    @NonNull
    public String tipoSenha;

    public int tentativasEnvio;

    @Nullable
    public String clienteCheckinId;

    @NonNull
    public String status = STATUS_PENDENTE;

    public static final String STATUS_PENDENTE = "pendente";
    public static final String STATUS_ERRO = "erro";

    public boolean isCritical() {
        return "coacao".equals(tipoSenha) || "finalizacao".equals(tipoSenha);
    }
}
