package com.application.essentials;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;


public class AlarmReceiverEndTime extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int intervalRequestCode = intent.getIntExtra("intervalRequestCode", 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intentRepeatInterval = new Intent(context, AlarmReceiverInterval.class);
        PendingIntent pendingIntentRepeatInterval = PendingIntent.getBroadcast(context, intervalRequestCode, intentRepeatInterval, 0);
        alarmManager.cancel(pendingIntentRepeatInterval);
        Toast.makeText(context, "Alarm ended", Toast.LENGTH_SHORT)
                .show();
    }
}