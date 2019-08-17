package com.application.essentials;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

public class UpcomingAlarmBroadcastReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID_NOTIFY_NEXT_ALARM = "UPCOMING_ALARM";
    private static final String CHANNEL_NAME_NOTIFY_NEXT_ALARM = "Upcoming Alarm";

    @Override
    public void onReceive(Context context, Intent intent) {
        int dayRequestCode = intent.getIntExtra("dayRequestCode", 0);
        Boolean repeatWeekly = intent.getBooleanExtra("repeatWeekly", false);
        Integer alarmStartTimeInSeconds = intent.getIntExtra("alarmStartTimeInSeconds", 0);
        String nextAlarmTimeFormatted = intent.getStringExtra("nextAlarmTimeFormatted");

        setNotifyNextAlarmNotification(context, dayRequestCode, repeatWeekly, alarmStartTimeInSeconds, nextAlarmTimeFormatted);
    }


    public void setNotifyNextAlarmNotification(Context context, int dayRequestCode, Boolean repeatWeekly, int alarmStartTimeInSeconds, String nextAlarmTimeFormatted) {
        Intent intentNotifyNextAlarm = new Intent(context, UpcomingNotificationReceiver.class);
        intentNotifyNextAlarm.putExtra("notification_action", "DISMISS");
        intentNotifyNextAlarm.putExtra("dayRequestCode", dayRequestCode);
        intentNotifyNextAlarm.putExtra("repeatWeekly", repeatWeekly);
        intentNotifyNextAlarm.putExtra("alarmStartTimeInSeconds", alarmStartTimeInSeconds);
        PendingIntent pendingIntentNotifyNextAlarmDismissAll = PendingIntent.getBroadcast(context, dayRequestCode, intentNotifyNextAlarm, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel nChannelNextAlarm = new NotificationChannel(
                    CHANNEL_ID_NOTIFY_NEXT_ALARM, CHANNEL_NAME_NOTIFY_NEXT_ALARM, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(nChannelNextAlarm);
        }

        NotificationCompat.Action actionDismissAll =
                new NotificationCompat.Action.Builder(0, "DISMISS", pendingIntentNotifyNextAlarmDismissAll)
                        .build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID_NOTIFY_NEXT_ALARM)
                .setSmallIcon(R.drawable.ic_round_schedule_24px)
                .setContentTitle("Upcoming Alarm")
                .setContentText(nextAlarmTimeFormatted)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(actionDismissAll)
                .setOngoing(true);
//        Toast.makeText(context, "nextAlarmTimeFormatted: "+nextAlarmTimeFormatted, Toast.LENGTH_SHORT).show();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(dayRequestCode, builder.build());
    }
}
