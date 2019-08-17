package com.application.essentials;


import android.app.Activity;
import android.app.AlarmManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.media.AudioManager;
import android.support.v4.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.application.essentials.db.AlarmEntity;
import com.application.essentials.db.AlarmsRepository;
import com.application.essentials.model.AlarmsViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SetAlarmActivity extends AppCompatActivity {

    Context context;
    SetAlarmFragment setAlarmFragment;
    Boolean checked;
    public int choice;
    public int rowPosition;
    public int rowLastPosition;
    Bundle bundle;
    private AlarmsDialogInterface listener;
    private AudioManager audio;

    public void setListener(AlarmsDialogInterface listener)
    {
        this.listener = listener ;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        setListener(setAlarmFragment);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm!=null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms);

        // From: https://stackoverflow.com/questions/11425020/actionbar-in-a-dialogfragment
        // this.requestWindowFeature(Window.FEATURE_ACTION_BAR);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.alpha = 1.0f;
        params.dimAmount = 0f;
        this.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        bundle =  getIntent().getExtras();
        choice = bundle.getInt("choice");
        rowPosition = bundle.getInt("rowPosition");
        rowLastPosition = bundle.getInt("rowLastPosition");

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_AlarmsDialog));
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setTitle(null);

        TextView tvCancel = (TextView) findViewById(R.id.toolbar_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView tvSave = (TextView) findViewById(R.id.toolbar_save);
        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                       // todo_ ensure fragment is called and getAlarmEntity is accessible before click
                Intent returnIntent = new Intent();
                AlarmEntity alarmEntity = listener.getAlarmEntity();
                Log.e("returnIntent","alarmEntity: "+alarmEntity);
                returnIntent.putExtra("alarmId", alarmEntity.getAlarmId());
                returnIntent.putExtra("label", alarmEntity.getLabel());
                returnIntent.putExtra("alarmOn", alarmEntity.getAlarmOn());
                returnIntent.putExtra("repeatWeekly", alarmEntity.getRepeatWeekly());
                returnIntent.putExtra("hourOfDay", alarmEntity.getHourOfDay());
                returnIntent.putExtra("minute", alarmEntity.getMinute());
                returnIntent.putExtra("daysArr", (ArrayList<Integer>) alarmEntity.getDaysArr());
                returnIntent.putExtra("intervalTime", alarmEntity.getIntervalTime());
                returnIntent.putExtra("durationTime", alarmEntity.getDurationTime());
                returnIntent.putExtra("endAtTimeHour", alarmEntity.getEndAtTimeHour());
                returnIntent.putExtra("endAtTimeMinute", alarmEntity.getEndAtTimeMinute());
                returnIntent.putExtra("choice", choice);
                returnIntent.putExtra("rowPosition", rowPosition);
                returnIntent.putExtra("rowLastPosition", rowLastPosition);
                returnIntent.putExtra("alarmStartTimeInMillis", alarmEntity.getAlarmStartTimeInMillis());
                returnIntent.putExtra("alarmEndTimeInMillis", alarmEntity.getAlarmEndTimeInMillis());
                returnIntent.putExtra("vibrate", alarmEntity.getVibrate());
                returnIntent.putExtra("upcomingAlarmTime", alarmEntity.getUpcomingAlarmTime());
                returnIntent.putExtra("ascendingVolumeMax", alarmEntity.getAscendingVolumeMax());
                setResult(Activity.RESULT_OK, returnIntent);

                finish();
            }
        });


        if (savedInstanceState == null) {
            Log.e("savedInstance","null");
            setAlarmFragment = new SetAlarmFragment();
            setAlarmFragment.setArguments(bundle);
            setListener(setAlarmFragment);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.layout_alarms_dialog, setAlarmFragment, "TAG_AlarmsDialog").commit();
        }

        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audio.adjustStreamVolume(AudioManager.STREAM_ALARM,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                Toast.makeText(this, "Alarm Volume: "+audio.getStreamVolume(AudioManager.STREAM_ALARM), Toast.LENGTH_SHORT).show();
                setAlarmFragment.ascendingVolumeSubText.setText("Volume level: " + audio.getStreamVolume(AudioManager.STREAM_ALARM));
                setAlarmFragment.ascendingVolumeMax = audio.getStreamVolume(AudioManager.STREAM_ALARM);
                return true;

            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audio.adjustStreamVolume(AudioManager.STREAM_ALARM,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                Toast.makeText(this, "Alarm Volume: "+audio.getStreamVolume(AudioManager.STREAM_ALARM), Toast.LENGTH_SHORT).show();
                setAlarmFragment.ascendingVolumeSubText.setText("Volume level: " + audio.getStreamVolume(AudioManager.STREAM_ALARM));
                setAlarmFragment.ascendingVolumeMax = audio.getStreamVolume(AudioManager.STREAM_ALARM);
                return true;

            case KeyEvent.KEYCODE_BACK:
                // Todo: handle back pressed: Save/Cancel
                finish();

            default:
                return false;
        }
    }

}