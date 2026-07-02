package com.guardpoint.android.data.local.db;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.guardpoint.android.data.local.db.dao.CheckinDao;
import com.guardpoint.android.data.local.db.dao.TurnoDao;
import com.guardpoint.android.data.local.db.entity.CheckinPendente;
import com.guardpoint.android.data.local.db.entity.TurnoAtivo;

@Database(entities = {CheckinPendente.class, TurnoAtivo.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE checkins_pendentes ADD COLUMN clienteCheckinId TEXT");
            database.execSQL("ALTER TABLE checkins_pendentes ADD COLUMN status TEXT NOT NULL DEFAULT 'pendente'");
        }
    };

    public abstract CheckinDao checkinDao();

    public abstract TurnoDao turnoDao();
}
