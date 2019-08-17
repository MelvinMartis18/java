package com.application.essentials;

import android.support.v4.app.Fragment;

import com.application.essentials.db.AlarmEntity;

public interface AlarmsDialogInterface  {
    AlarmEntity getAlarmEntity();
    Fragment getFragment();
}