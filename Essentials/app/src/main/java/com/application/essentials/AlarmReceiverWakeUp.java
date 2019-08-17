package com.application.essentials;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiverWakeUp extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmWakeLock.acquire(context);
        Log.e("InAlarmReceiverWakeUp", "Wokenup");
    }
}
