package com.application.essentials.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;
import android.widget.Toast;

import com.application.essentials.db.AlarmEntity;
import com.application.essentials.db.AlarmsRepository;

import java.util.List;

public class AlarmsViewModel extends AndroidViewModel {

    AlarmsRepository alarmsRepository;
    LiveData<List<AlarmEntity>> alarmLiveData;

    public AlarmsViewModel(Application application) {
        super(application);
        alarmsRepository = new AlarmsRepository(application);
    }

// returns LiveData object
    public LiveData<List<AlarmEntity>> getAlarm() {
        if (alarmLiveData == null) {
            alarmLiveData = alarmsRepository.getAlarm();
//            Log.e("viewModel","alarm is null fetching from repository");
        }
        return alarmLiveData;
    }

    public void insertAlarm(AlarmEntity alarmEntity) {
        alarmsRepository.insertAlarm(alarmEntity);
    }

    public void deleteAlarm(AlarmEntity alarmEntity) {
        alarmsRepository.deleteAlarm(alarmEntity);
    }
}