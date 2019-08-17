package com.application.essentials;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.application.essentials.db.AlarmEntity;

import java.util.Calendar;
import java.util.Locale;

public class UpcomingNotificationReceiver extends BroadcastReceiver {
    private AlarmManager alarmManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        // interval, endTimeInMillis
        // Todo: On BOOT_COMPLETE set all alarms OFF and ON
        int dayRequestCode = intent.getIntExtra("dayRequestCode", 0);
        String notificationAction = intent.getStringExtra("notification_action");
        Boolean repeatWeekly = intent.getBooleanExtra("repeatWeekly", false);
        Integer alarmStartTimeInSeconds = intent.getIntExtra("alarmStartTimeInSeconds", 0);

        if (notificationAction.equals("DISMISS")) {
            Toast.makeText(context, "DISMISS called in NotificationReceiver", Toast.LENGTH_LONG).show();
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intentWeekly = new Intent(context, AlarmReceiverWeekly.class);
            PendingIntent pendingIntentWeekly = PendingIntent.getBroadcast(context, dayRequestCode, intentWeekly, 0);
            alarmManager.cancel(pendingIntentWeekly);

            // NotificationReceiver to receive repeatWeekly value, and set Alarm again accordingly
            // endTime
            if (repeatWeekly) {
                setAlarmAgainIfRepeatWeekly(context, alarmStartTimeInSeconds, dayRequestCode);
            }

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(dayRequestCode);
        }
    }


    private void setAlarmAgainIfRepeatWeekly(Context context, Integer alarmStartTimeInSeconds, int dayRequestCode) {
        Intent intentForWeekly = new Intent(context, AlarmReceiverWeekly.class);
        Long alarmTimeInSecondsLong = Long.valueOf(alarmStartTimeInSeconds);
        Long weeklyStartAlarmTime = alarmTimeInSecondsLong * 1000;

        PendingIntent pendingIntentForWeekly = PendingIntent.getBroadcast(context, dayRequestCode, intentForWeekly, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(weeklyStartAlarmTime + (7 * 24 * 60 * 60 * 1000), pendingIntentForWeekly), pendingIntentForWeekly);

            long nextAlarmTime = alarmManager.getNextAlarmClock().getTriggerTime();
            Calendar calendar2 = Calendar.getInstance(Locale.getDefault());
            calendar2.setTimeInMillis(nextAlarmTime);
            Toast.makeText(context, "nextAlarmWeeklyOnDismiss: " + calendar2.getTime(), Toast.LENGTH_LONG).show();
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, weeklyStartAlarmTime + (7 * 24 * 60 * 60 * 1000), 7 * 24 * 60 * 60 * 1000, pendingIntentForWeekly);
        }
    }
}