package com.application.essentials.model;

import com.application.essentials.db.AlarmEntity;

public interface AdapterFragmentInterface {

    void launchSetAlarmActivity(AlarmEntity alarmEntity, int choice);
    void saveAlarm(AlarmEntity alarmEntity, int choice);
    void showDeleteDialog(int position, int getItemCount, AlarmEntity alarmEntity);
    void deleteAlarm(AlarmEntity alarmEntity);
}
