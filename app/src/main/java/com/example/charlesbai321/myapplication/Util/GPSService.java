package com.example.charlesbai321.myapplication.Util;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
 * this class is completely useless now. XDDD Read below for why
 *
 * This has been replaced with a service that starts AlarmManager, of which calls
 * ParseLocationService
 *
 * Originally, this was how my app was going to request locations every 15 or so minutes,
 * but because services don't run when the CPU is a asleep and obtaining a constant CPU
 * wakelock is not a valid solution, this class is now deprecated.
 */
public class GPSService extends Service {
    public static final int USE_GPS = 3;

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
    }

    public boolean checkPermission(){
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void parseLocation(Location l){
        Log.d(MainActivity.LOG, l.toString());
        Toast.makeText(this, l.toString(), Toast.LENGTH_SHORT).show();
        Toast.makeText(this,
                db.monitoredLocationDao().getListOfLocations().toString(),
                Toast.LENGTH_SHORT).show();
    }

    private void initializeLocationRequest(){
        if(checkPermission()) {
            lr = new LocationRequest();
            lr.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            //TODO: might see if I need to tweak this
            lr.setFastestInterval(10000/*TEN_MINUTES*/);
            lr.setInterval(10000/*TEN_MINUTES*/);

            //initialize client
            locationclient = LocationServices.getFusedLocationProviderClient(this);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(lr);
            //LocationSettingsRequest locationSettingsRequest = builder.build();

            locationclient.requestLocationUpdates(lr, callback, null);
        }
        else{
            Toast.makeText(this, "No permissions for GPS", Toast.LENGTH_SHORT).show();
        }
    }


}
