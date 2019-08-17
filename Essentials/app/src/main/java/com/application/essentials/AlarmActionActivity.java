package com.application.essentials;

import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmActionActivity extends AppCompatActivity {
    private final String CHANNEL_ID_NEXT_ALARM = "NEXT_ALARM";
    private final String CHANNEL_NAME_NEXT_ALARM = "Next Alarm";
    private final String CHANNEL_ID_MISSED_ALARM= "MISSED_ALARM";
    private final String CHANNEL_NAME_MISSED_ALARM = "Missed Alarm";
    private AudioManager audio;
    Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    MediaPlayer mp = new MediaPlayer();
    AudioManager audioManager;
    Vibrator vib;
    NotificationManagerCompat notificationManagerCompat;
    Boolean handleSlideFlag;
    String alarmTimeFormatted;
    String alarmDayFormatted;
    Boolean vibrate;
    int ascendingVolumeMax;
    int interval;
    int duration;
    long endTimeInMillis;
    int intervalRequestCode;
    int dayRequestCode;
    Boolean repeatWeekly;
    Integer alarmStartTimeInSeconds;
    Boolean ascendingVolume;
    private long alarmTimeInMillis;
    private boolean vibrateReceiverUnregistered;
    private Timer volumeTimer;
    private int alarmVolumeLevelToRestore;
    private Handler durationHandler;
    private boolean alarmStopped;


    private void getAllExtras() {
        interval =  getIntent().getIntExtra("interval",5);        // declaring this outside onCreate doesn't work. NullPointer for getIntent()
        duration = getIntent().getIntExtra("duration",1);
        endTimeInMillis =  getIntent().getLongExtra("endTimeInMillis",0);
        intervalRequestCode = getIntent().getIntExtra("intervalRequestCode", 0);
        dayRequestCode = getIntent().getIntExtra("dayRequestCode", 0);
        repeatWeekly = getIntent().getBooleanExtra("repeatWeekly", false);
        alarmStartTimeInSeconds = getIntent().getIntExtra("alarmStartTimeInSeconds", 0);
        vibrate = getIntent().getBooleanExtra("vibrate", true);
        ascendingVolumeMax =  getIntent().getIntExtra("ascendingVolumeMax", 6);
        // todo: pass to activity from UI, UI/Entity field boolean
        // todo: dismiss next alarm notification when alarm off
        ascendingVolume = getIntent().getBooleanExtra("ascendingVolume", true);
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_action);
        setWindowProperties();
        getAllExtras();
        notificationManagerCompat = NotificationManagerCompat.from(AlarmActionActivity.this);
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        handleSlideFlag = false;
        alarmStopped = false;

        alarmTimeInMillis = System.currentTimeMillis();
        Calendar calAlarmTime = Calendar.getInstance(Locale.getDefault());
        calAlarmTime.setTimeInMillis(alarmTimeInMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        alarmTimeFormatted = sdf.format(calAlarmTime.getTime());
        SimpleDateFormat sdfForDay = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        alarmDayFormatted = sdfForDay.format(calAlarmTime.getTime());

        // set Alarm Time to Activity
        TextView tvAlarmTime = (TextView) findViewById(R.id.alarmTime);
        tvAlarmTime.setText(alarmTimeFormatted);
        TextView tvAlarmDay = (TextView) findViewById(R.id.alarmDay);
        tvAlarmDay.setText(alarmDayFormatted);

        ringAlarm();
        createNotificationOngoing();

        // When duration completed (expired)
        durationHandler = new Handler();
        durationHandler.postDelayed(new Runnable() {
            public void run() {
                handleSlideFlag = false;
                stopAndEndAlarm();
            }
        }, duration*60*1000);

        SlideButton slideButton = (SlideButton) findViewById(R.id.slideButton);
        slideButton.setSlideButtonListener(new SlideButtonListener() {
            @Override
            public void handleSlide() {
                handleSlideFlag = true;
                stopAndEndAlarm();
            }
        });

        slideButton.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()  {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)  {
                if (seekBar.getProgress() >= 90) { seekBar.setProgress(90); }
                if (seekBar.getProgress() <= 10) { seekBar.setProgress(10); }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }


    private void setWindowProperties() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O ) {
            this.getWindow()
                    .addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else {
            setTurnScreenOn(true);
            setShowWhenLocked(true);
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
        }

        // https://stackoverflow.com/questions/21724420/how-to-hide-navigation-bar-permanently-in-android-activity
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(flags);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            decorView.setSystemUiVisibility(flags);
                        }
                    }
            });
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    private void stopAndEndAlarm() {
        stopRingAlarm();
        alarmStopped = true;
        Toast.makeText(this, "stopAndEndAlarm called", Toast.LENGTH_LONG).show();
        Log.e("stopAndEndAlarm","stopAndEndAlarm");

        setContentView(R.layout.activity_alarm_off);

        if (interval != -1) {
            endAlarm();
        }
        else {
            Toast.makeText(this, "interval is -1", Toast.LENGTH_LONG).show();
            // last alarm
            TextView nextAlarmText = (TextView) findViewById(R.id.nextAlarmText);
            nextAlarmText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
            nextAlarmText.setText(R.string.lastAlarmText);
            notificationManagerCompat.cancel(0);
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 5000);
    }


    private void endAlarm() {
        Toast.makeText(this, "endAlarm called", Toast.LENGTH_LONG).show();
        long nextAlarmTimeInMillis = alarmTimeInMillis + interval*60*1000;
        Log.e("endAlarm","nextAlarmTimeInMillis: "+ nextAlarmTimeInMillis + ", endTimeInMillis" + endTimeInMillis);
        // set Next Alarm Time to Activity
        if (nextAlarmTimeInMillis < endTimeInMillis) {
            Calendar calNextAlarmTime = Calendar.getInstance(Locale.getDefault());
            calNextAlarmTime.setTimeInMillis(nextAlarmTimeInMillis);
            SimpleDateFormat sdfNextAlarmTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String nextAlarmTimeFormatted = sdfNextAlarmTime.format(calNextAlarmTime.getTime());
            SimpleDateFormat sdfForDay = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
            String nextAlarmDayFormatted = sdfForDay.format(calNextAlarmTime.getTime());

            TextView nextAlarmText = (TextView) findViewById(R.id.nextAlarmText);
            TextView nextAlarmTime = (TextView) findViewById(R.id.nextAlarmTime);
            TextView alarmDayInNext = (TextView) findViewById(R.id.alarmDayInNext);
            nextAlarmText.setText(R.string.next_alarm);
            nextAlarmTime.setText(nextAlarmTimeFormatted);
            alarmDayInNext.setText(nextAlarmDayFormatted);

            final Animation in = new AlphaAnimation(0.0f, 1.0f);
            in.setDuration(2000);
            nextAlarmText.startAnimation(in);
            nextAlarmTime.startAnimation(in);
            alarmDayInNext.startAnimation(in);

            createNotificationForNextAlarmDismiss(nextAlarmTimeFormatted);
        }
        else {
            // last alarm
            Toast.makeText(this, "nextAlarmTimeInMillis exceeds endTimeInMillis", Toast.LENGTH_LONG).show();
            Log.e("endAlarm","nextAlarmTimeInMillis exceeds endTimeInMillis");
            TextView nextAlarmText = (TextView) findViewById(R.id.nextAlarmText);
            nextAlarmText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
            nextAlarmText.setText(R.string.lastAlarmText);

            notificationManagerCompat.cancel(0);
        }
    }


    public BroadcastReceiver vibrateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                long[] vibratePattern = new long[]{0,2000,2000};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vib.vibrate(VibrationEffect.createWaveform(vibratePattern, 0));
                }
                else {
                    vib.vibrate(vibratePattern, 0);
                }
            }
        }
    };


    private void ringAlarm() {
        alarmVolumeLevelToRestore = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0);
        volumeTimer = new Timer();
        // increment alarm volume every 15 seconds
        volumeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int alarmVolumeLoop = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                if (alarmVolumeLoop < ascendingVolumeMax) {
                    alarmVolumeLoop++;
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, alarmVolumeLoop, 0);
//                    Toast.makeText(AlarmActionActivity.this, "Alarm Volume: "+audioManager.getStreamVolume(AudioManager.STREAM_ALARM), Toast.LENGTH_SHORT).show();
                }
            }
        }, 15000, 15000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            mp.setAudioAttributes(audioAttributes);
        }
        else {
            mp.setAudioStreamType(AudioManager.STREAM_ALARM);
        }

        try {
            mp.setLooping(true);
            mp.setDataSource(getApplicationContext(), alarmUri);
            mp.prepareAsync();
        } catch (IOException e) {
            Log.e("mediaPlayerFailed: ", ""+e.getMessage());
        }

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mp.start();
                long[] vibratePattern = new long[]{0,2000,2000};

                if (vibrate) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vib.vibrate(VibrationEffect.createWaveform(vibratePattern, 0));
                    }
                    else {
                        vib.vibrate(vibratePattern, 0);
                    }
                    registerReceiver(vibrateReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
                }
            }
        });
    }


    private void createNotificationOngoing() {
        Toast.makeText(this, "ongoing", Toast.LENGTH_LONG).show();
    }


    private void createNotificationForNextAlarmDismiss(String nextAlarmTimeFormatted) {
        Intent intentNotificationDismissAll = new Intent(AlarmActionActivity.this, NotificationReceiver.class);
        intentNotificationDismissAll.putExtra("notification_action", "DISMISS");
        intentNotificationDismissAll.putExtra("intervalRequestCode", intervalRequestCode);
        intentNotificationDismissAll.putExtra("dayRequestCode", dayRequestCode);
        intentNotificationDismissAll.putExtra("repeatWeekly", repeatWeekly);
        intentNotificationDismissAll.putExtra("alarmStartTimeInSeconds", alarmStartTimeInSeconds);
        PendingIntent pendingIntentNotificationDismissAll = PendingIntent.getBroadcast(AlarmActionActivity.this, 1, intentNotificationDismissAll, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) AlarmActionActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel nChannelNextAlarm = new NotificationChannel(
                    CHANNEL_ID_NEXT_ALARM, CHANNEL_NAME_NEXT_ALARM, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(nChannelNextAlarm);
        }

        NotificationCompat.Action actionDismissAll =
                new NotificationCompat.Action.Builder(0, "DISMISS", pendingIntentNotificationDismissAll)
                        .build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(AlarmActionActivity.this, CHANNEL_ID_NEXT_ALARM)
                .setSmallIcon(R.drawable.ic_round_schedule_24px)
                .setContentTitle("Next Alarm")
                .setContentText(nextAlarmTimeFormatted)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(actionDismissAll)
                .setOngoing(true);

        notificationManagerCompat.notify(0, builder.build());
    }


    private void createNotificationForMissedAlarm(String alarmTimeFormatted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) AlarmActionActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel nChannelMissedAlarm = new NotificationChannel(
                    CHANNEL_ID_MISSED_ALARM, CHANNEL_NAME_MISSED_ALARM, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(nChannelMissedAlarm);
        }

        String GROUP_MISSED_ALARMS = "GROUP_MISSED_ALARMS";

        NotificationCompat.Builder groupBuilder =
                new NotificationCompat.Builder(AlarmActionActivity.this, CHANNEL_ID_MISSED_ALARM)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setGroupSummary(true)
                        .setGroup(GROUP_MISSED_ALARMS);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(AlarmActionActivity.this, CHANNEL_ID_MISSED_ALARM)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Missed Alarm")
                .setContentText(alarmTimeFormatted)
                .setGroup(GROUP_MISSED_ALARMS)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        int uniqueId = (int) System.currentTimeMillis();
        notificationManagerCompat.notify(10, groupBuilder.build());
        notificationManagerCompat.notify(uniqueId, builder.build());
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
            default:
                return false;
        }
    }


    private void stopRingAlarm() {
        // to avoid IllegalState exception
        if (!alarmStopped) {
            mp.reset();
            mp.release();
        }

        vib.cancel();
        volumeTimer.cancel();
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, alarmVolumeLevelToRestore, 0);
        durationHandler.removeCallbacksAndMessages(null);
        //Toast.makeText(this, "cancel ongoing", Toast.LENGTH_SHORT).show();

        if (!vibrateReceiverUnregistered) {
            try {
                unregisterReceiver(vibrateReceiver);
                vibrateReceiverUnregistered = true;
            }
            catch(IllegalArgumentException e) { e.printStackTrace(); }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRingAlarm();
        alarmStopped = true;
        Log.e("onDestroy", "handleSlide: "+handleSlideFlag);

        if (!handleSlideFlag) {
            createNotificationForMissedAlarm(alarmTimeFormatted);
        }
    }
}