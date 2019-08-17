package com.application.essentials;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.application.essentials.db.AlarmEntity;
import com.application.essentials.model.AdapterFragmentInterface;
import com.application.essentials.model.AlarmsAdapter;
import com.application.essentials.model.AlarmsViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlarmsFragment extends Fragment implements AdapterFragmentInterface {

    View v;
    public AlarmsAdapter alarmsAdapter;
    public AlarmManager alarmManager;
    private Context context;
    private Intent intentForWeekly;
    AlarmsViewModel alarmsViewModel;
    private int ALARMS_ACTIVITY_REQUEST_CODE = 2;
    private Intent intentForUpcomingAlarmBCReceiver;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity().getApplicationContext();
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Log.e("onCreate","AlarmsFragment");
        intentForWeekly = new Intent(getActivity().getApplicationContext(), AlarmReceiverWeekly.class);
        intentForUpcomingAlarmBCReceiver = new Intent(getActivity().getApplicationContext(), UpcomingAlarmBroadcastReceiver.class);

        if (alarmsAdapter == null) {  alarmsAdapter = new AlarmsAdapter(this);    }
        if (alarmsViewModel == null) {  alarmsViewModel = ViewModelProviders.of(this).get(AlarmsViewModel.class); }

        alarmsViewModel.getAlarm().observe(this, new Observer<List<AlarmEntity>>() {
            @Override
            public void onChanged(final List<AlarmEntity> alarmEntityList) {
               alarmsAdapter.setAlarmEntity(alarmEntityList);
            }
        });
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        v = inflater.inflate(
                R.layout.fragment_alarms, container, false);

        RecyclerView rv = v.findViewById(R.id.recycler_view);
        rv.getRecycledViewPool().clear();
        rv.setAdapter(alarmsAdapter);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
//        rv.setItemAnimator(null);

        showNextAlarmSnackBar();
        return v;
    }


    private void showNextAlarmSnackBar() {
        String nextAlarmTime = "No alarm set";
        View parentLayout = v.findViewById(R.id.contentAlarmsFragment);
        if (Build.VERSION.SDK_INT >= 21) {
            Calendar calNextAlarm = Calendar.getInstance(Locale.getDefault());
            if (alarmManager.getNextAlarmClock()!= null) {
                calNextAlarm.setTimeInMillis(alarmManager.getNextAlarmClock().getTriggerTime());
                SimpleDateFormat sdfForDay = new SimpleDateFormat("EEE, MMM d  HH:mm", Locale.getDefault());
                nextAlarmTime = sdfForDay.format(calNextAlarm.getTime());
                nextAlarmTime = "Next alarm: "+nextAlarmTime;
            }
        }
        else {
            nextAlarmTime = Settings.System.getString(getActivity().getContentResolver(),
                    Settings.System.NEXT_ALARM_FORMATTED);
            nextAlarmTime = "Next alarm: "+nextAlarmTime;
        }

        Snackbar snackBar = Snackbar.make(parentLayout, nextAlarmTime, Snackbar.LENGTH_LONG);
        View sbView = snackBar.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryLight));
        snackBar.show();
    }


    public AlarmEntity getDefaultAlarmEntity() {
        Long alarmIdTemp = System.currentTimeMillis()/1000;
        int alarmId = alarmIdTemp.intValue();
        String label = "";
        int hourOfDay = 6;
        int minute = 0;
        Boolean alarmOn = true;
        Boolean repeatWeekly = false;
        List<Integer> daysArr = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0));
        int intervalTime = 5;
        int durationTime = 2;
        int endAtTimeHour = 7;
        int endAtTimeMinute = 0;
        long alarmStartTimeInMillis;
        long alarmEndTimeInMillis;
        Boolean vibrate = true;
        int upcomingAlarmTime = 1;
        int ascendingVolumeMax = 6;

        Calendar calendarStartTime = Calendar.getInstance(Locale.getDefault());
        calendarStartTime.set(Calendar.HOUR_OF_DAY, 6);
        calendarStartTime.set(Calendar.MINUTE, 0);
        alarmStartTimeInMillis = calendarStartTime.getTimeInMillis();
        if (alarmStartTimeInMillis < System.currentTimeMillis()) {
            alarmStartTimeInMillis += 24*60*60*1000;
        }

        alarmEndTimeInMillis = alarmStartTimeInMillis + (60*60*1000);

        AlarmEntity alarmEntity = new AlarmEntity(alarmId, hourOfDay, minute, label, alarmOn, repeatWeekly, daysArr, intervalTime, durationTime, endAtTimeHour, endAtTimeMinute, alarmStartTimeInMillis, alarmEndTimeInMillis, vibrate, upcomingAlarmTime, ascendingVolumeMax);
        return alarmEntity;
    }


    @Override
    public  void deleteAlarm(AlarmEntity alarmEntity) {
        alarmsViewModel.deleteAlarm(alarmEntity);
        cancelAllAlarms(alarmEntity);
    }

    @Override
    public void showDeleteDialog(final int position, final int itemCount, final AlarmEntity alarmEntity) {
        AlertDialog.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this.getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this.getActivity());
        }

        builder.setTitle("Delete Alarm")
                .setMessage("Delete this alarm?")
                .setCancelable(true)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
//                        alarmsViewModel.deleteAlarm(alarmEntity);
//                        cancelAllAlarms(alarmEntity);
                        deleteAlarm(alarmEntity);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public void launchSetAlarmActivity(AlarmEntity alarmEntity, int choice) {
                            // callback implementation. "this" is passed as reference to adapter, instantiates Interface
        // todo: handle orientation changes including activity destroyed when inactive for some time.
        // eg. alarm action activity overlays mainactivity > "new" in toolbar
        // handle using persistence: https://developer.android.com/guide/topics/resources/runtime-changes
        Intent intent = new Intent(this.getActivity(), SetAlarmActivity.class);
        intent.putExtra("alarmId", alarmEntity.getAlarmId());
        intent.putExtra("label", alarmEntity.getLabel());
        intent.putExtra("alarmOn", alarmEntity.getAlarmOn());
        intent.putExtra("hourOfDay", alarmEntity.getHourOfDay());
        intent.putExtra("minute", alarmEntity.getMinute());
        intent.putExtra("daysArr", (ArrayList<Integer>) alarmEntity.getDaysArr());
        intent.putExtra("repeatWeekly", alarmEntity.getRepeatWeekly());
        intent.putExtra("intervalTime", alarmEntity.getIntervalTime());
        intent.putExtra("durationTime", alarmEntity.getDurationTime());
        intent.putExtra("endAtTimeHour", alarmEntity.getEndAtTimeHour());
        intent.putExtra("endAtTimeMinute", alarmEntity.getEndAtTimeMinute());
        intent.putExtra("alarmStartTimeInMillis", alarmEntity.getAlarmStartTimeInMillis());
        intent.putExtra("alarmEndTimeInMillis", alarmEntity.getAlarmEndTimeInMillis());
        intent.putExtra("vibrate", alarmEntity.getVibrate());
        intent.putExtra("upcomingAlarmTime", alarmEntity.getUpcomingAlarmTime());
        intent.putExtra("ascendingVolumeMax", alarmEntity.getAscendingVolumeMax());
        intent.putExtra("choice", choice);

        startActivityForResult(intent, ALARMS_ACTIVITY_REQUEST_CODE);
    }


    // todo: Ascending volume boolean
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
        if (requestCode == ALARMS_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                int alarmId = returnIntent.getIntExtra("alarmId", 0);
                String label = returnIntent.getStringExtra("label");
                int hourOfDay = returnIntent.getIntExtra("hourOfDay", 0);
                int minute = returnIntent.getIntExtra("minute", 0);
                List<Integer> daysArr = returnIntent.getIntegerArrayListExtra("daysArr");
                Boolean repeatWeekly = returnIntent.getBooleanExtra("repeatWeekly", false);
                Boolean alarmOn = returnIntent.getBooleanExtra("alarmOn", false);
                int intervalTime = returnIntent.getIntExtra("intervalTime", 5);
                int durationTime = returnIntent.getIntExtra("durationTime", 5);
                int endAtTimeHour = returnIntent.getIntExtra("endAtTimeHour", 7);
                int endAtTimeMinute = returnIntent.getIntExtra("endAtTimeMinute", 0);
                int choice = returnIntent.getIntExtra("choice", 0);
                long alarmStartTimeInMillis = returnIntent.getLongExtra("alarmStartTimeInMillis", 0);
                long alarmEndTimeInMillis = returnIntent.getLongExtra("alarmEndTimeInMillis", 0);
                Boolean vibrate = returnIntent.getBooleanExtra("vibrate", true);
                int upcomingAlarmTime = returnIntent.getIntExtra("upcomingAlarmTime", 1);
                int ascendingVolumeMax = returnIntent.getIntExtra("ascendingVolumeMax", 6);

                AlarmEntity alarmEntity = new AlarmEntity(alarmId, hourOfDay, minute, label, alarmOn, repeatWeekly, daysArr, intervalTime, durationTime, endAtTimeHour, endAtTimeMinute, alarmStartTimeInMillis, alarmEndTimeInMillis, vibrate, upcomingAlarmTime, ascendingVolumeMax);
                saveAlarm(alarmEntity, choice);
            }
            else {
//                Toast.makeText(getActivity(), "Result_NOT_OK", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void saveAlarm(AlarmEntity alarmEntity, int choice) {
        if (alarmEntity.getAlarmOn()) {
            List<Integer> blankDaysArrayToBeSet = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0));
            String alarmEntityDaysArrayString = alarmEntity.getDaysArr().toString();
            String blankDaysArrayToBeSetString = blankDaysArrayToBeSet.toString();

            if (alarmEntityDaysArrayString.equals(blankDaysArrayToBeSetString)) {
                Calendar calendarSetDayByTime = Calendar.getInstance(Locale.getDefault());
                calendarSetDayByTime.set(Calendar.HOUR_OF_DAY, alarmEntity.getHourOfDay());
                calendarSetDayByTime.set(Calendar.MINUTE, alarmEntity.getMinute());
                calendarSetDayByTime.set(Calendar.SECOND, 0);

                int dayIndex = (calendarSetDayByTime.get(Calendar.DAY_OF_WEEK)+5)%7;
                if (calendarSetDayByTime.getTimeInMillis() < System.currentTimeMillis()) {
                    blankDaysArrayToBeSet.set((dayIndex + 1)%7, 1);     // set for next day
                }
                else {
                    blankDaysArrayToBeSet.set(dayIndex, 1);     // set for same day
                }
                alarmEntity.setDaysArr(blankDaysArrayToBeSet);
            }
            setAlarm(alarmEntity);
        }
        else {
            cancelAllAlarms(alarmEntity);
        }

        switch (choice) {
            case 1:
                //add new
                alarmsViewModel.insertAlarm(alarmEntity);
                break;
            case 2:
                //update row from activity
                alarmsViewModel.insertAlarm(alarmEntity);
                break;
            case 3:
                //update row imagebutton
                alarmsViewModel.insertAlarm(alarmEntity);
                break;
        }
    }


    public void setAlarm(AlarmEntity alarmEntity) {
        List<Integer> daysArray = alarmEntity.getDaysArr();
        int hourOfDay = alarmEntity.getHourOfDay();
        int minute = alarmEntity.getMinute();
        int endTimeHourOfDay = alarmEntity.getEndAtTimeHour();
        int endTimeMinute = alarmEntity.getEndAtTimeMinute();

        Calendar calendarCurrentTime = Calendar.getInstance(Locale.getDefault());
        Date currentTime = calendarCurrentTime.getTime();

        int requestCode = alarmEntity.getAlarmId();
        int dayRequestCode;
        int intervalRequestCode;
        long lastAlarmTimeInMillis = -1L;

        cancelAllAlarms(alarmEntity);

        // loop for each day in daysArray
        for (int day = 0; day < 7; day++) {
            dayRequestCode = requestCode + day;
            intervalRequestCode = dayRequestCode + 10;

            if (daysArray.get(day) == 1) {
                Calendar calendarAlarmTime = Calendar.getInstance(Locale.getDefault());
                calendarAlarmTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendarAlarmTime.set(Calendar.MINUTE, minute);
                calendarAlarmTime.set(Calendar.SECOND, 0);

                Calendar calendarEndTime = Calendar.getInstance(Locale.getDefault());
                calendarEndTime.set(Calendar.HOUR_OF_DAY, endTimeHourOfDay);
                calendarEndTime.set(Calendar.MINUTE, endTimeMinute);
                calendarEndTime.set(Calendar.SECOND, 0);

                calendarAlarmTime.set(Calendar.DAY_OF_WEEK, day + 2);
                calendarEndTime.set(Calendar.DAY_OF_WEEK, day + 2);

                if (calendarAlarmTime.getTime().compareTo(currentTime) < 0) {           // if alarm time is less than current time, set alarm time to next week
                    calendarAlarmTime.add(Calendar.DATE, 7);
                    calendarEndTime.add(Calendar.DATE, 7);
                }

                Long alarmStartTimeInMillis = calendarAlarmTime.getTimeInMillis();
                long endTimeInMillis = calendarEndTime.getTimeInMillis();

                if (endTimeInMillis < alarmStartTimeInMillis) {                     // if alarmTime and endTime are across 00:00AM
                    endTimeInMillis += 24*60*60*1000;
                }

                if (alarmStartTimeInMillis > lastAlarmTimeInMillis) {               // stores the max last alarm time for alarm on/off in onBind
                    lastAlarmTimeInMillis = alarmStartTimeInMillis;
                }

                setAlarmWeekly(alarmEntity, alarmStartTimeInMillis, dayRequestCode, intervalRequestCode, endTimeInMillis);                                                   // set the alarm for weekly time

                // cancel broadcast call for current time set and then reset for new alarm
                PendingIntent pendingIntentForUpcomingAlarmBCReceiver = PendingIntent.getBroadcast(getActivity().getApplicationContext(), dayRequestCode, intentForUpcomingAlarmBCReceiver, 0);
                alarmManager.cancel(pendingIntentForUpcomingAlarmBCReceiver);

                setNotifyUpcomingAlarm(alarmEntity, alarmStartTimeInMillis, dayRequestCode);
            }
            // else cancel for blank days
            // commented to test for: cancel all alarms and then set (in case reusing entity)
//            else {
//                PendingIntent pendingIntentForWeekly = PendingIntent.getBroadcast(getActivity().getApplicationContext(), dayRequestCode, intentForWeekly, 0);
//                alarmManager.cancel(pendingIntentForWeekly);
//                Toast.makeText(getActivity().getApplicationContext(), "Cancelled day: "+day, Toast.LENGTH_SHORT)
//                        .show();
//
//
//                PendingIntent pendingIntentForUpcomingAlarmBCReceiver = PendingIntent.getBroadcast(getActivity().getApplicationContext(), dayRequestCode, intentForUpcomingAlarmBCReceiver, 0);
//                alarmManager.cancel(pendingIntentForUpcomingAlarmBCReceiver);
//
//                Intent intentRepeatInterval = new Intent(getActivity().getApplicationContext(), AlarmReceiverInterval.class);
//                intervalRequestCode = dayRequestCode + 10;
//                PendingIntent pendingIntentRepeatInterval = PendingIntent.getBroadcast(context, intervalRequestCode, intentRepeatInterval, 0);
//                alarmManager.cancel(pendingIntentRepeatInterval);
//            }
        }

        // set the last alarm time in millis for entity, -1 default for repeating weekly
        if (!alarmEntity.getRepeatWeekly()) {
            alarmEntity.setLastAlarmTimeInMillis(lastAlarmTimeInMillis);
        }
    }


    // 1. Set For Weekly
    //      - for any API level: if repeating every week: repeat in AlarmReceiverWeekly
    //
    // 2. Set for repeating in intervals:
    //      - for >=API_21: set repeating in AlarmReceiverWeekly and AlarmReceiverInterval
    //      - for <API_21 : set repeating here
    private void setAlarmWeekly(AlarmEntity alarmEntity, Long alarmStartTimeInMillis, int dayRequestCode, int intervalRequestCode, long endTimeInMillis) {
        Long alarmStartTimeInSecondsLong = alarmStartTimeInMillis/1000;
        int alarmStartTimeInSeconds = alarmStartTimeInSecondsLong.intValue();

        intentForWeekly.putExtra("dayRequestCode", dayRequestCode);
        intentForWeekly.putExtra("intervalRequestCode", intervalRequestCode);
        intentForWeekly.putExtra("interval", alarmEntity.getIntervalTime());
        intentForWeekly.putExtra("duration", alarmEntity.getDurationTime());
        intentForWeekly.putExtra("endTimeInMillis", endTimeInMillis);
        intentForWeekly.putExtra("repeatWeekly", alarmEntity.getRepeatWeekly());
        intentForWeekly.putExtra("alarmStartTimeInSeconds", alarmStartTimeInSeconds);
        intentForWeekly.putExtra("vibrate", alarmEntity.getVibrate());
        intentForWeekly.putExtra("ascendingVolumeMax", alarmEntity.getAscendingVolumeMax());

        Intent intentForWakeup = new Intent(getActivity().getApplicationContext(), AlarmReceiverWakeUp.class);
        PendingIntent pendingIntentForWeekly = PendingIntent.getBroadcast(getActivity().getApplicationContext(), dayRequestCode, intentForWeekly, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntentForWakeUp = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 1, intentForWakeup, PendingIntent.FLAG_UPDATE_CURRENT);

        long weeklyStartAlarmTime = alarmStartTimeInMillis;
        long wakeUpTime = weeklyStartAlarmTime-(15*60*1000);

        // wake up device before first alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntentForWakeUp);
            Toast.makeText(context, "Alarm Set AllowWhileIdle", Toast.LENGTH_SHORT).show();
//            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(wakeUpTime, pendingIntentForWakeUp), pendingIntentForWakeUp);
        }

        if (!alarmEntity.getRepeatWeekly()) {
            // sets at lock screen as well (same as set exact and allow while idle)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(weeklyStartAlarmTime, pendingIntentForWeekly), pendingIntentForWeekly);
                  alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, weeklyStartAlarmTime, pendingIntentForWeekly);
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, weeklyStartAlarmTime, pendingIntentForWeekly);
            }

            // intervalRequestCode will be different for each day
            // for end time: pass intervalRequestCode to endTimeIntent to cancel repeating alarm
            // interval is set in AlarmReceiverWeekly, when first alarm goes off
            if (alarmEntity.getIntervalTime() != -1) {
                Intent intentEndTime = new Intent(getActivity().getApplicationContext(), AlarmReceiverEndTime.class);
                intentEndTime.putExtra("intervalRequestCode", intervalRequestCode);
                PendingIntent pendingIntentEndTime = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, intentEndTime, 0);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, endTimeInMillis, pendingIntentEndTime);
            }
        }
        else {
            // AlarmReceiverWeekly will set repeating in interval (if "interval" is set)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, weeklyStartAlarmTime, pendingIntentForWeekly);
//                alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(weeklyStartAlarmTime, pendingIntentForWeekly), pendingIntentForWeekly);
            }

            long weeklyInterval = (7*24*60*60*1000);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, weeklyStartAlarmTime, weeklyInterval, pendingIntentForWeekly);
            }

            // set once. weekly alarm repeats in AlarmWeeklyReceiver. this cancels on repeat
            if (alarmEntity.getIntervalTime() != -1) {
                Intent intentEndTime = new Intent(getActivity().getApplicationContext(), AlarmReceiverEndTime.class);
                intentEndTime.putExtra("intervalRequestCode", intervalRequestCode);
                PendingIntent pendingIntentEndTime = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, intentEndTime, 0);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, endTimeInMillis,7*24*60*60*1000 , pendingIntentEndTime);
            }
        }
    }


    private void setNotifyUpcomingAlarm(AlarmEntity alarmEntity, long alarmStartTimeInMillis, int dayRequestCode) {
        Calendar calNextAlarmTime = Calendar.getInstance(Locale.getDefault());
        calNextAlarmTime.setTimeInMillis(alarmStartTimeInMillis);
        SimpleDateFormat sdfNextAlarmTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String nextAlarmTimeFormatted = sdfNextAlarmTime.format(calNextAlarmTime.getTime());

        Long alarmStartTimeInSecondsLong = alarmStartTimeInMillis/1000;
        int alarmStartTimeInSeconds = alarmStartTimeInSecondsLong.intValue();

        intentForUpcomingAlarmBCReceiver.putExtra("dayRequestCode", dayRequestCode);
        intentForUpcomingAlarmBCReceiver.putExtra("repeatWeekly", alarmEntity.getRepeatWeekly());
        intentForUpcomingAlarmBCReceiver.putExtra("alarmStartTimeInSeconds", alarmStartTimeInSeconds);
        intentForUpcomingAlarmBCReceiver.putExtra("nextAlarmTimeFormatted", nextAlarmTimeFormatted);

        int upcomingAlarmTime = alarmEntity.getUpcomingAlarmTime();  // receive this as parameter from alarmEntity
        PendingIntent pendingIntentForUpcomingAlarmBCReceiver = PendingIntent.getBroadcast(getActivity().getApplicationContext(), dayRequestCode, intentForUpcomingAlarmBCReceiver, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmStartTimeInMillis-(upcomingAlarmTime*60*60*1000), pendingIntentForUpcomingAlarmBCReceiver);
        }
        else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmStartTimeInMillis-(upcomingAlarmTime*60*60*1000), pendingIntentForUpcomingAlarmBCReceiver);
        }

    }


    public void cancelNotifyNextAlarmNotification(int dayRequestCode) {
        PendingIntent pendingIntentForUpcomingAlarmBCReceiver = PendingIntent.getBroadcast(getActivity().getApplicationContext(), dayRequestCode, intentForUpcomingAlarmBCReceiver, 0);
        alarmManager.cancel(pendingIntentForUpcomingAlarmBCReceiver);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getActivity().getApplicationContext());
        notificationManagerCompat.cancel(dayRequestCode);
    }


    //Todo: Alarm Off on Dismiss and pending alarms: DB call from Notification action

    public void cancelAllAlarms(AlarmEntity alarmEntity) {
        int requestCode = alarmEntity.getAlarmId();
        int dayRequestCode;
        int intervalRequestCode;

        for (int day = 0; day < 7; day++) {
            dayRequestCode = requestCode + day;
            PendingIntent pendingIntentWeekly = PendingIntent.getBroadcast(getActivity().getApplicationContext(), dayRequestCode, intentForWeekly, 0);
            Toast.makeText(getActivity().getApplicationContext(), "Cancelled day: " + day, Toast.LENGTH_SHORT).show();
            alarmManager.cancel(pendingIntentWeekly);

            intervalRequestCode = dayRequestCode + 10;
            Intent intentInterval = new Intent(context, AlarmReceiverInterval.class);
            PendingIntent pendingIntentInterval = PendingIntent.getBroadcast(getActivity().getApplicationContext(), intervalRequestCode, intentInterval, 0);
            alarmManager.cancel(pendingIntentInterval);

            cancelNotifyNextAlarmNotification(dayRequestCode);
        }
        Log.e("cancelAllAlarms","cancelAllAlarms called");
        Toast.makeText(getActivity().getApplicationContext(), "Cancel All Alarms", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onResume() {
        super.onResume();
        showNextAlarmSnackBar();
//        alarmsAdapter.notifyDataSetChanged();
    }
}