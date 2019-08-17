package com.application.essentials.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import com.application.essentials.db.AlarmEntity;

import java.util.List;

@Dao
public interface AlarmsDAO {

    @Query("SELECT * FROM AlarmEntity")
    LiveData<List<AlarmEntity>> getAlarm();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAlarm(AlarmEntity alarmEntity);

    @Delete
    void deleteAlarm(AlarmEntity alarmEntity);
}
