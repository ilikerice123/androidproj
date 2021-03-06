package com.example.charlesbai321.myapplication.Util;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

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

    //this is done on a background thread
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        boolean start = intent.getExtras().getBoolean(MainActivity.USE_GPS);


        AlarmManager am = (AlarmManager)
                getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(getApplicationContext(), AlarmReceiver.class);

        PendingIntent pi = PendingIntent.getBroadcast(this, 0,
                i, PendingIntent.FLAG_UPDATE_CURRENT);

        if(am == null) {
            Toast.makeText(this, "Something went wrong, try again later",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if(start) {
            Log.d(MainActivity.LOG, "started logging service");
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + FIVE_MINUTES, FIVE_MINUTES, pi);
            Toast.makeText(this, "Started Logging!", Toast.LENGTH_SHORT).show();
        }
        else {
            Log.d(MainActivity.LOG, "removing logging service");
            am.cancel(pi);
            Toast.makeText(this, "Stopped Logging!", Toast.LENGTH_SHORT).show();
        }
    }
}
