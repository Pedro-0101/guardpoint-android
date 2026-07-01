package com.guardpoint.android.data.local.db.entity;

import androidx.annotation.NonNull;
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
    public String tipoSenha;

    public int tentativasEnvio;
}
