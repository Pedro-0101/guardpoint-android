package com.guardpoint.android.data.local.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "turno_ativo")
public class TurnoAtivo {

    @PrimaryKey
    @NonNull
    public String turnoId;

    public String postoId;
    public String postoNome;
    public int intervaloMinutos;
    public long ultimoCheckinMillis;
    public String tokenSessao;
    public String status;
}
