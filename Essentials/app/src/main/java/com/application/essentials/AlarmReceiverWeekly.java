package com.application.essentials;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;
import java.util.Calendar;
import java.util.Locale;

public class AlarmReceiverWeekly extends BroadcastReceiver {
    private int dayRequestCode;
    private int intervalRequestCode;
    private long endTimeInMillis;
    private int interval;
    private int duration;
    private Boolean repeatWeekly;
    private int alarmStartTimeInSeconds;
    private Boolean vibrate;
    private int ascendingVolumeMax;


    @Override
    public void onReceive(Context context, Intent intent) {
//        AlarmWakeLock.acquire(context);
        getAllExtras(intent);

        //Todo: callback from dismiss/ when last alarm time rings
        // Cancel Upcoming Alarm Notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(dayRequestCode);

        // Set repeating interval if Next Alarm time is < End time
        long nextAlarmTimeInMillis = System.currentTimeMillis() + (interval*60*1000);
        if ((interval != -1)
                && (nextAlarmTimeInMillis < endTimeInMillis)) {
            setRepeatingInterval(context);
        }

        // Todo: On BOOT_COMPLETE set all active alarms off and on
        setAgainIfRepeatingWeekly(context);

        // todo: show next alarm notification when missed
// 2.  launchAlarmActionActivity
//  on Stop/expire, generate notification: pass interval requestCode through intent for cancelling (via notification)
        try {
            Intent intentLaunchAlarmAction = new Intent(context.getApplicationContext(), AlarmActionActivity.class);
            intentLaunchAlarmAction.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intentLaunchAlarmAction.putExtra("interval", interval);
            intentLaunchAlarmAction.putExtra("duration", duration);
            intentLaunchAlarmAction.putExtra("dayRequestCode", dayRequestCode);
            intentLaunchAlarmAction.putExtra("intervalRequestCode", intervalRequestCode);
            intentLaunchAlarmAction.putExtra("endTimeInMillis", endTimeInMillis);
            intentLaunchAlarmAction.putExtra("repeatWeekly", repeatWeekly);
            intentLaunchAlarmAction.putExtra("alarmStartTimeInSeconds", alarmStartTimeInSeconds);
            intentLaunchAlarmAction.putExtra("vibrate", vibrate);
            intentLaunchAlarmAction.putExtra("ascendingVolumeMax", ascendingVolumeMax);

            context.startActivity(intentLaunchAlarmAction);
        }
        catch (Exception e){
            e.getStackTrace();
            Log.e("weeklyReceiverError",""+e.getMessage());
            Toast.makeText(context.getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_LONG).show();
        }
//        AlarmWakeLock.release();
    }


    private void getAllExtras (Intent intent) {
        dayRequestCode = intent.getIntExtra("dayRequestCode", 0);
        intervalRequestCode = intent.getIntExtra("intervalRequestCode", 0);
        endTimeInMillis = intent.getLongExtra("endTimeInMillis", 0L);
        interval = intent.getIntExtra("interval", 5);
        duration = intent.getIntExtra("duration", 1);
        repeatWeekly = intent.getBooleanExtra("repeatWeekly", false);
        alarmStartTimeInSeconds = intent.getIntExtra("alarmStartTimeInSeconds", 0);
        vibrate = intent.getBooleanExtra("vibrate", true);
        ascendingVolumeMax = intent.getIntExtra("ascendingVolumeMax", 6);
    }


    private  void setAgainIfRepeatingWeekly(Context context) {
        if (repeatWeekly) {
            // manually repeat for L and above
            // since setAlarmClockRepeating is not available, for showing next repeating alarm on lock screen, set repeating
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                long nextRepeatingTimeInMillis = System.currentTimeMillis()+(7*24*60*60*1000);

                // skipped send extras, since PendingIntent Flag is NOT FLAG_UPDATE_CURRENT
                Intent intentRepeatWeekly = new Intent(context, AlarmReceiverWeekly.class);
//                intentRepeatWeekly.putExtra("dayRequestCode", dayRequestCode);
//                intentRepeatWeekly.putExtra("intervalRequestCode", intervalRequestCode);
//                intentRepeatWeekly.putExtra("interval", interval);
//                intentRepeatWeekly.putExtra("duration", duration);
//                intentRepeatWeekly.putExtra("endTimeInMillis", endTimeInMillis);
//                intentRepeatWeekly.putExtra("repeatWeekly", repeatWeekly);
//                intentRepeatWeekly.putExtra("alarmStartTimeInSeconds", alarmStartTimeInSeconds);
//                intentRepeatWeekly.putExtra("vibrate", vibrate);
                PendingIntent pendingIntentRepeatWeekly = PendingIntent.getBroadcast(context, dayRequestCode, intentRepeatWeekly, 0);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(nextRepeatingTimeInMillis, pendingIntentRepeatWeekly), pendingIntentRepeatWeekly);

                if (interval != -1) {
                    Intent intentEndTime = new Intent(context, AlarmReceiverEndTime.class);
                    intentEndTime.putExtra("intervalRequestCode", intervalRequestCode);
                    PendingIntent pendingIntentEndTime = PendingIntent.getBroadcast(context, 0, intentEndTime, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, endTimeInMillis+(7*24*60*60*1000),7*24*60*60*1000 , pendingIntentEndTime);
                }
            }
        }
    }


    private void setRepeatingInterval(Context context) {
        Intent intentRepeatInterval = new Intent(context, AlarmReceiverInterval.class);
        // send request code as parameter to launched activity (on stop/expire action show notification)
        // (notification action will be dismiss all)
        intentRepeatInterval.putExtra("intervalRequestCode", intervalRequestCode);
        intentRepeatInterval.putExtra("dayRequestCode", dayRequestCode);
        intentRepeatInterval.putExtra("interval", interval);
        intentRepeatInterval.putExtra("duration", duration);
        intentRepeatInterval.putExtra("endTimeInMillis", endTimeInMillis);
        intentRepeatInterval.putExtra("repeatWeekly", repeatWeekly);
        intentRepeatInterval.putExtra("alarmStartTimeInSeconds", alarmStartTimeInSeconds);
        intentRepeatInterval.putExtra("vibrate", vibrate);
        intentRepeatInterval.putExtra("ascendingVolumeMax", ascendingVolumeMax);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntentRepeatInterval = PendingIntent.getBroadcast(context, intervalRequestCode, intentRepeatInterval, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // this is overriding setRepeating and sets the alarm only for this time
            // hence not repeating
            // solution: in intervalReceiver repeat the following block
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(System.currentTimeMillis()+(interval*60*1000), pendingIntentRepeatInterval), pendingIntentRepeatInterval);
            long nextAlarmTimeIntervalInWeekly = alarmManager.getNextAlarmClock().getTriggerTime();
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.setTimeInMillis(nextAlarmTimeIntervalInWeekly);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+(interval * 60 * 1000), interval * 60 * 1000, pendingIntentRepeatInterval);
        }

        Log.e("intervalCheck","interval: "+interval);
    }
}