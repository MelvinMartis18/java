package com.application.essentials.db;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.application.essentials.AlarmsFragment;
import com.application.essentials.model.AlarmsAdapter;
import com.application.essentials.model.AlarmsViewModel;

public class SaveAlarm {

    private AlarmEntity alarmEntity;
    private int choice;
    private int position;
    private int lastPosition;
    private AlarmsViewModel alarmsViewModel;
    private Fragment fragment;


//    public interface FragmentInterface { void persistAlarm(AlarmEntity alarmEntity); }
//    private FragmentInterface fragmentInterface = new AlarmsFragment();
//    public interface NotifyInterface { void notifyAdapter(int choice, int position, int lastPosition);  }
//    private NotifyInterface notifyInterface = new AlarmsAdapter();

    public SaveAlarm(int choice, int position, int lastPosition, AlarmEntity alarmEntity) {
        this.alarmEntity = alarmEntity;
        this.choice = choice;
        this.position = position;
        this.lastPosition = lastPosition;
        this.fragment = fragment;
        alarmsViewModel = ViewModelProviders.of(fragment).get(AlarmsViewModel.class);
    }


    public void insertAlarm(AlarmEntity alarmEntity) {
        alarmsViewModel.insertAlarm(alarmEntity);

         //fragmentInterface.persistAlarm(alarmEntity);
        //notifyInterface.notifyAdapter(choice, position, lastPosition);
        //notifyInterface.notifyAdapter(choice, position, lastPosition);

//        if (alarmEntity.getAlarmOn()) {
//            setAlarm(alarmEntity);
//            Toast.makeText(fragment.getContext(), "alarmOn: " + alarmEntity.getAlarmOn(), Toast.LENGTH_SHORT)
//                    .show();
//        }
//        else {
//            cancelAlarm(alarmEntity);
//            Toast.makeText(fragment.getContext(), "Alarm cancelled", Toast.LENGTH_SHORT)
//                    .show();
//        }
    }

    public void deleteAlarm() {
        alarmsViewModel.deleteAlarm(alarmEntity);
       // notifyInterface.notifyAdapter(choice, position, lastPosition);
        cancelAlarm(alarmEntity);
    }

    private void setAlarm(AlarmEntity alarmEntity) {
        // set alarm
    }

    private void cancelAlarm(AlarmEntity alarmEntity) {
        //cancel alarm
    }
}
