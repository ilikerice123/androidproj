package com.example.charlesbai321.myapplication.Util;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.charlesbai321.myapplication.Activities.MainActivity;

/**
 * Created by charlesbai321 on 27/01/18.
 */

/**
 * this class is for setting and cancelling the Alarm that wakes up location logging service
 * called from MainActivity when the user clicks a button
 * depending on the button, it will either cancel or set the alarm.
 */
public class StartGPSService extends IntentService {

    public final int FIVE_MINUTES = 5*60*1000;

    public StartGPSService(){
        super("StartGPSService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        boolean start = intent.getExtras().getBoolean(MainActivity.USE_GPS);
        AlarmManager am = (AlarmManager)
                getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(getApplicationContext(), ParseLocationService.class);
        PendingIntent pi = PendingIntent.getService(getApplicationContext(), 0,
                i, PendingIntent.FLAG_UPDATE_CURRENT);
        if(start) {
            Log.d(MainActivity.LOG, "started logging service");
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + FIVE_MINUTES, FIVE_MINUTES, pi);
        }
        else {
            if(am != null){
                Log.d(MainActivity.LOG, "removing logging service");
                am.cancel(pi);
            }
        }
    }
}
