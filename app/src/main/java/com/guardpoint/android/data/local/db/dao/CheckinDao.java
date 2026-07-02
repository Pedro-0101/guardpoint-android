package com.guardpoint.android.data.local.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.guardpoint.android.data.local.db.entity.CheckinPendente;

import java.util.List;

@Dao
public interface CheckinDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CheckinPendente checkinPendente);

    @Query("SELECT * FROM checkins_pendentes WHERE status IS NULL OR status = 'pendente' ORDER BY timestampCriacao ASC")
    List<CheckinPendente> getAllPendentes();

    @Query("DELETE FROM checkins_pendentes WHERE id IN (:ids)")
    void deleteByIds(List<Long> ids);

    @Query("SELECT COUNT(*) FROM checkins_pendentes WHERE status IS NULL OR status = 'pendente'")
    LiveData<Integer> getPendentesCountLive();

    @Query("SELECT COUNT(*) FROM checkins_pendentes WHERE status IS NULL OR status = 'pendente'")
    int getPendentesCount();

    @Query("UPDATE checkins_pendentes SET tentativasEnvio = tentativasEnvio + 1 WHERE id = :id")
    void incrementTentativas(long id);

    @Query("UPDATE checkins_pendentes SET status = :status WHERE id = :id")
    void updateStatus(long id, String status);

    @Query("DELETE FROM checkins_pendentes WHERE tentativasEnvio >= :maxRetry AND tipoSenha NOT IN ('coacao', 'finalizacao') AND (status IS NULL OR status = 'pendente')")
    void deleteExcedentes(int maxRetry);

    @Query("SELECT * FROM checkins_pendentes WHERE status = 'erro' AND (tipoSenha = 'coacao' OR tipoSenha = 'finalizacao') ORDER BY timestampCriacao ASC")
    List<CheckinPendente> getErroCritico();
}
