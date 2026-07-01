package com.guardpoint.android.data.local.db.dao;

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

    @Query("SELECT * FROM checkins_pendentes ORDER BY timestampCriacao ASC")
    List<CheckinPendente> getAllPendentes();

    @Query("DELETE FROM checkins_pendentes WHERE id IN (:ids)")
    void deleteByIds(List<Long> ids);
}
