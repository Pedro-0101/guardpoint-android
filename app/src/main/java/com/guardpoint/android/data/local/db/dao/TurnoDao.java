package com.guardpoint.android.data.local.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.guardpoint.android.data.local.db.entity.TurnoAtivo;

@Dao
public interface TurnoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TurnoAtivo turnoAtivo);

    @Query("SELECT * FROM turno_ativo LIMIT 1")
    TurnoAtivo getTurnoAtivo();

    @Update
    void update(TurnoAtivo turnoAtivo);

    @Query("DELETE FROM turno_ativo")
    void delete();
}
