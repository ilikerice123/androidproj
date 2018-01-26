package com.example.charlesbai321.myapplication.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.charlesbai321.myapplication.Activities.MainActivity;

/**
 * Created by charlesbai321 on 23/01/18.
 */

//TODO: add to manifest file
public class AlarmReciever extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "HI CHARLES", Toast.LENGTH_LONG).show();
        Intent i = new Intent(context, GPSService.class);
        i.putExtra(MainActivity.GPS_TOGGLE, "true");
        context.startService(i);

    }
}
