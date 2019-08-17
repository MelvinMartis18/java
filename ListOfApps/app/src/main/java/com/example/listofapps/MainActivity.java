package com.example.listofapps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class StableArrayAdapter extends ArrayAdapter<String> {
    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

    public StableArrayAdapter(Context context, int textViewResourceId,
                              List<String> objects) {
        super(context, textViewResourceId, objects);
        for (int i = 0; i < objects.size(); ++i) {
            mIdMap.put(objects.get(i), i);
        }
    }

    @Override
    public long getItemId(int position) {
        String item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView listview = (ListView) findViewById(R.id.listview);
        List<String> apps = new ArrayList<>();
        final Map<String, Intent> appsMap = new HashMap<>();
        String appName = "";

        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages)
        {
            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                appName = (String)pm.getApplicationLabel(packageInfo);
                apps.add(appName);
                appsMap.put(appName, pm.getLaunchIntentForPackage(packageInfo.packageName));
//                Log.i(TAG, "Installed package :" + packageInfo.packageName);
//                Log.i(TAG, "Source dir : " + packageInfo.sourceDir);
//                Log.i(TAG, "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));
            }
        }

        Collections.sort(apps, new Comparator<String>() {
            public int compare(String str1, String str2) {
                return str1.compareTo(str2);
        }
    });


        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, apps);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id){
                String item = (String) adapter.getItemAtPosition(position);
                Intent launchIntent = appsMap.get(item);
                startActivity(launchIntent);
            }
        });



        //notificationTest();
        //initControls();
    }


//    private void notificationTest() {
//        try {
//
//            Intent intent = new Intent(this, MainActivity.class);
//
//            // Creating a pending intent and wrapping our intent
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            NotificationCompat.Builder mBuilder
//                    = new NotificationCompat.Builder(this,"CHANNEL_ID")
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle("Test")
//                    .setContentText("Example");
//
//            mBuilder.addAction(R.mipmap.ic_launcher, "Action", pendingIntent).setStyle(new NotificationCompat.MediaStyle()
//                            .setShowActionsInCompactView(0, 1));
//            //.addAction(R.mipmap.ic_launcher, "Action", pendingIntent);
//
//            RemoteViews mContentView = new RemoteViews(getPackageName(), R.layout.activity_main);
//            //mContentView.setImageViewResource(R.id.notif_icon, R.mipmap.ic_launcher);
//            mContentView.setTextViewText(R.id.text, "Custom notification");
//
//            NotificationCompat.Builder customNotification = new NotificationCompat.Builder(this, "CHANNEL_ID_2")
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
//                    .setCustomContentView(mContentView);
//
//            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            notificationManager.notify(2, mBuilder.build());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


//    private void initControls() {
//        try {
//            volumeSeekbar = findViewById(R.id.seekBar);
//            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//            volumeSeekbar.setMax(audioManager
//                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
//            volumeSeekbar.setProgress(audioManager
//                    .getStreamVolume(AudioManager.STREAM_MUSIC));
//
//
//            volumeSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
//                @Override
//                public void onStopTrackingTouch(SeekBar arg0) {
//                }
//
//                @Override
//                public void onStartTrackingTouch(SeekBar arg0) {
//                }
//
//                @Override
//                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
//                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
//                            progress, 0);
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
