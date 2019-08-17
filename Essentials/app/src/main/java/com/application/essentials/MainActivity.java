package com.application.essentials;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;

import com.application.essentials.db.AlarmEntity;
import com.application.essentials.model.AdapterFragmentInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    AdapterFragmentInterface alarmsFragmentCallback;
    AlarmsFragment alarmsFragment;
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private TabLayout tabLayout;
    private AudioManager audio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Todo: Dismiss only for tomorrow: if repeat weekly -- cancelled alarm and reset in Notification receiver
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));


        // Initialization
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(2);
        tabLayout = findViewById(R.id.tabLayout);
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        alarmsFragment = new AlarmsFragment();
        mAdapter.addFragment(alarmsFragment, "Alarms");
        mAdapter.addFragment(new MemoFragment(), "Memo");
        mAdapter.addFragment(new TodoFragment(), "Todo");

        viewPager.setAdapter(mAdapter);
        tabLayout.setupWithViewPager(viewPager);

        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_new) {
            // fab icon code here
            // default values for new alarm
            AlarmEntity alarmEntity = alarmsFragment.getDefaultAlarmEntity();
            alarmsFragment.launchSetAlarmActivity(alarmEntity, 1);
            return true;
        }

        if (id == R.id.action_add_new_quick) {
            // quick alarm code here (Alert Dialog)
            final AlarmEntity alarmEntity = alarmsFragment.getDefaultAlarmEntity();
            final List<Integer> hoursList = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8));
            final List<Integer> minsList = new ArrayList<Integer>(Arrays.asList(0, 15, 20, 30, 45));

            AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
            mBuilder.setTitle("Wake Up in...");
            final View spinnerView = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
            final Spinner spinnerWakeUpInHours = spinnerView.findViewById(R.id.spinnerWakeUpInHours);
            ArrayAdapter<String> spinnerHoursAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                    getResources().getStringArray(R.array.SpinnerWakeUpInHoursList));
            spinnerHoursAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            spinnerWakeUpInHours.setAdapter(spinnerHoursAdapter);
            spinnerWakeUpInHours.setSelection(1);

            final Spinner spinnerWakeUpInMins = spinnerView.findViewById(R.id.spinnerWakeUpInMins);
            ArrayAdapter<String> spinnerMinsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                    getResources().getStringArray(R.array.SpinnerWakeUpInMinsList));
            spinnerMinsAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            spinnerWakeUpInMins.setAdapter(spinnerMinsAdapter);

            mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int hour = hoursList.get(spinnerWakeUpInHours.getSelectedItemPosition());
                    int mins = minsList.get(spinnerWakeUpInMins.getSelectedItemPosition());

                    long hourMillis = hour*60*60*1000;
                    long minsMillis = mins*60*1000;
                    long quickAlarmTimeInMillis = System.currentTimeMillis() + hourMillis + minsMillis;
                    Calendar quickAlarmCalendar = Calendar.getInstance(Locale.getDefault());
                    quickAlarmCalendar.setTimeInMillis(quickAlarmTimeInMillis);
                    int hourOfDay = quickAlarmCalendar.get(Calendar.HOUR_OF_DAY);
                    int minute = quickAlarmCalendar.get(Calendar.MINUTE);
                    alarmEntity.setHourOfDay(hourOfDay);
                    alarmEntity.setMinute(minute);

                    Calendar quickAlarmEndCalendar = Calendar.getInstance(Locale.getDefault());
                    long defaultEndTimeDuration = 60*60*1000;
                    quickAlarmEndCalendar.setTimeInMillis(quickAlarmTimeInMillis + defaultEndTimeDuration);
                    int hourOfDayEnd = quickAlarmEndCalendar.get(Calendar.HOUR_OF_DAY);
                    int minuteEnd = quickAlarmEndCalendar.get(Calendar.MINUTE);
                    alarmEntity.setEndAtTimeHour(hourOfDayEnd);
                    alarmEntity.setEndAtTimeMinute(minuteEnd);
//                    long lastAlarmTimeInMillis = quickAlarmEndCalendar.getTimeInMillis();
//                    alarmEntity.setLastAlarmTimeInMillis(lastAlarmTimeInMillis);

                    List<Integer> blankDaysArrayToBeSet = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0));
                    blankDaysArrayToBeSet.set((quickAlarmCalendar.get(Calendar.DAY_OF_WEEK)+5)%7, 1);
                    alarmEntity.setDaysArr(blankDaysArrayToBeSet);
                    alarmEntity.setLabel("TEMP");

                    alarmsFragment.saveAlarm(alarmEntity, 1);
                    dialogInterface.dismiss();
                }
            });

            mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            mBuilder.setView(spinnerView);
            AlertDialog dialog = mBuilder.create();
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audio.adjustStreamVolume(AudioManager.STREAM_ALARM,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                Toast.makeText(this, "Alarm Volume: "+audio.getStreamVolume(AudioManager.STREAM_ALARM), Toast.LENGTH_SHORT).show();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audio.adjustStreamVolume(AudioManager.STREAM_ALARM,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                Toast.makeText(this, "Alarm Volume: "+audio.getStreamVolume(AudioManager.STREAM_ALARM), Toast.LENGTH_SHORT).show();
                return true;
            case KeyEvent.KEYCODE_BACK:
                finish();
            default:
                return false;
        }
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
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}
