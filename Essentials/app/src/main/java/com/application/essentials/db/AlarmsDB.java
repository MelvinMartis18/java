package com.application.essentials.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;


@Database(entities = {AlarmEntity.class}, version = 1, exportSchema = false)
public abstract class AlarmsDB extends RoomDatabase {

    public abstract AlarmsDAO alarmsDAO();

    private static volatile AlarmsDB INSTANCE;

    static AlarmsDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AlarmsDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AlarmsDB.class, "alarms_database19")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}