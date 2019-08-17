package com.application.essentials;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;
import java.util.Locale;

public class AlarmReceiverInterval extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle intervalBundle = intent.getExtras();
        int intervalRequestCode = intent.getIntExtra("intervalRequestCode", 0);
        int dayRequestCode = intent.getIntExtra("dayRequestCode", 0);
        int interval = intent.getIntExtra("interval", 5);
        int duration = intent.getIntExtra("duration", 1);
        long endTimeInMillis = intent.getLongExtra("endTimeInMillis", 0);
        Boolean repeatWeekly = intent.getBooleanExtra("repeatWeekly", false);
        int alarmStartTimeInSeconds = intent.getIntExtra("alarmStartTimeInSeconds", 0);
        Boolean vibrate = intent.getBooleanExtra("vibrate", true);
        int ascendingVolumeMax = intent.getIntExtra("ascendingVolumeMax", 6);

        //  launchAlarmActionActivity
        //  pass intervalRequestCode through intent for cancelling (via notification)
        Intent intentLaunchAlarmAction = new Intent(context.getApplicationContext(), AlarmActionActivity.class);
        intentLaunchAlarmAction.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                                        | Intent.FLAG_ACTIVITY_NO_HISTORY);
        intentLaunchAlarmAction.putExtra("interval", interval);
        intentLaunchAlarmAction.putExtra("duration", duration);
        intentLaunchAlarmAction.putExtra("dayRequestCode", dayRequestCode);
        intentLaunchAlarmAction.putExtra("intervalRequestCode", intervalRequestCode);
        intentLaunchAlarmAction.putExtra("endTimeInMillis", endTimeInMillis);
        intentLaunchAlarmAction.putExtra("repeatWeekly", repeatWeekly);
        intentLaunchAlarmAction.putExtra("alarmStartTimeInSeconds", alarmStartTimeInSeconds);
        intentLaunchAlarmAction.putExtra("vibrate", vibrate);
        intentLaunchAlarmAction.putExtra("ascendingVolumeMax", ascendingVolumeMax);
        // todo: check if action activity is receiving parameter vibrate when set to true/false
        context.startActivity(intentLaunchAlarmAction);

        // manually repeat for L and above
        // since setAlarmClockRepeating is not available, for showing next repeating alarm on lock screen, set repeating
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            long nextRepeatingTimeInMillis = System.currentTimeMillis()+(interval*60*1000);

            Log.e("AlarmReceiverInterval", "before if, endTimeInMillis: " + endTimeInMillis + "nextRepeatingTimeInMillis: "+nextRepeatingTimeInMillis);
            if (nextRepeatingTimeInMillis < endTimeInMillis) {
                Log.e("AlarmReceiverInterval", "inside if");
                Intent intentRepeatInterval = new Intent(context, AlarmReceiverInterval.class);
                intentRepeatInterval.putExtras(intervalBundle);
                PendingIntent pendingIntentRepeatInterval = PendingIntent.getBroadcast(context, intervalRequestCode, intentRepeatInterval, 0);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(nextRepeatingTimeInMillis, pendingIntentRepeatInterval), pendingIntentRepeatInterval);
                // long nextAlarmTimeIntervalOnReceive = alarmManager.getNextAlarmClock().getTriggerTime();
                // Calendar calendar3 = Calendar.getInstance(Locale.getDefault());
                // calendar3.setTimeInMillis(nextAlarmTimeIntervalOnReceive);
            }
        }
    }
}