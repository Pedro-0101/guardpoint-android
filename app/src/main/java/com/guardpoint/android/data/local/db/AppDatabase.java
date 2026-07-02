package com.guardpoint.android.data.local.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.guardpoint.android.data.local.db.dao.CheckinDao;
import com.guardpoint.android.data.local.db.dao.TurnoDao;
import com.guardpoint.android.data.local.db.entity.CheckinPendente;
import com.guardpoint.android.data.local.db.entity.TurnoAtivo;

@Database(entities = {CheckinPendente.class, TurnoAtivo.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    public abstract CheckinDao checkinDao();

    public abstract TurnoDao turnoDao();
}
