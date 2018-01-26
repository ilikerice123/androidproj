package com.example.charlesbai321.myapplication.Util;

import android.Manifest;
import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.charlesbai321.myapplication.Activities.MainActivity;
import com.example.charlesbai321.myapplication.Data.MonitoredLocation;
import com.example.charlesbai321.myapplication.Data.MonitoredLocationsDatabase;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

/**
 * Created by charlesbai321 on 18/01/18.
 */


/**
 * was originally gonna use this for periodically checking location and logging data to my
 * database, but then I decided it's better to use alarm manager to call a service intent or
 * something
 */
public class GPSService extends Service {
    public static final int USE_GPS = 3;
    public static final String TAG = "lol";

    private boolean restart;
    private MonitoredLocationsDatabase db;
    private LocationRequest lr;
    private FusedLocationProviderClient locationclient;
    private LocationCallback callback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult){
            //TODO: check if the location is close to anywhere!
            Location l = locationResult.getLastLocation();
            parseLocation(l);
        }
    };

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Toast.makeText(this, "Service started!", Toast.LENGTH_SHORT).show();
        restart = intent.getExtras().getBoolean(MainActivity.GPS_TOGGLE);
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate(){
        initializeLocationRequest();
        db = Room.databaseBuilder(this,
                MonitoredLocationsDatabase.class, MonitoredLocation.DATABASE_KEY)
                .allowMainThreadQueries().build();
    }

    @Override
    public void onDestroy(){
        if(callback != null) {
            locationclient.removeLocationUpdates(callback);
        }
        if(restart){
            //TODO: this is dangerous...
            Toast.makeText(this, "Restarting", Toast.LENGTH_SHORT).show();
            Intent broadcast = new Intent("restartService");
            sendBroadcast(broadcast);
        }
    }

    public boolean checkPermission(){
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void parseLocation(Location l){
        Toast.makeText(this, l.toString(), Toast.LENGTH_SHORT).show();
    }

    private void initializeLocationRequest(){
        if(checkPermission()) {
            lr = new LocationRequest();
            lr.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            lr.setFastestInterval(10000/*60 * 1000*/); //milliseconds, 1 minutes
            lr.setInterval(10000/*5 * 60 * 10000*/); //slowest, 10 minutes

            //initialize client
            locationclient = LocationServices.getFusedLocationProviderClient(this);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(lr);
            LocationSettingsRequest locationSettingsRequest = builder.build();

            locationclient.requestLocationUpdates(lr, callback, null);
        }
        else{
            Toast.makeText(this, "No permissions for GPS", Toast.LENGTH_SHORT).show();
        }
    }


}
