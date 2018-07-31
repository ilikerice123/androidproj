package com.example.charlesbai321.myapplication.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.charlesbai321.myapplication.Activities.MainActivity;

/**
 * Created by charlesbai321 on 23/01/18.
 */

/**
 * not needed - unregistered in manifest file.
 * This was originally used as a way to restart the service when it gets shut down,
 * but we don't need that anymore because we are not using a persistence service to
 * request GPS
 */

public class AlarmReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(MainActivity.LOG, "Hi charles");

        Intent i = new Intent(context, GPSService.class);
        i.putExtra(MainActivity.LOCSTARTSTOP, true);
        context.startService(i);
    }
}
