package com.application.essentials;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.application.essentials.db.AlarmEntity;
import com.application.essentials.model.AlarmsViewModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;


public class SetAlarmFragment extends DialogFragment implements AlarmsDialogInterface {

    private static final long DEFAULT_END_TIME_INTERVAL = 60*60*1000;
    Context context;
    AlarmEntity alarmEntity;
    AlarmsViewModel alarmsViewModel;
    public int mHour;
    public int mMinute;
    List<Integer> daysArr = Arrays.asList(0,0,0,0,0,0,0);   // should be passed to Fragment
    String label = "";
    int alarmId;
    Boolean checked;
    Boolean alarmOn = false;
    Boolean repeatWeekly = false;
    int intervalTime;
    int durationTime;
    int endAtTimeHour;
    int endAtTimeMinute;
    Long alarmStartTimeInMillis;
    Long alarmEndTimeInMillis;
    EditText labelEditView;
    Boolean vibrate;
    int upcomingAlarmTime;
    int ascendingVolumeMax;
    final private CharSequence[] mShortWeekDayStrings = {"M","T","W","T","F","S","S"};
    public TextView ascendingVolumeSubText;

    public SetAlarmFragment() {
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        alarmsViewModel = ViewModelProviders.of(this).get(AlarmsViewModel.class);
        context = getActivity().getApplicationContext();

        Bundle bundle = getArguments();
        if (bundle != null) {
            label = bundle.getString("label");
            mHour = bundle.getInt("hourOfDay");
            mMinute = bundle.getInt("minute");
            alarmId = bundle.getInt("alarmId");
            daysArr = bundle.getIntegerArrayList("daysArr");
            alarmOn = bundle.getBoolean("alarmOn");
            repeatWeekly = bundle.getBoolean("repeatWeekly");
            intervalTime = bundle.getInt("intervalTime");
            durationTime = bundle.getInt("durationTime");
            endAtTimeHour = bundle.getInt("endAtTimeHour");
            endAtTimeMinute = bundle.getInt("endAtTimeMinute");
            alarmStartTimeInMillis = bundle.getLong("alarmStartTimeInMillis");
            alarmEndTimeInMillis = bundle.getLong("alarmEndTimeInMillis");
            vibrate = bundle.getBoolean("vibrate");
            upcomingAlarmTime = bundle.getInt("upcomingAlarmTime");
            ascendingVolumeMax = bundle.getInt("ascendingVolumeMax");
        }
        else {
            Log.e("bundleNull","Bundle in SetAlarmFragment is null");
        }

        final View v = inflater.inflate(
                R.layout.fragment_set_alarm, container, false);


        labelEditView = (EditText) v.findViewById(R.id.labelEditText);
        labelEditView.setText(label);
        labelEditView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                labelEditView.setCursorVisible(true);
            }
        });


        final Switch aSwitch = (Switch) v.findViewById(R.id.alarmSwitch);
        aSwitch.setChecked(alarmOn);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                alarmOn = isChecked;
            }
        });


        final TextView endAtTime = (TextView) v.findViewById(R.id.endAtTime);
        DecimalFormat endAtTimeFormatter = new DecimalFormat("00");
        String endAtTimeHourFormatted = endAtTimeFormatter.format(endAtTimeHour);
        String endAtTimeMinuteFormatted = endAtTimeFormatter.format(endAtTimeMinute);
        endAtTime.setText(endAtTimeHourFormatted + ":" +endAtTimeMinuteFormatted);

        final TextView tvDialogTime = (TextView) v.findViewById(R.id.tvDialogTime);
        final LinearLayout endAtLayout = (LinearLayout) v.findViewById(R.id.endAtLayout);
        DecimalFormat timeFormatter = new DecimalFormat("00");
        String mHourFormatted = timeFormatter.format(mHour);
        String mMinuteFormatted = timeFormatter.format(mMinute);

        tvDialogTime.setText(mHourFormatted + ":" + mMinuteFormatted);
        tvDialogTime.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                    // Launch Time Picker Dialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                            new TimePickerDialog.OnTimeSetListener() {

                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay,
                                                      int minute) {
                                    DecimalFormat df = new DecimalFormat("00");
                                    mHour = hourOfDay;
                                    mMinute = minute;

                                    Calendar calendarAlarmTime = Calendar.getInstance(Locale.getDefault());
                                    calendarAlarmTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    calendarAlarmTime.set(Calendar.MINUTE, minute);
                                    calendarAlarmTime.set(Calendar.SECOND, 0);
                                    alarmStartTimeInMillis = calendarAlarmTime.getTimeInMillis();

                                    tvDialogTime.setText(df.format(hourOfDay) + ":" + df.format(minute));

                                    // set endAtTime to default 1hr
                                    alarmEndTimeInMillis = alarmStartTimeInMillis + DEFAULT_END_TIME_INTERVAL;
                                    Calendar calendarEndAtTime = Calendar.getInstance(Locale.getDefault());
                                    calendarEndAtTime.setTimeInMillis(alarmEndTimeInMillis);
                                    DecimalFormat endAtTimeFormatter = new DecimalFormat("00");
                                    endAtTimeHour = calendarEndAtTime.get(Calendar.HOUR_OF_DAY);
                                    endAtTimeMinute = calendarEndAtTime.get(Calendar.MINUTE);
                                    String endAtTimeHourFormatted = endAtTimeFormatter.format(endAtTimeHour);
                                    String endAtTimeMinuteFormatted = endAtTimeFormatter.format(endAtTimeMinute);
                                    endAtTime.setText(endAtTimeHourFormatted + ":" +endAtTimeMinuteFormatted);

                                    alarmOn = true;
                                    aSwitch.setChecked(true);
                                }
                            }, mHour, mMinute, true);
                    timePickerDialog.show();
                }
            }
        );


        final CheckBox repeatWeeklyCheckBox = (CheckBox) v.findViewById(R.id.checkBoxRepeatWeekly);
        repeatWeeklyCheckBox.setChecked(repeatWeekly);
        repeatWeeklyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                repeatWeekly = isChecked;
                Log.e("repeatWeekly","repeatWeeklyChecked: "+repeatWeekly);
            }
        });

        final LinearLayout repeatDays = (LinearLayout) v.findViewById(R.id.repeat_days);
        final CompoundButton[] dayButtons = new CompoundButton[7];

        // Build button for each day
        for (int i = 0; i < 7; i++) {
            final CompoundButton dayButton = (CompoundButton) inflater.inflate(R.layout.day_button,
                    repeatDays, false);

            dayButton.setWidth(200);
            dayButton.setBackgroundResource(R.color.colorPrimary);
            dayButton.setText(mShortWeekDayStrings[i]);
            checked = daysArr.get(i) == 1;
            dayButton.setChecked(checked);

            repeatDays.addView(dayButton);
            dayButtons[i] = dayButton;
        }

        // Set onClickListener for each CompoundButton
        for (int i = 0; i < 7; i++) {
            final int buttonIndex = i;
            dayButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final boolean isChecked = ((CompoundButton) view).isChecked();
                    if (isChecked) {  daysArr.set(buttonIndex,1); }
                    else           {  daysArr.set(buttonIndex,0);  }
                }
            });
        }


        final LinearLayout intervalLayout = (LinearLayout) v.findViewById(R.id.intervalLayout);
        final TextView intervalSubText = (TextView) v.findViewById(R.id.intervalSubText);
        final List<Integer> intervalsList = new ArrayList<>(Arrays.asList(-1, 1, 3, 5, 10, 15 , 20, 30));
        final String[] intervalsText = {"OFF", "1 min", "3 mins", "5 mins", "10 mins", "15 mins" , "20 mins", "30 mins"};
        int indexOfintervalTime = intervalsList.indexOf(intervalTime);

        if (intervalsText[indexOfintervalTime].equalsIgnoreCase("OFF")) {
            intervalSubText.setText("OFF");
        }
        else {
            intervalSubText.setText("Repeat every: "+intervalsText[indexOfintervalTime]);
        }

        intervalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder intervalAlertDialog = new AlertDialog.Builder(getActivity());
                intervalAlertDialog.setTitle("Alarm Interval: ");

                intervalAlertDialog.setItems(intervalsText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int choice) {
                        intervalTime = intervalsList.get(choice);
                        if (choice == 0) {
                            intervalSubText.setText("OFF");
                        }
                        else {
                            intervalSubText.setText("Repeat every: "+intervalsText[choice]);
                        }

                    }
                });
                AlertDialog dialog = intervalAlertDialog.create();
                dialog.show();
            }
        });


        final LinearLayout durationLayout = (LinearLayout) v.findViewById(R.id.durationLayout);
        final TextView durationSubText = (TextView) v.findViewById(R.id.durationSubText);
        final String[] durationTextList = {"1 min", "2 mins", "3 mins" , "5 mins"};
        final List<Integer> durationList = new ArrayList<>(Arrays.asList(1, 2, 3, 5));
        int indexOfDuration = durationList.indexOf(durationTime);
        durationSubText.setText("Ring for: "+durationTextList[indexOfDuration]);

        durationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder intervalAlertDialog = new AlertDialog.Builder(getActivity());
                intervalAlertDialog.setTitle("Alarm Duration: ");

                intervalAlertDialog.setItems(durationTextList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int choice) {
                        durationTime = durationList.get(choice);
                        durationSubText.setText("Ring for: " +durationTextList[choice]);
                    }
                });

                AlertDialog dialog = intervalAlertDialog.create();
                dialog.show();
            }
        });

        //endAtTime text view declared before alarmTimeTextView


        endAtLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                        new TimePickerDialog.OnTimeSetListener() {

                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onTimeSet(TimePicker view, int onSetEndAtTimeHour,
                                                  int onSetEndAtTimeMinute) {
                                DecimalFormat df = new DecimalFormat("00");
                                Calendar calendarCheckEndAtTime = Calendar.getInstance(Locale.getDefault());
                                calendarCheckEndAtTime.set(Calendar.HOUR_OF_DAY, onSetEndAtTimeHour);
                                calendarCheckEndAtTime.set(Calendar.MINUTE, onSetEndAtTimeMinute);
                                calendarCheckEndAtTime.set(Calendar.SECOND, 0);
                                long alarmEndTimeInMillisTemp = calendarCheckEndAtTime.getTimeInMillis();

                                if (alarmEndTimeInMillisTemp < System.currentTimeMillis()) {
                                    alarmEndTimeInMillisTemp += 24*60*60*1000;
                                }

                                if (alarmEndTimeInMillisTemp < alarmStartTimeInMillis) {
                                    // don't set time and throw dialog exception
                                    Toast.makeText(context, "End Time cannot be less than alarm time", Toast.LENGTH_LONG).show();
                                    Log.e("alarmTimeInMillis",""+alarmStartTimeInMillis);
                                    Log.e("alarmEndTimeInMillis",""+alarmEndTimeInMillisTemp);
                                }
                                else {
                                    endAtTimeHour = onSetEndAtTimeHour;
                                    endAtTimeMinute = onSetEndAtTimeMinute;
                                    alarmEndTimeInMillis = alarmEndTimeInMillisTemp;
                                    endAtTime.setText(df.format(onSetEndAtTimeHour) + ":" + df.format(onSetEndAtTimeMinute));
                                }
                            }
                        }, endAtTimeHour, endAtTimeMinute, true);
                timePickerDialog.show();
            }
        });


        final LinearLayout alarmToneLayout = (LinearLayout) v.findViewById(R.id.alarmToneLayout);
        alarmToneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentAlarmTone = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intentAlarmTone.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select ringtone for notifications:");
                intentAlarmTone.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                intentAlarmTone.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intentAlarmTone.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
                startActivityForResult(intentAlarmTone, 100);
            }
        });


        final CheckBox vibrateCheckBox = (CheckBox) v.findViewById(R.id.vibrateCheckBox);
        vibrateCheckBox.setChecked(vibrate);
        vibrateCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                vibrate = isChecked;
                Log.e("vibrate","vibrateChecked: "+vibrate);
            }
        });


        final LinearLayout notifyNextAlarmLayout = (LinearLayout) v.findViewById(R.id.notifyNextAlarmLayout);
        final TextView notifyNextAlarmSubText = (TextView) v.findViewById(R.id.notifyNextAlarmSubText);
        final List<Integer> notifyNextAlarmTimeList = new ArrayList<>(Arrays.asList(-1, 1, 2, 4, 6, 8, 10, 12));
        final String[] notifyNextAlarmText = {"OFF", "1 hour", "2 hours", "4 hours", "6 hours", "8 hours" , "10 hours", "12 hours"};

        int indexOfNextAlarmTime = notifyNextAlarmTimeList.indexOf(upcomingAlarmTime);
        if (notifyNextAlarmText[indexOfNextAlarmTime].equalsIgnoreCase("OFF")) {
            notifyNextAlarmSubText.setText("OFF");
        }
        else {
            notifyNextAlarmSubText.setText("Notify "+ notifyNextAlarmText[indexOfNextAlarmTime] + " before alarm");
        }

        notifyNextAlarmLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder notifyNextAlarmAlertDialog = new AlertDialog.Builder(getActivity());
                notifyNextAlarmAlertDialog.setTitle("Notify Upcoming Alarm In: ");

                notifyNextAlarmAlertDialog.setItems(notifyNextAlarmText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int choice) {
                        upcomingAlarmTime = notifyNextAlarmTimeList.get(choice);
                        if (choice != 0) {
                            notifyNextAlarmSubText.setText(String.format(notifyNextAlarmTimeList.get(choice).toString(), Locale.getDefault()) + " hour(s) before alarm");
                        }
                        else {
                            notifyNextAlarmSubText.setText("OFF");
                        }
                    }
                });

                AlertDialog dialog = notifyNextAlarmAlertDialog.create();
                dialog.show();
            }
        });


        final LinearLayout ascendingVolumeLayout = (LinearLayout) v.findViewById(R.id.ascendingVolumeLayout);
        ascendingVolumeSubText = (TextView) v.findViewById(R.id.ascendingVolumeSubText);
        ascendingVolumeSubText.setText("Volume level: "+ascendingVolumeMax);
        // ascendingVolumeMax populated from

        ascendingVolumeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME ,AudioManager.FLAG_SHOW_UI);
                Toast.makeText(getActivity().getApplicationContext(), "Use volume buttons", Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent returnIntentAlarmTone) {
        if (resultCode == RESULT_OK) {
            Uri uri = returnIntentAlarmTone.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(context)) {
                    RingtoneManager.setActualDefaultRingtoneUri(
                            getActivity(),
                            RingtoneManager.TYPE_ALARM,
                            uri);
                    Toast.makeText(context, ""+uri.toString(), Toast.LENGTH_SHORT).show();
                    // Todo: Get name of alarm tone currently set and display as subtext
                }
                else {
                    // Todo: Launch write settings intent after dialog
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }
    }


    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }


    @Override
    public AlarmEntity getAlarmEntity() {
        alarmEntity = new AlarmEntity(alarmId, mHour, mMinute,  label, alarmOn, repeatWeekly, daysArr, intervalTime, durationTime, endAtTimeHour, endAtTimeMinute, alarmStartTimeInMillis, alarmEndTimeInMillis, vibrate, upcomingAlarmTime, ascendingVolumeMax);
        label = labelEditView.getText().toString();
        alarmEntity.setLabel(label);
        return alarmEntity;
    }

    @Override
    public Fragment getFragment() {
        return this;
    }
}